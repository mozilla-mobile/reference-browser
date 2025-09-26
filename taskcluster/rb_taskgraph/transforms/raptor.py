# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
"""
Apply some defaults and minor modifications to the jobs defined in the build
kind.
"""


import copy
import json

from taskgraph.transforms.base import TransformSequence
from taskgraph.util.treeherder import inherit_treeherder_from_dep
from taskgraph.util.schema import resolve_keyed_by

transforms = TransformSequence()


@transforms.add
def add_variants(config, tasks):
    only_types = config.config["only-for-build-types"]
    only_abis = config.config["only-for-abis"]

    tests = list(tasks)

    for dep_task in config.kind_dependencies_tasks.values():
        build_type = dep_task.attributes.get("build-type", '')
        if build_type not in only_types:
            continue

        for abi, apk in dep_task.attributes["apks"].items():
            if abi not in only_abis:
                continue
            for test in tests:
                test = copy.deepcopy(test)
                attributes = copy.deepcopy(dep_task.attributes)
                attributes.update(test.get("attributes", {}))
                attributes["abi"] = abi
                attributes["apk"] = apk
                test["attributes"] = attributes
                test["primary-dependency"] = dep_task
                yield test

@transforms.add
def fill_email_data(config, tasks):
    product_name = config.graph_config['taskgraph']['repositories']['mobile']['name']
    format_kwargs = {
        "product_name": product_name.lower(),
        "head_rev": config.params["head_rev"],
    }

    for task in tasks:
        format_kwargs["task_name"] = task["name"]

        resolve_keyed_by(task, 'notify', item_name=task["name"], level=config.params["level"])
        email = task["notify"].get("email")
        if email:
            email["link"]["href"] = email["link"]["href"].format(**format_kwargs)
            email["subject"] = email["subject"].format(**format_kwargs)

        yield task
