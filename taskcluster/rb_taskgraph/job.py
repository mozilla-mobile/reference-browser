# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

from __future__ import absolute_import, print_function, unicode_literals

from taskgraph.transforms.job import run_job_using, configure_taskdesc_for_run
from taskgraph.util import path
from taskgraph.util.schema import Schema
from voluptuous import Required, Optional
from six import text_type

from pipes import quote as shell_quote

gradlew_schema = Schema(
    {
        Required("using"): "gradlew",
        Required("gradlew"): [text_type],
        # Base work directory used to set up the task.
        Required("workdir"): text_type,
        Optional("use-caches"): bool,
        Optional("secrets"): [
            {
                Required("name"): text_type,
                Required("path"): text_type,
                Required("key"): text_type,
            }
        ],
    }
)


@run_job_using("docker-worker", "gradlew", schema=gradlew_schema)
def configure_gradlew(config, job, taskdesc):
    run = job["run"]
    worker = taskdesc["worker"] = job["worker"]

    command_prefix = ["./gradlew"]

    gradlew = run.pop("gradlew")
    command = command_prefix + gradlew

    worker.setdefault("env", {}).update(
        {"ANDROID_SDK_ROOT": path.join(run["workdir"], "android-sdk-linux")}
    )

    # defer to the run_task implementation
    run["command"] = "taskcluster/scripts/install-sdk.sh"
    secrets = run.pop("secrets", [])
    if secrets:
        scopes = taskdesc.setdefault("scopes", [])
        for secret in secrets:
            run[
                "command"
            ] += " && taskcluster/scripts/get-secret.py -s {name} -k {key} -f {path}".format(
                **secret
            )
            scopes.append("secrets:get:{}".format(secret["name"]))

    run["command"] += " && {}".format(" ".join(map(shell_quote, command)))
    run["cwd"] = "{checkout}"
    run["using"] = "run-task"
    configure_taskdesc_for_run(config, job, taskdesc, job["worker"]["implementation"])
