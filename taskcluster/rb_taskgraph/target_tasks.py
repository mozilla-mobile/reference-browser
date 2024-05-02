# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

import os

from redo import retry
from taskgraph.target_tasks import register_target_task
from taskgraph.util.taskcluster import find_task_id


def index_exists(index_path, reason=""):
    print(f"Looking for existing index {index_path} {reason}...")
    try:
        task_id = find_task_id(index_path)
        print(f"Index {index_path} exists: taskId {task_id}")
        return True
    except KeyError:
        print(f"Index {index_path} doesn't exist.")
        return False


@register_target_task("nightly")
def target_tasks_nightly(full_task_graph, parameters, graph_config):
    """Select the set of tasks required for a nightly build."""

    def filter(task, parameters):
        return task.attributes.get("nightly", False)

    index_path = (
        f"{graph_config['trust-domain']}.v2.{parameters['project']}.branch."
        f"{parameters['head_ref']}.revision.{parameters['head_rev']}.taskgraph.decision-nightly"
    )
    if os.environ.get("MOZ_AUTOMATION") and retry(
        index_exists,
        args=(index_path,),
        kwargs={
            "reason": "to avoid triggering multiple nightlies off the same revision",
        },
    ):
        return []

    return [l for l, t in full_task_graph.tasks.items() if filter(t, parameters)]


@register_target_task("bump_android_components")
def target_tasks_bump_android_components(full_task_graph, parameters, graph_config):
    """Select the set of tasks required to update android components."""

    def filter(task, parameters):
        return task.attributes.get("bump-type", "") == "android-components"

    return [l for l, t in full_task_graph.tasks.items() if filter(t, parameters)]
