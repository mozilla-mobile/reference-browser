import argparse
import datetime
from enum import Enum
import json
import subprocess
from typing import List

import arrow
import taskcluster

from decisionlib import decision as decisionlib

ABIS = ('aarch64', 'arm', 'x86')


def project_shell_task(
        name: str,
        script: str,
        artifacts: List[decisionlib.AndroidArtifact] = (),
):
    image = 'mozillamobile/android-components:1.15'
    return decisionlib.shell_task(name, image, script, artifacts)


def gradle_task(
        name: str,
        gradle_command: str,
        artifacts: List[decisionlib.AndroidArtifact] = (),
):
    command = './gradlew --no-daemon -PcrashReports=true clean {}'.format(gradle_command)
    return project_shell_task(name, command, artifacts)


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

    def platform(self):
        return 'android-{}-{}'.format(self.abi, self.build_type)

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
                             'abis ({})'.format(raw_variant, ABIS))

        build_type = raw_variant[len(engine + abi)]
        return Variant(raw_variant, engine + abi, engine, abi, build_type)


def gradle_get_variants():
    variants_json = _run_gradle_process('printBuildVariants', 'variants: ')
    variants = json.loads(variants_json)
    return [Variant.from_gradle_variant_string(raw_variant) for raw_variant in variants]


def gradle_get_geckoview_nightly_version():
    nightly_version = _run_gradle_process('printGeckoviewVersions', 'nightly: ')
    nightly_version = nightly_version.strip('"')
    return nightly_version


def _run_gradle_process(gradle_command, output_prefix):
    process = subprocess.Popen(["./gradlew", "--no-daemon", "--quiet", gradle_command],
                               stdout=subprocess.PIPE)
    output, err = process.communicate()
    exit_code = process.wait()

    if exit_code is not 0:
        print("Gradle command returned error: {}".format(exit_code))

    output_line = [line for line in output.split('\n') if line.startswith(output_prefix)][0]
    return output_line.split(' ', 1)[1]


def lint_tasks():
    return [
        gradle_task('detekt', 'detekt').with_treeherder('test', 'lint', 1, 'detekt'),
        gradle_task('ktlint', 'ktlint').with_treeherder('test', 'lint', 1, 'ktlint'),
        gradle_task('lint', 'lint').with_treeherder('test', 'lint', 1, 'lint'),
        project_shell_task(
            'compare-locales',
            """
            pip install "compare-locales>5.0.2,<6.0"
            compare-locales --validate l10n.toml .
            """
        ).with_treeherder('test', 'lint', 2, 'compare-locale')
    ]


def variant_assemble_task(scheduler: decisionlib.Scheduler, variant: Variant):
    unsigned = '' if variant.signed_by_default else '-unsigned'
    output_path = '{flavor}/{build_type}/app-{engine}-{abi}-{build_type}{unsigned}.apk'.format(
        flavor=variant.flavor,
        build_type=variant.build_type,
        engine=variant.engine,
        abi=variant.abi,
        unsigned=unsigned,
    )

    return gradle_task(
        'assemble: {}'.format(variant.raw),
        'assemble{}'.format(variant.raw.capitalize()),
        [decisionlib.AndroidArtifact('public/target.apk', output_path)]
    ) \
        .with_treeherder('build', variant.platform(), 1, 'A', variant.engine) \
        .schedule(scheduler)


def variant_test_task(scheduler: decisionlib.Scheduler, variant: Variant):
    gradle_task(
        'test: {}'.format(variant.raw),
        'test{}UnitTest'.format(variant.raw.capitalize()),
    ) \
        .with_treeherder('test', variant.platform(), 1, 'T', variant.engine) \
        .schedule(scheduler)


def pull_request(scheduler: decisionlib.Scheduler, pr_title):
    if '[ci skip]' in pr_title:
        print('Pull request title contains "[ci skip]"')
        print('Exit')
        return {}

    scheduler.append_all(lint_tasks())
    for variant in gradle_get_variants():
        variant_assemble_task(scheduler, variant)
        variant_test_task(scheduler, variant)


def master_push(
        scheduler: decisionlib.Scheduler,
        mozharness_task_id: decisionlib.SlugId,
        gecko_revision: str
):
    scheduler.append_all(lint_tasks())
    for variant in gradle_get_variants():
        assemble_task_id = variant_assemble_task(scheduler, variant)
        variant_test_task(scheduler, variant)

        if variant.abi in ('aarch64', 'arm') and variant.build_type == 'releaseRaptor':
            sign_task_id = decisionlib.sign_task(
                'sign: {}'.format(variant.raw),
                'autograph_apk_reference_browser',
                decisionlib.SigningType.DEP,
                [(assemble_task_id, ['public/target.apk'])],
            ) \
                .with_treeherder('other', variant.platform(), 1, 'As', variant.engine) \
                .schedule(scheduler)

            decisionlib.raptor_task(
                'raptor speedometer: {}'.format(variant.raw),
                decisionlib.RemoteArtifact(sign_task_id, 'public/target.apk'),
                mozharness_task_id,
                variant.abi == 'arm',
                'refbrow',
                'org.mozilla.reference.browser',
                'GeckoViewActivity',
                gecko_revision
            ) \
                .with_treeherder('test', variant.platform(), 2, 'sp') \
                .schedule(scheduler)


