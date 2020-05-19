#!/bin/bash

set -ex

function get_abs_path {
    local file_path="$1"
    echo "$( cd "$(dirname "$file_path")" >/dev/null 2>&1 ; pwd -P )"
}

CURRENT_DIR="$(get_abs_path $0)"
PROJECT_DIR="$(get_abs_path $CURRENT_DIR/../../../..)"


export ANDROID_SDK_ROOT=$MOZ_FETCHES_DIR
yes | "${ANDROID_SDK_ROOT}/tools/bin/sdkmanager" --licenses
# It's nice to have the build logs include the state of the world upon completion.
"${ANDROID_SDK_ROOT}/tools/bin/sdkmanager" --list

pushd $PROJECT_DIR
./gradlew --debug assemble
popd

tar cf - -C "$ANDROID_SDK_ROOT" . --transform 's,^\./,android-sdk-linux/,' | xz > "$UPLOAD_DIR/android-sdk-linux.tar.xz"
