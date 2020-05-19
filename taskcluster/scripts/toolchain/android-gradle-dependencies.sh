#!/bin/bash

# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

set -ex

. "$(dirname $0)/directories.sh"

pushd $PROJECT_DIR
. taskcluster/scripts/toolchain/android-gradle-dependencies/before.sh
NEXUS_PREFIX='http://localhost:8081/nexus/content/repositories'
GRADLE_ARGS="--parallel -PgoogleRepo=$NEXUS_PREFIX/google/ -PjcenterRepo=$NEXUS_PREFIX/jcenter/"
# We build everything to be sure to fetch all dependencies
./gradlew $GRADLE_ARGS assemble assembleAndroidTest bundle test lint ktlint detekt
. taskcluster/scripts/toolchain/android-gradle-dependencies/after.sh
popd
