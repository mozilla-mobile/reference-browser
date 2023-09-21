# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.


# /!\ This implementation differs from what android-components, fenix, and focus have.
# The main reason is: a-c is much more complex and subject to changes. The 3 projects
# being in the same repo, they all follow the same model. This means, there's a
# mechanism to ensure the content of `gradlew printVariants` stays in sync with the
# .buildconfig.yml files.
#
# Here, we're just tracking a handful build_types, so we don't need the same complexity
# as what was introduced in https://github.com/mozilla-mobile/firefox-android/pull/340.
#
# If you want to update this variable, just run `gradlew printVariants` and copy-paste
# the result.
_ALL_VARIANTS = [
    {
        "apks": [
            {"abi": "arm64-v8a", "fileName": "app-arm64-v8a-debug.apk"},
            {"abi": "armeabi-v7a", "fileName": "app-armeabi-v7a-debug.apk"},
            {"abi": "x86", "fileName": "app-x86-debug.apk"},
            {"abi": "x86_64", "fileName": "app-x86_64-debug.apk"},
        ],
        "build_type": "debug",
        "name": "debug",
    }, {
        "apks": [
            {"abi": "arm64-v8a", "fileName": "app-arm64-v8a-raptor-unsigned.apk"},
            {"abi": "armeabi-v7a", "fileName": "app-armeabi-v7a-raptor-unsigned.apk"},
            {"abi": "x86", "fileName": "app-x86-raptor-unsigned.apk"},
            {"abi": "x86_64", "fileName": "app-x86_64-raptor-unsigned.apk"},
        ],
        "build_type": "raptor",
        "name": "raptor",
    }, {
        "apks": [
            {"abi": "arm64-v8a", "fileName": "app-arm64-v8a-nightly-unsigned.apk"},
            {"abi": "armeabi-v7a", "fileName": "app-armeabi-v7a-nightly-unsigned.apk"},
            {"abi": "x86", "fileName": "app-x86-nightly-unsigned.apk"},
            {"abi": "x86_64", "fileName": "app-x86_64-nightly-unsigned.apk"},
        ],
        "build_type": "nightly",
        "name": "nightly",
    }
]


def get_build_variant(build_type):
    matching_variants = [
        variant for variant in _ALL_VARIANTS
        if variant["build_type"] == build_type
    ]
    number_of_matching_variants = len(matching_variants)
    if number_of_matching_variants == 0:
        raise ValueError(f'No variant found for build type "{build_type}"')
    elif number_of_matching_variants > 1:
        raise ValueError('Too many variants found for build type "{}": {}'.format(
            build_type, matching_variants
        ))

    return matching_variants.pop()
