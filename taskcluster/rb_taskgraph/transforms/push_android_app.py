# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
"""
Apply some defaults and minor modifications to the jobs defined in the
push_bundle kind.
"""


from taskgraph.transforms.base import TransformSequence
from taskgraph.util.treeherder import inherit_treeherder_from_dep


transforms = TransformSequence()


@transforms.add
def build_android_app_task(config, tasks):
    for task in tasks:
        dep = task.pop("primary-dependency")
        task["attributes"] = dep.attributes.copy()
        if "aab" in task["attributes"]:
            paths = ["public/target.aab"]
            task_type = "signing-bundle"
        else:
            paths = list(dep.attributes["apks"].values())
            task_type = "signing"
        task["dependencies"] = {task_type: dep.label}
        task["name"] = dep.label[len(dep.kind) + 1 :]
        if "run_on_tasks_for" in task["attributes"]:
            task["run-on-tasks-for"] = task["attributes"]["run_on_tasks_for"]

        task["treeherder"] = inherit_treeherder_from_dep(task, dep)
        task["worker"]["upstream-artifacts"] = [
            {
                "taskId": {"task-reference": f"<{task_type}>"},
                "taskType": "signing",
                "paths": paths,
            }
        ]
        task["worker"]["dep"] = config.params["level"] != "3"
        yield task
