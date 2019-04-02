# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

import datetime
import json
import taskcluster

DEFAULT_EXPIRES_IN = '1 year'


class TaskBuilder(object):
    def __init__(self, task_id, commit, owner, source, scheduler_id, build_worker_type):
        self.task_id = task_id
        self.commit = commit
        self.owner = owner
        self.source = source
        self.scheduler_id = scheduler_id
        self.build_worker_type = build_worker_type
        self.tasks_priority = 'lowest'  # TODO Parametrize

    def build_task(self, name, description, command, artifacts={}, scopes=[], features={}):
        return self.craft_default_task_definition(
            worker_type=self.build_worker_type,
            provisioner_id='aws-provisioner-v1',
            dependencies=[],
            routes=[],
            scopes=scopes,
            name=name,
            description=description,
            payload={
                "features": features,
                "maxRunTime": 7200,
                "image": "mozillamobile/android-components:1.15",
                "command": [
                    "/bin/bash",
                    "--login",
                    "-cx",
                    command
                ],
                "artifacts": artifacts,
            },
            treeherder={
                'jobKind': 'build',
                'machine': {
                  'platform': 'android-all',
                },
                'symbol': 'NA',
                'tier': 1,
            },
        )

    def craft_signing_task(self, build_task_id, name, description, signing_format, is_staging, apks=[], scopes=[], routes=[]):
        return self.craft_default_task_definition(
            worker_type='mobile-signing-dep-v1' if is_staging else 'mobile-signing-v1',
            provisioner_id='scriptworker-prov-v1',
            dependencies=[build_task_id],
            routes=routes,
            scopes=scopes,
            name=name,
            description=description,
            payload={
                "maxRunTime": 3600,
                "upstreamArtifacts": [{
                    "paths": apks,
                    "formats": [signing_format],
                    "taskId": build_task_id,
                    "taskType": "build"
                }]
            },
            treeherder={
                'jobKind': 'other',
                'machine': {
                  'platform': 'android-all',
                },
                'symbol': 'Ns',
                'tier': 1,
            },
        )

    def craft_push_task(self, signing_task_id, name, description, is_staging, apks=[], scopes=[]):
        return self.craft_default_task_definition(
            worker_type='mobile-pushapk-dep-v1' if is_staging else 'mobile-pushapk-v1',
            provisioner_id='scriptworker-prov-v1',
            dependencies=[signing_task_id],
            routes=[],
            scopes=scopes,
            name=name,
            description=description,
            payload={
                "commit": True,
                "google_play_track": 'nightly',
                "upstreamArtifacts": [{
                    "paths": apks,
                    "taskId": signing_task_id,
                    "taskType": 'signing'
                }],
            },
            treeherder={
                'jobKind': 'other',
                'machine': {
                  'platform': 'android-all',
                },
                'symbol': 'gp',
                'tier': 1,
            },
        )

    def craft_upload_apk_nimbledroid_task(self, build_task_id, name, description, command, dependencies, scopes):
        created = datetime.datetime.now()
        expires = taskcluster.fromNow('1 year')
        deadline = taskcluster.fromNow('1 day')

        return {
            "workerType":  self.build_worker_type,
            "taskGroupId": self.task_id,
            "schedulerId": self.scheduler_id,
            "expires": taskcluster.stringDate(expires),
            "retries": 5,
            "created": taskcluster.stringDate(created),
            "tags": {},
            "priority": 'lowest',
            "deadline": taskcluster.stringDate(deadline),
            "dependencies": [self.task_id, build_task_id],
            "routes": [],
            "scopes": scopes,
            "requires": 'all-completed',
            "payload": {
                "features": {
                "taskclusterProxy": True
                },
                "maxRunTime": 7200,
                "image": "mozillamobile/android-components:1.15",
                "command": [
                    "/bin/bash",
                    "--login",
                    "-cx",
                    command
                ],
                "artifacts": {},
                "deadline": taskcluster.stringDate(deadline)
            },
            "provisionerId": 'aws-provisioner-v1',
            "metadata": {
                "name": name,
                "description": description,
                "owner": self.owner,
                "source": self.source
            }
        }

    def craft_default_task_definition(
        self, worker_type, provisioner_id, dependencies, routes, scopes, name, description,
        payload, treeherder
    ):
        created = datetime.datetime.now()
        deadline = taskcluster.fromNow('1 day')
        expires = taskcluster.fromNow(DEFAULT_EXPIRES_IN)

        return {
            "provisionerId": provisioner_id,
            "workerType": worker_type,
            "taskGroupId": self.task_id,
            "schedulerId": self.scheduler_id,
            "created": taskcluster.stringDate(created),
            "deadline": taskcluster.stringDate(deadline),
            "expires": taskcluster.stringDate(expires),
            "retries": 5,
            "tags": {},
            "priority": self.tasks_priority,
            "dependencies": [self.task_id] + dependencies,
            "requires": "all-completed",
            "routes": [
                "tc-treeherder.v2.reference-browser.{}".format(self.commit)
            ] + routes,
            "scopes": scopes,
            "payload": payload,
            "extra": {
                "treeherder": treeherder,
            },
            "metadata": {
                "name": name,
                "description": description,
                "owner": self.owner,
                "source": self.source,
            },
        }


def schedule_task(queue, taskId, task):
    print "TASK", taskId
    print json.dumps(task, indent=4, separators=(',', ': '))

    result = queue.createTask(taskId, task)
    print "RESULT", taskId
    print json.dumps(result)
