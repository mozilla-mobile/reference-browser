import argparse
import datetime
from enum import Enum
from typing import List

import arrow
import taskcluster
from decisionlib import decision as decisionlib

from gradle import load_geckoview_nightly_version, load_variants
from variant import Variant, ABIS


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
    for variant in load_variants():
        variant_assemble_task(scheduler, variant)
        variant_test_task(scheduler, variant)


def master_push(
        scheduler: decisionlib.Scheduler,
        mozharness_task_id: decisionlib.SlugId,
        gecko_revision: str
):
    scheduler.append_all(lint_tasks())
    for variant in load_variants():
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
        pip install decisionlib
        API_KEY=`decisionlib get-secret {secret} api_key`
        curl --location https://queue.taskcluster.net/v1/task/{task_id}/artifacts/public/target.arm.apk > target.arm.apk
        curl -F username="$API_KEY" -F password="" -F apk=@target.arm.apk https://nimbledroid.com/api/v2/apks -F auto_scenarios=false
        curl --location https://index.taskcluster.net/v1/task/gecko.v2.mozilla-central.latest.mobile.android-api-16-opt/artifacts/public/build/geckoview_example.apk > geckoview_example_nd.apk
        curl -F username="$API_KEY" -F password="" -F apk=@geckoview_example_nd.apk https://nimbledroid.com/api/v2/apks -F auto_scenarios=false
        """.format(secret=nimbledroid_secret, task_id=assemble_task_id)
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

    command_subparser = parser.add_subparsers(dest='command')
    command_subparser.required = True
    command_subparser.add_parser('master-push')

    pr_parser = command_subparser.add_parser('pull-request')
    pr_parser.add_argument('--pr-title', required=True)

    release_parser = command_subparser.add_parser('release')
    release_parser.add_argument('--date', required=True)
    release_parser.add_argument('--track', required=True,
                                choices=['nightly', 'staging-nightly'])

    result = parser.parse_args()
    checkout = decisionlib.Checkout.from_cwd()
    trigger = decisionlib.Trigger.from_environment()
    # queue = taskcluster.Queue({'baseUrl': 'http://taskcluster/queue/v1'})
    scheduler = decisionlib.Scheduler()
    if result.command == 'pull-request':
        pull_request(scheduler, result.pr_title)
    elif result.command == 'master-push':
        geckoview_nightly_version = load_geckoview_nightly_version()
        mozharness_task_id = taskcluster_get_geckoview_task_id(geckoview_nightly_version)
        # gecko_revision = queue.task(mozharness_task_id)['payload']['env']['GECKO_HEAD_REV']
        gecko_revision = '<gecko_revision>'
        master_push(scheduler, mozharness_task_id, gecko_revision)
    else:
        date = arrow.get(result.date)
        release(scheduler, Track(result.track), date, checkout.commit)

    class Bonk:
        def createTask(self, _, __):
            pass

        def task(self, task_id):
            return task_id

    scheduler.schedule_tasks(Bonk(), trigger, checkout)


if __name__ == '__main__':
    main()
