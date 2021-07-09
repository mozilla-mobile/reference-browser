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
def add_artifacts(config, tasks):
    for task in tasks:
        variant = task["attributes"]["build-type"]
        artifacts = task.setdefault("worker", {}).setdefault("artifacts", [])
        if "aab-artifact-template" in task:
            artifact_template = task.pop("aab-artifact-template")
            artifacts.append({
                "type": artifact_template["type"],
                "name": artifact_template["name"],
                "path": artifact_template["path"].format(variant=variant),
            })
            task["attributes"]["aab"] = artifact_template["name"]

        yield task
