#!/bin/bash

set -ex

function get_abs_path {
    local file_path="$1"
    echo "$( cd "$(dirname "$file_path")" >/dev/null 2>&1 ; pwd -P )"
}

CURRENT_DIR="$(get_abs_path $0)"
PROJECT_DIR="$(get_abs_path $CURRENT_DIR/../../../..)"

pushd $PROJECT_DIR
./gradlew tasks
popd

tar cf - -C "$HOME/.gradle/" --transform='s,^,gradle/,' 'wrapper/dists' \
  | xz > "$UPLOAD_DIR/gradle.tar.xz"
