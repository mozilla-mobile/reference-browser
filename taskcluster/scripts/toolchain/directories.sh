#!/bin/bash

set -ex

function get_absolute_path {
    local file_path="$1"
    echo "$( cd "$(dirname "$file_path")" >/dev/null 2>&1 ; pwd -P )"
}

export CURRENT_DIR="$(get_absolute_path $0)"
export PROJECT_DIR="$(get_absolute_path $CURRENT_DIR/../../../..)"
