# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
"""
Apply some defaults and minor modifications to the jobs defined in the build
kind.
"""


from taskgraph.transforms.base import TransformSequence

transforms = TransformSequence()


@transforms.add
def build_signing_task(config, tasks):
    for task in tasks:
        dep = task["primary-dependency"]
        task["dependencies"] = {"build": dep.label}
        task["worker"]["upstream-artifacts"] = [
            {
                "taskId": {"task-reference": "<build>"},
                "taskType": "build",
                "paths": list(dep.attributes["apks"].values()),
                "formats": ["autograph_apk"],
            }
        ]
        del task["primary-dependency"]
        yield task
