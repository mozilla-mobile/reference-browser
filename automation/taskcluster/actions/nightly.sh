# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

########################################################################
# Build nightly, sign it with a throw-away key and attach it as artifact
# to the taskcluster task.
########################################################################

# If a command fails then do not proceed and fail this script too.
set -ex

# Fetch sentry token for crash reporting
python automation/taskcluster/helper/get-secret.py -s project/mobile/reference-browser/sentry -k dsn -f .sentry_token

# First build and test everything
./gradlew --no-daemon -PcrashReportEnabled=true clean assembleRelease test

# Fetch preview/throw-away key from secrets service
python automation/taskcluster/helper/get-secret.py -s project/mobile/reference-browser/preview-key-store -k keyStoreFile -f .store --decode
python automation/taskcluster/helper/get-secret.py -s project/mobile/reference-browser/preview-key-store -k keyStorePassword -f .store_token
python automation/taskcluster/helper/get-secret.py -s project/mobile/reference-browser/preview-key-store -k keyPassword -f .key_token

# Sign APKs with preview/throw-away key
python automation/taskcluster/helper/sign-builds.py --zipalign --path ./app/build/outputs/apk --store .store --store-token .store_token --key-alias preview-key --key-token .key_token --archive ./preview

# Copy release APKs to separate folder for attaching as artifacts
mkdir release
cp preview/app-geckoNightly-arm-armeabi-v7a-release-signed-aligned.apk release/reference-browser-arm.apk
cp preview/app-geckoNightly-x86-x86-release-signed-aligned.apk release/reference-browser-x86.apk
