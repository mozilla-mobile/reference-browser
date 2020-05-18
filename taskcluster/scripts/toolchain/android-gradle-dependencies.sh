#!/bin/bash

# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

set -ex

. folders.sh

pushd $PROJECT_DIR

. taskcluster/scripts/toolchain/android-gradle-dependencies/before.sh

# We build everything to be sure to fetch all dependencies
./gradlew --no-daemon -PgoogleRepo='http://localhost:8081/nexus/content/repositories/google/' -PjcenterRepo='http://localhost:8081/nexus/content/repositories/jcenter/' assemble assembleAndroidTest bundle test lint ktlint detekt

. taskcluster/scripts/toolchain/android-gradle-dependencies/after.sh

popd
