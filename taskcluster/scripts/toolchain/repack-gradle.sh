#!/bin/bash

set -ex

. "$(dirname $0)/directories.sh"

pushd $PROJECT_DIR
# No need to build anything to fetch gradle.
./gradlew tasks
popd

tar cf - -C "$HOME/.gradle/" --transform='s,^,gradle/,' '.' \
  | xz > "$UPLOAD_DIR/gradle.tar.xz"
