#!/bin/bash

set -ex

function get_abs_path {
    local file_path="$1"
    echo "$( cd "$(dirname "$file_path")" >/dev/null 2>&1 ; pwd -P )"
}

CURRENT_DIR="$(get_abs_path $0)"
PROJECT_DIR="$(get_abs_path $CURRENT_DIR/../../../..)"

pushd $PROJECT_DIR

# We build everything to be sure to fetch all dependencies
./gradlew --no-daemon assemble assembleAndroidTest bundle test lint ktlint detekt

# We only cache dependencies outside of the ones hosted on maven.mozilla.org.
# caches/modules-2/files-2.1/ is where the pom, jar, and aar files are.
tar cf - -C "$HOME/.gradle/" --exclude='*mozilla*' --transform='s,^,android-gradle-dependencies/,' 'caches/modules-2/files-2.1/' \
  | xz > "$HOME/artifacts/android-gradle-dependencies.tar.xz"

popd
