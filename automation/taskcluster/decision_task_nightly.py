# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

"""
Decision task for releases
"""

from __future__ import print_function
import datetime
import json
import os
import taskcluster

import lib.tasks

TASK_ID = os.environ.get('TASK_ID')
HEAD_REV = os.environ.get('MOBILE_HEAD_REV')

def generate_build_task():
    created = datetime.datetime.now()
    expires = taskcluster.fromNow('1 year')
    deadline = taskcluster.fromNow('1 day')

    command = "automation/taskcluster/actions/nightly.sh"

    return {
        "workerType": 'gecko-focus',
        "taskGroupId": TASK_ID,
        "expires": taskcluster.stringDate(expires),
        "retries": 5,
        "created": taskcluster.stringDate(created),
        "tags": {},
        "priority": "lowest",
        "schedulerId": "focus-nightly-sched",
        "deadline": taskcluster.stringDate(deadline),
        "dependencies": [ TASK_ID ],
        "routes": [
            "index.project.mobile.reference-browser.nightly.latest"
        ],
        "scopes": [
            "queue:route:index.project.mobile.reference-browser.nightly.*",
            "secrets:get:project/mobile/reference-browser/preview-key-store",
            "secrets:get:project/mobile/reference-browser/sentry"
        ],
        "requires": "all-completed",
        "payload": {
            "features": {
                'taskclusterProxy': True
            },
            "maxRunTime": 7200,
            "image": "mozillamobile/android-components:1.9",
            "command": [
                "/bin/bash",
                "--login",
                "-cx",
                "cd .. && git clone https://github.com/mozilla-mobile/reference-browser.git && cd reference-browser && %s" % command
            ],
            "artifacts": {
                "public": {
                    "type": "directory",
                    "path": "/build/reference-browser/release",
                    "expires": taskcluster.stringDate(expires)
                }
            },
            "env": {
                "TASK_GROUP_ID": TASK_ID
            }
        },
        "provisionerId": "aws-provisioner-v1",
        "metadata": {
            "name": "build",
            "description": "Building reference browser nightly",
            "owner": "skaspari@mozilla.com",
            "source": "https://github.com/mozilla-mobile/android-components"
        }
    }

def nightly():
    queue = taskcluster.Queue({'baseUrl': 'http://taskcluster/queue/v1'})

    task_graph = {}
    build_task_id = taskcluster.slugId()
    build_task = generate_build_task()
    lib.tasks.schedule_task(queue, build_task_id, build_task)

    task_graph[build_task_id] = {}
    task_graph[build_task_id]["task"] = queue.task(build_task_id)

    print(json.dumps(task_graph, indent=4, separators=(',', ': ')))

    task_graph_path = "task-graph.json"
    with open(task_graph_path, 'w') as f:
        json.dump(task_graph, f)

if __name__ == "__main__":
    nightly()
