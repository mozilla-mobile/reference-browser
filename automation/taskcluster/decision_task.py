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

def create_task(name, description, command, scopes = []):
    return create_raw_task(name, description, "./gradlew --no-daemon clean %s" % command, scopes)

def create_raw_task(name, description, full_command, scopes = []):
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
        "deadline": taskcluster.stringDate(deadline),
        "dependencies": [ TASK_ID ],
        "routes": [],
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
            "artifacts": {},
            "env": {
                "TASK_GROUP_ID": TASK_ID
            }
        },
        "provisionerId": "aws-provisioner-v1",
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
        command="assemble" + variant.capitalize())

def create_variant_test_task(variant):
    return create_task(
        name="test: %s" % variant,
        description='Building and testing variant ' + variant,
        command="test" + variant.capitalize() + "UnitTest")

def create_detekt_task():
    return create_task(
        name='detekt',
        description='Running detekt over all modules',
        command='detekt')

def create_ktlint_task():
    return create_task(
        name='ktlint',
        description='Running ktlint over all modules',
        command='ktlint')

def create_lint_task():
    return create_task(
        name='lint',
        description='Running tlint over all modules',
        command='lint')

def create_compare_locales_task():
    return create_raw_task(
        name='compare-locales',
        description='Validate strings.xml with compare-locales',
        full_command='pip install "compare-locales>=4.0.1,<5.0" && compare-locales --validate l10n.toml .')


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
