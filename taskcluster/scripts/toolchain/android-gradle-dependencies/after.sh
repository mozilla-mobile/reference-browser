#!/bin/bash

# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

# This is copy of
# https://searchfox.org/mozilla-central/rev/2cd2d511c0d94a34fb7fa3b746f54170ee759e35/taskcluster/scripts/misc/android-gradle-dependencies/after.sh.
# Later changes to that file have been picked up piecemeal.

set -x -e

echo "running as" $(id)

: WORKSPACE ${WORKSPACE:=/builds/worker/workspace}

set -v

# Resolved before the `pushd` below leaves the project directory.
DEPENDENCY_INVENTORY="$PWD/gradle/verification-metadata.dryrun.xml"
VERIFY_DEPENDENCIES="$PWD/taskcluster/scripts/toolchain/android-gradle-dependencies/verify_dependencies.py"

# Package everything up.
pushd $WORKSPACE
mkdir -p android-gradle-dependencies /builds/worker/artifacts

cp -R ${NEXUS_WORK}/storage/central android-gradle-dependencies
cp -R ${NEXUS_WORK}/storage/google android-gradle-dependencies
cp -R ${NEXUS_WORK}/storage/gradle-plugins android-gradle-dependencies || {
    echo "FATAL ERROR: no gradle-plugins storage. Did plugin resolution reach the proxy?"
    exit 1
}

# Bug 1953671: catch intermittently incomplete artifacts here rather than
# downstream.
# The Mozilla repositories in build.gradle are not proxied, so what they serve
# is legitimately absent from the packaged tree.
python3 "$VERIFY_DEPENDENCIES" --inventory "$DEPENDENCY_INVENTORY" \
    --unproxied org/mozilla \
    android-gradle-dependencies/central android-gradle-dependencies/google \
    android-gradle-dependencies/gradle-plugins

tar cavf /builds/worker/artifacts/android-gradle-dependencies.tar.zst android-gradle-dependencies

popd
