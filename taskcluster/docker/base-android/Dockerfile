# %ARG DOCKER_IMAGE_PARENT
FROM $DOCKER_IMAGE_PARENT

MAINTAINER Johan Lorenzo <jlorenzo+tc@mozilla.com>

RUN apt-get install -y openjdk-8-jdk \
    && apt-get clean

USER worker:worker

ENV ANDROID_SDK_SHA256='444e22ce8ca0f67353bda4b85175ed3731cae3ffa695ca18119cbacef1c1bea0' \
    ANDROID_SDK_VERSION='3859397' \
    ANDROID_SDK_ROOT='/builds/worker/android-sdk-linux' \
    GRADLE_OPTS='-Xmx4096m -Dorg.gradle.daemon=false' \
    SDK_ZIP_LOCATION="$HOME/sdk-tools-linux.zip"

RUN $CURL --output "$SDK_ZIP_LOCATION" "https://dl.google.com/android/repository/sdk-tools-linux-${ANDROID_SDK_VERSION}.zip" \
    && echo "$ANDROID_SDK_SHA256  $SDK_ZIP_LOCATION" | sha256sum --check \
    && unzip -d "$ANDROID_SDK_ROOT" "$SDK_ZIP_LOCATION" \
    && rm "$SDK_ZIP_LOCATION" \
    && yes | "${ANDROID_SDK_ROOT}/tools/bin/sdkmanager" --licenses \
    && chown -R worker:worker "$ANDROID_SDK_ROOT"

# run-task expects to run as root
USER root
