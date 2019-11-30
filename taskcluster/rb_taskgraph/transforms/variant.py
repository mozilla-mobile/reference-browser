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

@transforms.add
def add_shippable_secrets(config, tasks):
    for task in tasks:
        secrets = task["run"].setdefault("secrets", [])

        if task.pop("include-shippable-secrets", False) and config.params["level"] == "3":
            build_type = task["attributes"]["build-type"]
            secret_index = 'project/mobile/reference-browser/{}'.format(build_type)
            secrets.extend([{
                "key": key,
                "name": secret_index,
                "path": target_file,
            } for key, target_file in (
                ('sentry_dsn', '.sentry_token'),
                ('firebase', 'app/src/main/res/values/firebase.xml'),
            )])
        else:
            task["run"]["pre-gradlew"] = [[
                "echo", '"{}"'.format(fake_value), ">", target_file
            ] for fake_value, target_file in (
                ("https://fake@sentry.prod.mozaws.net/368", ".sentry_token"),
                # We don't need a fake key for Firebase.
            )]

        yield task
