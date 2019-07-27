# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
"""
Apply some defaults and minor modifications to the jobs defined in the build
kind.
"""

from __future__ import absolute_import, print_function, unicode_literals

from taskgraph.transforms.base import TransformSequence

from .gradle import get_build_variant


transforms = TransformSequence()


@transforms.add
def add_variant_config(config, tasks):
    for task in tasks:
        variant = task["name"]
        task.setdefault("attributes", {}).update({"build-type": variant})
        task["treeherder"]["platform"] = "android/{}".format(variant)

        variant_config = get_build_variant(variant)
        artifacts = task.setdefault("worker", {}).setdefault("artifacts", [])
        task["attributes"]["apks"] = apks = {}
        if "apk-artifact-template" in task:
            artifact_template = task.pop("apk-artifact-template")
            for apk in variant_config["apks"]:
                apk_name = artifact_template["name"].format(variant=variant, **apk)
                artifacts.append(
                    {
                        "type": artifact_template["type"],
                        "name": apk_name,
                        "path": artifact_template["path"].format(
                            variant=variant, **apk
                        ),
                    }
                )
                apks[apk["abi"]] = apk_name

        yield task
