#!/bin/bash

set -ex

PROJECT_DIR="$(cd "$(dirname "$0")/../../.." && pwd -P)"
CONFIG_KT="$PROJECT_DIR/buildSrc/src/main/java/Config.kt"

kotlin_const() {
    sed -n "s/^ *const val $1 = \"\{0,1\}\([^\"]*\)\"\{0,1\}\$/\1/p" "$CONFIG_KT"
}

BUILD_TOOLS_VERSION="$(kotlin_const buildToolsVersion)"
COMPILE_SDK_VERSION="$(kotlin_const compileSdkMajorVersion).$(kotlin_const compileSdkMinorVersion)"
: "${BUILD_TOOLS_VERSION:?failed to parse buildToolsVersion from Config.kt}"
[[ "$COMPILE_SDK_VERSION" =~ ^[0-9]+\.[0-9]+$ ]] || { echo "failed to parse compileSdk from Config.kt: $COMPILE_SDK_VERSION" >&2; exit 1; }

export ANDROID_SDK_ROOT=$MOZ_FETCHES_DIR

JAVA17PATH="/usr/lib/jvm/java-17-openjdk-amd64/bin/:$PATH"

PATH=$JAVA17PATH "${ANDROID_SDK_ROOT}/cmdline-tools/bin/android" --no-metrics --sdk "${ANDROID_SDK_ROOT}" \
    sdk install "platforms;android-${COMPILE_SDK_VERSION}" "build-tools;${BUILD_TOOLS_VERSION}"

# It's nice to have the build logs include the state of the world upon completion.
PATH=$JAVA17PATH "${ANDROID_SDK_ROOT}/cmdline-tools/bin/android" --no-metrics --sdk "${ANDROID_SDK_ROOT}" sdk list

tar cf - -C "$ANDROID_SDK_ROOT" . --transform 's,^\./,android-sdk-linux/,' | xz > "$UPLOAD_DIR/android-sdk-linux.tar.xz"
