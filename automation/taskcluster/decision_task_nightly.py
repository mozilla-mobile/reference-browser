# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

"""
Decision task for nightly releases.
"""

from __future__ import print_function

import argparse
import json
import lib.tasks
import os
import taskcluster

TASK_ID = os.environ.get('TASK_ID')
SCHEDULER_ID = os.environ.get('SCHEDULER_ID')
GITHUB_HTTP_REPOSITORY = os.environ.get('MOBILE_HEAD_REPOSITORY')
HEAD_REV = os.environ.get('MOBILE_HEAD_REV')

BUILDER = lib.tasks.TaskBuilder(
    task_id=TASK_ID,
    owner="skaspari@mozilla.com",
    source='{}/raw/{}/.taskcluster.yml'.format(GITHUB_HTTP_REPOSITORY, HEAD_REV),
    scheduler_id=SCHEDULER_ID
)


def generate_build_task(apks):
    artifacts = {'public/{}'.format(os.path.basename(apk)): {
        "type": 'file',
        "path": "/build/reference-browser/{}".format(apk),
        "expires": taskcluster.stringDate(taskcluster.fromNow('1 year')),
    } for apk in apks}

    checkout = 'git clone {} && cd reference-browser && git checkout {}'.format(GITHUB_HTTP_REPOSITORY, HEAD_REV)

    return taskcluster.slugId(), BUILDER.build_task(
        name="(Reference Browser) Build task",
        description="Build Reference Browser from source code.",
        command=('cd .. && ' + checkout +
                 ' && python automation/taskcluster/helper/get-secret.py'
                 ' -s project/mobile/reference-browser/sentry -k dsn -f .sentry_token'
                 ' && ./gradlew --no-daemon -PcrashReportingEnabled clean test assembleRelease'),
        features={
            "chainOfTrust": True,
            "taskClusterProxy": True
        },
        artifacts=artifacts,
        scopes=[
            "secrets:get:project/mobile/reference-browser/sentry"
        ]
    )


def generate_signing_task(build_task_id, apks, is_staging):
    artifacts = ["public/{}".format(os.path.basename(apk)) for apk in apks]

    index = "index.project.mobile.reference-browser.{}.latest".format(
        'staging-nightly' if is_staging else 'nightly')
    routes = [index]
    scopes = [
        "project:mobile:reference-browser:releng:signing:format:autograph_apk_reference_browser",
        "project:mobile:reference-browser:releng:signing:cert:{}".format(
            'dep-signing' if is_staging else 'release-signing'),
        "queue:route:{}".format(index),
    ]

    return taskcluster.slugId(), BUILDER.build_signing_task(
        build_task_id,
        name="(Reference Browser) Signing task",
        description="Sign release builds of Reference Browser",
        apks=artifacts,
        scopes=scopes,
        routes=routes,
        signing_format='autograph_apk_reference_browser',
        is_staging=is_staging
    )


def generate_push_task(signing_task_id, apks, commit, is_staging):
    artifacts = ["public/{}".format(os.path.basename(apk)) for apk in apks]

    return taskcluster.slugId(), BUILDER.build_push_task(
        signing_task_id,
        name="(Reference Browser) Push task",
        description="Upload signed release builds of Reference Browser to Google Play",
        apks=artifacts,
        scopes=[
            "project:mobile:focus:releng:googleplay:product:reference-browser{}".format(':dep' if is_staging else '')
        ],
        commit=commit,
        is_staging=is_staging
    )


def populate_chain_of_trust_required_but_unused_files():
    # These files are needed to keep chainOfTrust happy. However, they have no need for Reference Browser
    # at the moment. For more details, see: https://github.com/mozilla-releng/scriptworker/pull/209/files#r184180585

    for file_name in ('actions.json', 'parameters.yml'):
        with open(file_name, 'w') as f:
            json.dump({}, f)


def nightly(apks, commit, is_staging):
    queue = taskcluster.Queue({'baseUrl': 'http://taskcluster/queue/v1'})

    task_graph = {}

    build_task_id, build_task = generate_build_task(apks)
    lib.tasks.schedule_task(queue, build_task_id, build_task)

    task_graph[build_task_id] = {}
    task_graph[build_task_id]['task'] = queue.task(build_task_id)

    sign_task_id, sign_task = generate_signing_task(build_task_id, apks, is_staging)
    lib.tasks.schedule_task(queue, sign_task_id, sign_task)

    task_graph[sign_task_id] = {}
    task_graph[sign_task_id]['task'] = queue.task(sign_task_id)

    # TODO enable pushing once first APK has been set up for bug #1512173
    # push_task_id, push_task = generate_push_task(sign_task_id, apks, commit, is_staging)
    # lib.tasks.schedule_task(queue, push_task_id, push_task)

    # task_graph[push_task_id] = {}
    # task_graph[push_task_id]['task'] = queue.task(push_task_id)

    print(json.dumps(task_graph, indent=4, separators=(',', ': ')))

    with open('task-graph.json', 'w') as f:
        json.dump(task_graph, f)

    populate_chain_of_trust_required_but_unused_files()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description='Create a release pipeline (build, sign, publish) on taskcluster.')

    parser.add_argument('--commit', dest="commit", action="store_true", help="commit the google play transaction")
    parser.add_argument('--apk', dest="apks", metavar="path", action="append", help="Path to APKs to sign and upload",
                        required=True)
    parser.add_argument('--output', dest="track", metavar="path", action="store", help="Path to the build output",
                        required=True)
    parser.add_argument('--staging', action="store_true", help="Perform a staging build (use dep workers, "
                                                               "don't communicate with Google Play) ")

    result = parser.parse_args()
    apks = ["{}/{}".format(result.track, apk) for apk in result.apks]
    nightly(apks, result.commit, result.staging)
