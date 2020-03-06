#!/bin/bash

# This script is also called at each run of the decision task

set -ex

export ANDROID_SDK_SHA256='444e22ce8ca0f67353bda4b85175ed3731cae3ffa695ca18119cbacef1c1bea0'
export ANDROID_SDK_VERSION='3859397'
export ANDROID_SDK_ROOT='/builds/worker/android-sdk-linux'
export GRADLE_OPTS='-Xmx4096m -Dorg.gradle.daemon=false'
export SDK_ZIP_LOCATION="$HOME/sdk-tools-linux.zip"

: ${CURL:='curl --location --retry 5'}

$CURL --output "$SDK_ZIP_LOCATION" "https://dl.google.com/android/repository/sdk-tools-linux-${ANDROID_SDK_VERSION}.zip"
echo "$ANDROID_SDK_SHA256  $SDK_ZIP_LOCATION" | sha256sum --check
unzip -d "$ANDROID_SDK_ROOT" "$SDK_ZIP_LOCATION"
rm "$SDK_ZIP_LOCATION"
yes | "${ANDROID_SDK_ROOT}/tools/bin/sdkmanager" --licenses
chown -R worker:worker "$ANDROID_SDK_ROOT"
