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
    # Overrides the daemon=false the base image puts in GRADLE_OPTS.
    --daemon
    --no-configuration-cache
    -Pcoverage
    -PcentralRepo='http://localhost:8081/nexus/content/repositories/central/'
    -PgoogleRepo='http://localhost:8081/nexus/content/repositories/google/'
    -PpluginRepo='http://localhost:8081/nexus/content/repositories/gradle-plugins/'
)

# Enumerates and downloads the dependencies of every resolvable configuration
# without running the requested tasks.
./gradlew "${GRADLE_FLAGS[@]}" --write-verification-metadata sha256 --dry-run detekt ktlint ktfmtCheck lint buildHealth build

# AGP resolves the aapt2 binary while its tasks run, so the pass above misses it.
./gradlew "${GRADLE_FLAGS[@]}" :app:processDebugResources

# Spotless resolves ktfmt the same way, so run the formatter for real. spotlessKotlin
# rather than ktfmtCheck: it resolves the same classpath but reports no violations, so a
# misformatted tree cannot fail the toolchain every other task depends on.
./gradlew "${GRADLE_FLAGS[@]}" spotlessKotlin

# Don't leave a 4GB heap sitting there while `after.sh` packages everything up.
./gradlew --stop

. taskcluster/scripts/toolchain/android-gradle-dependencies/after.sh

popd
