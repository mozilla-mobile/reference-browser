# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

from __future__ import absolute_import, print_function, unicode_literals

import json
import subprocess

from taskgraph.util.memoize import memoize


@memoize
def get_geckoview_version():
    print("Fetching geckoview version from gradle")
    geckoview_version = _run_gradle_process(
        ["printGeckoviewVersion"], prefix="geckoviewVersion: "
    )
    print('Got geckoview version: "{}"'.format(geckoview_version))
    return geckoview_version


def _run_gradle_process(gradle_command, prefix):
    output = subprocess.check_output(["./gradlew", "--quiet"] + gradle_command)
    output = _extract_content_from_command_output(output, prefix)
    return output


def _extract_content_from_command_output(output, prefix):
    prefixed_line = [line for line in output.split("\n") if line.startswith(prefix)][0]
    return json.loads(prefixed_line[len(prefix) :])
