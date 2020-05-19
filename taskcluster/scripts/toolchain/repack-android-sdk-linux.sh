#!/bin/bash

set -ex

. "$(dirname $0)/directories.sh"

export ANDROID_SDK_ROOT="$MOZ_FETCHES_DIR"
yes | "${ANDROID_SDK_ROOT}/tools/bin/sdkmanager" --licenses

pushd $PROJECT_DIR
# XXX The Android SDK is fully populated by gradle if it compiles something.
./gradlew assembleDebug
popd

# It's nice to have the build logs include the state of the world upon completion.
"${ANDROID_SDK_ROOT}/tools/bin/sdkmanager" --list

tar cf - -C "$ANDROID_SDK_ROOT" --transform 's,^\./,android-sdk-linux/,' '.' \
  | xz > "$UPLOAD_DIR/android-sdk-linux.tar.xz"
