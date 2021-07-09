# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
"""
Apply some defaults and minor modifications to the jobs defined in the build
kind.
"""


from taskgraph.transforms.base import TransformSequence

from ..gradle import get_build_variant


transforms = TransformSequence()


@transforms.add
def add_artifacts(config, tasks):
    for task in tasks:
        build_type = task["attributes"]["build-type"]
        variant_config = get_build_variant(build_type)
        artifacts = task.setdefault("worker", {}).setdefault("artifacts", [])
        task["attributes"]["apks"] = apks = {}
        if "apk-artifact-template" in task:
            artifact_template = task.pop("apk-artifact-template")
            for apk in variant_config["apks"]:
                apk_name = artifact_template["name"].format(**apk)
                artifacts.append({
                    "type": artifact_template["type"],
                    "name": apk_name,
                    "path": artifact_template["path"].format(
                        gradle_build_type=build_type, **apk
                    ),
                })
                apks[apk["abi"]] = apk_name

        yield task
