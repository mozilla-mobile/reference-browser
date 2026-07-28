#!/bin/bash

# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

set -ex

function get_abs_path {
    local file_path="$1"
    echo "$( cd "$(dirname "$file_path")" >/dev/null 2>&1 ; pwd -P )"
}

CURRENT_DIR="$(get_abs_path $0)"
PROJECT_DIR="$(get_abs_path $CURRENT_DIR/../../../..)"

pushd $PROJECT_DIR

. taskcluster/scripts/toolchain/android-gradle-dependencies/before.sh

GRADLE_FLAGS=(
    --no-daemon
    --no-configuration-cache
    -Pcoverage
    -PgoogleRepo='http://localhost:8081/nexus/content/repositories/google/'
    -PcentralRepo='http://localhost:8081/nexus/content/repositories/central/'
)

# Enumerates and downloads the dependencies of every resolvable configuration
# without running the requested tasks.
./gradlew "${GRADLE_FLAGS[@]}" --write-verification-metadata sha256 --dry-run detekt ktlint lint buildHealth build

# AGP resolves the aapt2 binary while its tasks run, so the pass above misses it.
./gradlew "${GRADLE_FLAGS[@]}" :app:processDebugResources

. taskcluster/scripts/toolchain/android-gradle-dependencies/after.sh

popd
