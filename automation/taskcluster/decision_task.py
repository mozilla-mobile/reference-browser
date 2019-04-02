# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

"""
Decision task for pull requests and pushes
"""

from __future__ import print_function
import datetime
import os
import taskcluster
import sys

import lib.build_variants
import lib.tasks

TASK_ID = os.environ.get('TASK_ID')
REPO_URL = os.environ.get('MOBILE_HEAD_REPOSITORY')
BRANCH = os.environ.get('MOBILE_HEAD_BRANCH')
COMMIT = os.environ.get('MOBILE_HEAD_REV')
PR_TITLE = os.environ.get('GITHUB_PULL_TITLE', '')
BUILD_WORKER_TYPE = os.environ.get('BUILD_WORKER_TYPE', '')


# If we see this text inside a pull request title then we will not execute any tasks for this PR.
SKIP_TASKS_TRIGGER = '[ci skip]'


def create_task(name, description, command, scopes=None, treeherder=None, artifacts=None):
    return create_raw_task(
        name,
        description,
        full_command='./gradlew --no-daemon clean {}'.format(command),
        scopes=scopes,
        treeherder=treeherder,
        artifacts=artifacts,
    )


def create_raw_task(name, description, full_command, scopes=None, treeherder=None, artifacts=None):
    scopes = [] if scopes is None else scopes
    treeherder = {} if treeherder is None else treeherder
    artifacts = {} if artifacts is None else artifacts

    created = datetime.datetime.now()
    expires = taskcluster.fromNow('1 year')
    deadline = taskcluster.fromNow('1 day')

    return {
        "workerType": BUILD_WORKER_TYPE,
        "taskGroupId": TASK_ID,
        "expires": taskcluster.stringDate(expires),
        "retries": 5,
        "created": taskcluster.stringDate(created),
        "tags": {},
        "priority": "lowest",
        "schedulerId": "taskcluster-github",
        "provisionerId": "aws-provisioner-v1",
        "deadline": taskcluster.stringDate(deadline),
        "dependencies": [ TASK_ID ],
        "routes": [
            "tc-treeherder.v2.reference-browser.{}".format(COMMIT)
        ],
        "scopes": scopes,
        "requires": "all-completed",
        "payload": {
            "features": {
                'taskclusterProxy': True
            },
            "maxRunTime": 7200,
            "image": "mozillamobile/android-components:1.15",
            "command": [
                "/bin/bash",
                "--login",
                "-cx",
                "cd .. && git clone %s && cd reference-browser && git config advice.detachedHead false && git checkout %s && %s" % (REPO_URL, COMMIT, full_command)
            ],
            "artifacts": artifacts,
            "env": {
                "TASK_GROUP_ID": TASK_ID
            }
        },
        "extra": {
            "treeherder": treeherder,
        },
        "metadata": {
            "name": name,
            "description": description,
            "owner": "android-components-team@mozilla.com",
            "source": "https://github.com/mozilla-mobile/android-components"
        }
    }


def create_variant_assemble_task(variant):
    return create_task(
        name="assemble: %s" % variant,
        description='Building and testing variant ' + variant,
        command='assemble{} && ls -R /build/reference-browser/'.format(variant.capitalize()),
        treeherder={
            'jobKind': 'build',
            'machine': {
              'platform': _craft_treeherder_platform_from_variant(variant),
            },
            'symbol': 'A',
            'tier': 1,
        },
        artifacts=_craft_artifacts_from_variant(variant),
    )


def create_variant_test_task(variant):
    return create_task(
        name="test: %s" % variant,
        description='Building and testing variant ' + variant,
        command='test{}UnitTest && ls -R /build/reference-browser/'.format(variant.capitalize()),
        treeherder={
            'jobKind': 'test',
            'machine': {
              'platform': _craft_treeherder_platform_from_variant(variant),
            },
            'symbol': 'T',
            'tier': 1,
        },
    )

def _craft_treeherder_platform_from_variant(variant):
    architecture, build_type = _get_architecture_and_build_type_from_variant(variant)
    return 'android-{}-{}'.format(architecture, build_type)


