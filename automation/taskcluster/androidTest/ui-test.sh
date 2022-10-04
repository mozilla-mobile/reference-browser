#!/usr/bin/env bash

# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

# This script does the following:
# 1. Retrieves glcoud service account token
# 2. Activates gcloud service account
# 3. Connects to google Firebase (using TestArmada's Flank tool)
# 4. Executes UI tests
# 5. Puts test artifacts into a public build worker artifacts directory

# NOTE:
# Flank supports sharding across multiple devices at a time, but gcloud API
# only supports 1 defined APK per test run.


# If a command fails then do not proceed and fail this script too.
set -e

#########################
# The command line help #
#########################
display_help() {
    echo "Usage: $0 Build_Variant [Number_Shards...]"
    echo
    echo "Examples:"
    echo "To run UI tests on ARM device shard (1 test / shard)"
    echo "$ ui-test.sh component arm"
    echo
    echo "To run UI tests on X86 device (on 3 shards)"
    echo "$ ui-test.sh x86 3"
    echo
}

# Basic parameter check
if [[ $# -lt 1 ]]; then
    echo "Your command line contains $# arguments"
    display_help
    exit 1
fi

device_type="$1"  # arm | x86
if [[ ! -z "$2" ]]; then
    num_shards=$2
fi

JAVA_BIN="/usr/bin/java"
PATH_TEST="./automation/taskcluster/androidTest"
PATH_APK="./app/build/outputs/apk/debug"
FLANK_BIN="/builds/worker/test-tools/flank.jar"
ARTIFACT_DIR="/builds/worker/artifacts"
RESULTS_DIR="${ARTIFACT_DIR}/results"

echo
echo "ACTIVATE SERVICE ACCT"
echo
# this is where the Google Testcloud project ID is set
gcloud config set project "$GOOGLE_PROJECT" 
echo

gcloud auth activate-service-account --key-file "$GOOGLE_APPLICATION_CREDENTIALS" 
echo
echo

# From now on disable exiting on error. If the tests fail we want to continue
# and try to download the artifacts. We will exit with the actual error code later.
set +e

if [[ "${device_type,,}" == "arm64-v8a" ]]
then
    flank_template="${PATH_TEST}/flank-arm64-v8a.yml"
    APK_APP="${PATH_APK}/app-arm64-v8a-debug.apk"
elif [[ "${device_type,,}" == "x86" ]]
then
    flank_template="${PATH_TEST}/flank-x86.yml"
    APK_APP="${PATH_APK}/app-x86-debug.apk"
else
     echo "NOT FOUND"
#    exitcode=1
fi

APK_TEST="./app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"
ls -la ./app/build/outputs/apk/androidTest/debug

# keep running script on failure, so we can capture the failure and do some
# final output
set +e
# function to exit script with exit code from test run.
# (Only 0 if all test executions passed)
function failure_check() {
    if [[ $exitcode -ne 0 ]]; then
        echo
        echo
	echo "ERROR: UI test run failed, please check above URL"
    fi
    exit $exitcode
}

echo
echo "EXECUTE TEST(S)"
echo
$JAVA_BIN -jar $FLANK_BIN android run --config=$flank_template --max-test-shards=$num_shards --app=$APK_APP --test=$APK_TEST --project=$GOOGLE_PROJECT --local-result-dir="${RESULTS_DIR}"
exitcode=$?
echo
echo
failure_check
echo
echo

echo
echo "RESULTS"
echo
ls -la "${RESULTS_DIR}"
echo 
echo

echo "All UI test(s) have passed!"
echo
echo
