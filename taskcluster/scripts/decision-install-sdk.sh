#!/bin/bash

set -ex

export CURL='curl --location --retry 5'

ANDROID_SDK_VERSION='9477386'
ANDROID_SDK_SHA256='bd1aa17c7ef10066949c88dc6c9c8d536be27f992a1f3b5a584f9bd2ba5646a0'
SDK_ZIP_LOCATION="$HOME/sdk-tools-linux.zip"
JAVA17PATH="/usr/lib/jvm/java-17-openjdk-amd64/bin/:$PATH"

$CURL --output "$SDK_ZIP_LOCATION" "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_SDK_VERSION}_latest.zip"
echo "$ANDROID_SDK_SHA256  $SDK_ZIP_LOCATION" | sha256sum --check
unzip -d "$ANDROID_SDK_ROOT" "$SDK_ZIP_LOCATION"
rm "$SDK_ZIP_LOCATION"

yes | PATH=$JAVA17PATH "${ANDROID_SDK_ROOT}/cmdline-tools/bin/sdkmanager" --licenses --sdk_root="${ANDROID_SDK_ROOT}"
