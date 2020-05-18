function get_abs_path {
    local file_path="$1"
    echo "$( cd "$(dirname "$file_path")" >/dev/null 2>&1 ; pwd -P )"
}

export CURRENT_DIR="$(get_abs_path $0)"
export PROJECT_DIR="$(get_abs_path $CURRENT_DIR/../../../..)"
