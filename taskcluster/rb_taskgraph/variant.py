# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
"""
Apply some defaults and minor modifications to the jobs defined in the build
kind.
"""

from __future__ import absolute_import, print_function, unicode_literals

import datetime

from taskgraph.transforms.base import TransformSequence


transforms = TransformSequence()


@transforms.add
def add_variant_config(config, tasks):
    for task in tasks:
        attributes = task.setdefault("attributes", {})
        variant = attributes["build-type"] if attributes.get("build-type") else task["name"]
        attributes["build-type"] = variant
        task["treeherder"]["platform"] = "android/{}".format(variant)
        yield task


@transforms.add
def add_nightly_version(config, tasks):
    formatted_date = datetime.datetime.now().strftime("%y%V")
    version_name = "1.0.{}".format(formatted_date)

    for task in tasks:
        if task.pop("include-nightly-version", False):
            task["run"]["gradlew"].append(
                "-PversionName={}".format(version_name)
            )
        yield task