def release_sign_task_routes(track: Track, date: datetime.datetime, commit: str):
    index_release = 'signed-nightly' if track == Track.NIGHTLY else 'staging-signed-nightly'
    return [
        route.format(
            prefix='index.project.mobile.reference-browser.{}'.format(index_release),
            year=date.year,
            month=date.month,
            day=date.day,
            commit=commit,
        ) for route in (
            '{prefix}.nightly.{year}.{month}.{day}.latest',
            '{prefix}.nightly.{year}.{month}.{day}.revision.{commit}',
            '{prefix}.nightly.latest',
        )
    ]


def release(scheduler: decisionlib.Scheduler, track: Track, date: datetime.datetime, commit: str):
    prefix_secret = '{}/project/mobile'.format(
        'garbage/staging' if track == Track.STAGING_NIGHTLY else '')
    sentry_secret = '{}/reference-browser/sentry'.format(prefix_secret)
    nimbledroid_secret = '{}/reference-browser/nimbledroid'.format(prefix_secret)

    assemble_task_id = gradle_task(
        'assemble',
        'assembleRelease',
        [decisionlib.AndroidArtifact(
            'public/target.{}.apk'.format(abi),
            "geckoNightly{}/release/app-geckoNightly-{}-release-unsigned.apk".format(
                abi.capitalize(), abi)
        ) for abi in ABIS],
    ) \
        .append_file_secret(sentry_secret, 'dsn', '.sentry_token') \
        .with_treeherder('build', 'android-all', 1, 'NA') \
        .map(lambda task, _: task.with_notify_owner() if track == Track.NIGHTLY else None) \
        .schedule(scheduler)

    sign_task_id = decisionlib.sign_task(
        'Sign',
        'autograph_apk',
        decisionlib.SigningType.DEP,
        [(assemble_task_id, ['public/target.{}.apk'.format(abi) for abi in ABIS])],
    ) \
        .with_treeherder('other', 'android-all', 1, 'Ns') \
        .with_routes(release_sign_task_routes(track, date, commit)) \
        .schedule(scheduler)

    decisionlib.google_play_task(
        'Push',
        'nightly',
        [(sign_task_id, ['public/target.{}.apk'.format(abi) for abi in ABIS])],
    ) \
        .with_treeherder('other', 'android-all', 1, 'gp') \
        .schedule(scheduler)

    project_shell_task(
        'nimbledroid',
        """
        curl --location https://queue.taskcluster.net/v1/task/{task_id}/artifacts/public/target.arm.apk > target.arm.apk
        python automation/taskcluster/upload_apk_nimbledroid.py
        """.format(task_id=assemble_task_id)
    ) \
        .append_secret(nimbledroid_secret) \
        .append_dependency(assemble_task_id) \
        .with_treeherder('test', 'android-all', 2, 'nd') \
        .schedule(scheduler)


def taskcluster_get_geckoview_task_id(geckoview_nightly_version):
    nightly_build_id = geckoview_nightly_version.split('.')[-1]
    nightly_date = arrow.get(nightly_build_id, 'YYYYMMDDHHmmss')

    raptor_index = 'gecko.v2.mozilla-central.pushdate.{}.{:02}.{:02}.{}.firefox.linux64-debug' \
        .format(nightly_date.year, nightly_date.month, nightly_date.day, nightly_build_id)
    return taskcluster.Index().findTask(raptor_index)['taskId']


def main():
    parser = argparse.ArgumentParser(
        description='Decides on a graph of tasks to submit to Taskcluster'
    )

    parser.add_argument('--task-group-id', action='store', required=True)
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
    checkout = decisionlib.Checkout.from_cwd()
    queue = taskcluster.Queue({'baseUrl': 'http://taskcluster/queue/v1'})
    scheduler = decisionlib.Scheduler()
    if result.command == 'pull-request':
        pull_request(scheduler, result.pr_title)
        trust_level = decisionlib.TrustLevel.L1
    elif result.command == 'master-push':
        geckoview_nightly_version = gradle_get_geckoview_nightly_version()
        mozharness_task_id = taskcluster_get_geckoview_task_id(geckoview_nightly_version)
        gecko_revision = queue.task(mozharness_task_id)['payload']['env']['GECKO_HEAD_REV']
        master_push(scheduler, mozharness_task_id, gecko_revision)
        trust_level = decisionlib.TrustLevel.L3
    else:
        release(scheduler, Track(result.track), datetime.datetime.now(), checkout.commit)
        trust_level = decisionlib.TrustLevel.L3

    trigger = decisionlib.Trigger(result.task_group_id, trust_level, result.owner, result.source)
    scheduler.schedule_tasks(queue, trigger, checkout)


if __name__ == '__main__':
    main()
