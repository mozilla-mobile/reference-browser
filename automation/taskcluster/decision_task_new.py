import argparse
from enum import Enum
import json
import subprocess
from typing import List

import taskcluster

import decisionlib
from decisionlib.scheduler import Scheduler

ABIS = ('aarch64', 'arm', 'x86')


def project_shell_task(
    name: str,
    commands: str,
    artifacts: List[decisionlib.AndroidArtifact] = (),
):
    image = 'mozillamobile/android-components:1.15'
    return decisionlib.shell_task(name, image, commands, artifacts)


class Track(Enum):
    NIGHTLY = 'nightly'
    STAGING_NIGHTLY = 'staging-nightly'


class Variant:
    def __init__(self, raw: str, flavor: str, engine: str, abi: str, build_type: str):
        self.raw = raw
        self.flavor = flavor
        self.engine = engine
        self.abi = abi
        self.build_type = build_type
        self.signed_by_default = build_type == 'debug'

    @staticmethod
    def from_gradle_variant_string(raw_variant: str):
        # The variant string is composed of three pieces (engine, abi, and build type)
        # but it doesn't delimit them. So, we need to keep track of all the possible prefixes
        # (engines) and middle bits (abis), and solve for the build type. Awesome.

        if not raw_variant.startswith('geckoNightly'):
            raise ValueError('This variant ("{}") does not start with the only supported '
                             'engine of "geckoNightly"'.format(raw_variant))
        engine = 'geckoNightly'

        for supported_abi in ABIS:
            if raw_variant[len(engine):].startswith(supported_abi):
                abi = supported_abi
                break
        else:
            raise ValueError('This variant ("{}") does not match any of our supported '
                             'architectures ({})'.format(raw_variant, ABIS))

        build_type = raw_variant[len(engine + abi)]
        return Variant(raw_variant, engine + abi, engine, abi, build_type)


def from_gradle():
    process = subprocess.Popen(["./gradlew", "--no-daemon", "--quiet", "printBuildVariants"],
                               stdout=subprocess.PIPE)
    (output, err) = process.communicate()
    exit_code = process.wait()

    if exit_code is not 0:
        print("Gradle command returned error: {}".format(exit_code))

    variants_line = [line for line in output.split('\n') if line.startswith('variants: ')][0]
    variants_json = variants_line.split(' ', 1)[1]
    variants = json.loads(variants_json)

    return [Variant.from_gradle_variant_string(raw_variant) for raw_variant in variants]


def pull_request(scheduler: Scheduler, pr_title):
    if '[ci skip]' in pr_title:
        print('Pull request title contains "[ci skip]"')
        print('Exit')
        return {}

    for variant in from_gradle():
        unsigned = '' if variant.signed_by_default else '-unsigned'
        treeherder_platform = 'android-{}-{}'.format(variant.abi, variant.build_type)
        output_path = '{flavor}/{build_type}/app-{engine}-{abi}-{build_type}{unsigned}.apk'.format(
            flavor=variant.flavor,
            build_type=variant.build_type,
            engine=variant.engine,
            abi=variant.abi,
            unsigned=unsigned,
        )

        project_shell_task(
            'Assemble: {}'.format(variant.raw),
            './gradlew --no-daemon -PcrashReports=true clean assemble{}'.format(
                variant.raw.capitalize()),
            [decisionlib.AndroidArtifact('public/target.apk', output_path)]
        ) \
            .with_treeherder('build', treeherder_platform, 1, 'A', variant.engine) \
            .schedule(scheduler)

        project_shell_task(
            'Test: {}'.format(variant.raw),
            './gradlew --no-daemon -PcrashReports=true clean test{}UnitTest'.format(
                variant.raw.capitalize()),
        ) \
            .with_treeherder('test', treeherder_platform, 1, 'T', variant.engine) \
            .schedule(scheduler)


def master_push(scheduler: Scheduler):
    return scheduler


def release(scheduler: Scheduler, track: Track):
    prefix_secret = '{}/project/mobile'.format(
        'garbage/staging' if track == Track.STAGING_NIGHTLY else '')
    sentry_secret = '{}/reference-browser/sentry'.format(prefix_secret)
    nimbledroid_secret = '{}/reference-browser/nimbledroid'.format(prefix_secret)

    assemble_task_id = project_shell_task(
        'Assemble',
        './gradlew --no-daemon -PcrashReports=true clean test assembleRelease',
        [decisionlib.AndroidArtifact(
            'public/target.{}.apk'.format(arch),
            "geckoNightly{}/release/app-geckoNightly-{}-release-unsigned.apk".format(
                arch.capitalize(), arch)
        ) for arch in ABIS],
    ) \
        .append_file_secret(sentry_secret, 'dsn', '.sentry_token') \
        .with_treeherder('build', 'android-all', 1, 'NA') \
        .map(
        lambda builder, _: builder.with_notify_owner() if track == Track.NIGHTLY else None) \
        .schedule(scheduler)

    signing_task_id = decisionlib.signing_task(
        'Sign',
        'autograph_apk',
        decisionlib.SigningType.DEP,
        [(assemble_task_id, ['public/target.{}.apk'.format(arch) for arch in ABIS])],
    ) \
        .with_treeherder('other', 'android-all', 1, 'Ns') \
        .schedule(scheduler)

    decisionlib.google_play_task(
        'Push',
        'nightly',
        [(signing_task_id, ['public/target.{}.apk'.format(arch) for arch in ABIS])],
    ) \
        .with_treeherder('other', 'android-all', 1, 'gp') \
        .schedule(scheduler)

    project_shell_task(
        'Nimbledroid',
        'curl --location https://queue.taskcluster.net/v1/task/{}/artifacts/public/target.arm.apk'
        ' > target.arm.apk'.format(assemble_task_id) +
        '&& python automation/taskcluster/upload_apk_nimbledroid.py',
    ) \
        .append_secret(nimbledroid_secret) \
        .append_dependency(assemble_task_id) \
        .with_treeherder('test', 'android-all', 2, 'nd') \
        .schedule(scheduler)

    return scheduler


def main():
    parser = argparse.ArgumentParser(
        description='Decides on a graph of tasks to submit to Taskcluster'
    )

    parser.add_argument('--task-group-id', action='store_true', required=True)
    parser.add_argument('--owner', action='store', required=True)
    parser.add_argument('--source', action='store', required=True)

    command_subparser = parser.add_subparsers(dest='command')
    command_subparser.add_parser('master-push')

    pr_parser = command_subparser.add_parser('pull-request')
    pr_parser.add_argument('--pr-title', action='store', required=True)

    release_parser = command_subparser.add_parser('release')
    release_parser.add_argument('--track', action='store', required=True,
                                choices=['nightly', 'staging-nightly'])

    result = parser.parse_args()
    scheduler = Scheduler()
    if result.command == 'pull-request':
        scheduler = pull_request(scheduler, result.pr_title)
        trust_level = decisionlib.TrustLevel.L1
    elif result.command == 'master-push':
        scheduler = master_push(scheduler)
        trust_level = decisionlib.TrustLevel.L3
    else:
        scheduler = release(scheduler, Track(result.track))
        trust_level = decisionlib.TrustLevel.L3

    trigger = decisionlib.Trigger(result.task_group_id, trust_level, result.owner, result.source)
    checkout = decisionlib.Checkout.from_cwd()
    queue = taskcluster.Queue({'baseUrl': 'http://taskcluster/queue/v1'})
    scheduler.schedule_tasks(queue, trigger, checkout)


if __name__ == '__main__':
    main()
