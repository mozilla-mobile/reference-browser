# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
"""
Apply some defaults and minor modifications to the jobs defined in the build
kind.
"""


from taskgraph.transforms.base import TransformSequence
from taskgraph.util.treeherder import inherit_treeherder_from_dep


transforms = TransformSequence()


@transforms.add
def build_pushapk_task(config, tasks):
    for task in tasks:
        dep = task.pop("primary-dependency")
        task["dependencies"] = {"signing": dep.label}
        task["name"] = dep.label[len(dep.kind) + 1 :]
        task["attributes"] = dep.attributes.copy()
        if "run_on_tasks_for" in task["attributes"]:
            task["run-on-tasks-for"] = task["attributes"]["run_on_tasks_for"]

        task["treeherder"] = inherit_treeherder_from_dep(task, dep)
        task["worker"]["upstream-artifacts"] = [
            {
                "taskId": {"task-reference": "<signing>"},
                "taskType": "signing",
                "paths": list(dep.attributes["apks"].values()),
            }
        ]
        task["worker"]["dep"] = config.params["level"] != "3"
        yield task