def _craft_artifacts_from_variant(variant):
    arch, _ = _get_architecture_and_build_type_from_variant(variant)
    return {
        'public/target.{}.apk'.format(arch): {
            'type': 'file',
            'path': _craft_apk_full_path_from_variant(variant),
            'expires': taskcluster.stringDate(taskcluster.fromNow(lib.tasks.DEFAULT_EXPIRES_IN)),
        }
    }


def _craft_apk_full_path_from_variant(variant):
    architecture, build_type = _get_architecture_and_build_type_from_variant(variant)

    short_variant = variant[:-len(build_type)]
    shorter_variant = short_variant[:-len(architecture)]
    postfix = '-unsigned' if build_type == 'release' else ''

    return '/build/reference-browser/app/build/outputs/apk/{short_variant}/{build_type}/app-{shorter_variant}-{architecture}-{build_type}{postfix}.apk'.format(
        architecture=architecture,
        build_type=build_type,
        short_variant=short_variant,
        shorter_variant=shorter_variant,
        postfix=postfix
    )


def _get_architecture_and_build_type_from_variant(variant):
    variant = variant.lower()

    architecture = None
    if 'aarch64' in variant:
        architecture = 'aarch64'
    elif 'x86' in variant:
        architecture = 'x86'
    elif 'arm' in variant:
        architecture = 'arm'

    build_type = None
    if variant.endswith('debug'):
        build_type = 'debug'
    elif variant.endswith('release'):
        build_type = 'release'

    if not architecture or not build_type:
        raise ValueError(
            'Unsupported variant "{}". Found architecture, build_type: {}'.format(
                variant, (architecture, build_type)
            )
        )

    return architecture, build_type

def create_detekt_task():
    return create_task(
        name='detekt',
        description='Running detekt over all modules',
        command='detekt',
        treeherder={
            'jobKind': 'test',
            'machine': {
              'platform': 'lint',
            },
            'symbol': 'detekt',
            'tier': 1,
        }
    )


def create_ktlint_task():
    return create_task(
        name='ktlint',
        description='Running ktlint over all modules',
        command='ktlint',
        treeherder={
            'jobKind': 'test',
            'machine': {
              'platform': 'lint',
            },
            'symbol': 'ktlint',
            'tier': 1,
        }
    )


def create_lint_task():
    return create_task(
        name='lint',
        description='Running tlint over all modules',
        command='lint',
        treeherder={
            'jobKind': 'test',
            'machine': {
              'platform': 'lint',
            },
            'symbol': 'lint',
            'tier': 1,
        }
    )


def create_compare_locales_task():
    return create_raw_task(
        name='compare-locales',
        description='Validate strings.xml with compare-locales',
        full_command='pip install "compare-locales>=4.0.1,<5.0" && compare-locales --validate l10n.toml .',
        treeherder={
            'jobKind': 'test',
            'machine': {
              'platform': 'lint',
            },
            'symbol': 'compare-locale',
            'tier': 2,
        }
    )


if __name__ == "__main__":
    if SKIP_TASKS_TRIGGER in PR_TITLE:
        print("Pull request title contains", SKIP_TASKS_TRIGGER)
        print("Exit")
        exit(0)

    queue = taskcluster.Queue({ 'baseUrl': 'http://taskcluster/queue/v1' })

    print("Fetching build variants from gradle")
    variants = lib.build_variants.from_gradle()

    if len(variants) == 0:
        print("Could not get build variants from gradle")
        sys.exit(2)

    print("Got variants: " + ' '.join(variants))

    for variant in variants:
        lib.tasks.schedule_task(queue, taskcluster.slugId(), create_variant_assemble_task(variant))
        lib.tasks.schedule_task(queue, taskcluster.slugId(), create_variant_test_task(variant))

    lib.tasks.schedule_task(queue, taskcluster.slugId(), create_detekt_task())
    lib.tasks.schedule_task(queue, taskcluster.slugId(), create_ktlint_task())
    lib.tasks.schedule_task(queue, taskcluster.slugId(), create_compare_locales_task())
    lib.tasks.schedule_task(queue, taskcluster.slugId(), create_lint_task())
