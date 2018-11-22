/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

// Synchronized version numbers for dependencies used by (some) modules
private object Versions {
    const val kotlin = "1.3.10"
    const val coroutines = "1.0.1"

    const val androidx_appcompat = "1.0.2"
    const val androidx_constraintlayout = "1.1.3"
    const val androidx_preference = "1.0.0"

    const val google_material = "1.0.0"

    const val android_gradle_plugin = "3.2.1"

    const val mozilla_android_components = "0.33.0"

    const val thirdparty_sentry = "1.7.10"
}

// Synchronized dependencies used by (some) modules
object Deps {
    const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val kotlin_coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"

    const val mozilla_concept_engine = "org.mozilla.components:concept-engine:${Versions.mozilla_android_components}"
    const val mozilla_concept_tabstray = "org.mozilla.components:concept-tabstray:${Versions.mozilla_android_components}"
    const val mozilla_concept_toolbar = "org.mozilla.components:concept-toolbar:${Versions.mozilla_android_components}"
    const val mozilla_concept_storage = "org.mozilla.components:concept-storage:${Versions.mozilla_android_components}"
    const val mozilla_browser_awesomebar = "org.mozilla.components:browser-awesomebar:${Versions.mozilla_android_components}"
    const val mozilla_browser_engine_gecko = "org.mozilla.components:browser-engine-gecko:${Versions.mozilla_android_components}"
    const val mozilla_browser_engine_gecko_beta = "org.mozilla.components:browser-engine-gecko-beta:${Versions.mozilla_android_components}"
    const val mozilla_browser_engine_gecko_nightly = "org.mozilla.components:browser-engine-gecko-nightly:${Versions.mozilla_android_components}"
    const val mozilla_browser_domains = "org.mozilla.components:browser-domains:${Versions.mozilla_android_components}"
    const val mozilla_browser_search = "org.mozilla.components:browser-search:${Versions.mozilla_android_components}"
    const val mozilla_browser_session = "org.mozilla.components:browser-session:${Versions.mozilla_android_components}"
    const val mozilla_browser_tabstray = "org.mozilla.components:browser-tabstray:${Versions.mozilla_android_components}"
    const val mozilla_browser_toolbar = "org.mozilla.components:browser-toolbar:${Versions.mozilla_android_components}"
    const val mozilla_browser_menu = "org.mozilla.components:browser-menu:${Versions.mozilla_android_components}"
    const val mozilla_browser_errorpages = "org.mozilla.components:browser-errorpages:${Versions.mozilla_android_components}"
    const val mozilla_browser_storage_sync = "org.mozilla.components:browser-storage-sync:${Versions.mozilla_android_components}"
    const val mozilla_feature_awesomebar = "org.mozilla.components:feature-awesomebar:${Versions.mozilla_android_components}"
    const val mozilla_feature_customtabs = "org.mozilla.components:feature-customtabs:${Versions.mozilla_android_components}"
    const val mozilla_feature_intent = "org.mozilla.components:feature-intent:${Versions.mozilla_android_components}"
    const val mozilla_feature_search = "org.mozilla.components:feature-search:${Versions.mozilla_android_components}"
    const val mozilla_feature_session = "org.mozilla.components:feature-session:${Versions.mozilla_android_components}"
    const val mozilla_feature_toolbar = "org.mozilla.components:feature-toolbar:${Versions.mozilla_android_components}"
    const val mozilla_feature_tabs = "org.mozilla.components:feature-tabs:${Versions.mozilla_android_components}"
    const val mozilla_feature_downloads = "org.mozilla.components:feature-downloads:${Versions.mozilla_android_components}"
    const val mozilla_feature_storage = "org.mozilla.components:feature-storage:${Versions.mozilla_android_components}"
    const val mozilla_ui_autocomplete = "org.mozilla.components:ui-autocomplete:${Versions.mozilla_android_components}"
    const val mozilla_ui_colors = "org.mozilla.components:ui-colors:${Versions.mozilla_android_components}"
    const val mozilla_service_firefox_accounts = "org.mozilla.components:service-firefox-accounts:${Versions.mozilla_android_components}"
    const val mozilla_support_utils = "org.mozilla.components:support-utils:${Versions.mozilla_android_components}"
    const val mozilla_support_ktx= "org.mozilla.components:support-ktx:${Versions.mozilla_android_components}"
    const val mozilla_lib_crash = "org.mozilla.components:lib-crash:${Versions.mozilla_android_components}"

    const val thirdparty_sentry = "io.sentry:sentry-android:${Versions.thirdparty_sentry}"

    const val androidx_appcompat = "androidx.appcompat:appcompat:${Versions.androidx_appcompat}"
    const val androidx_constraintlayout = "androidx.constraintlayout:constraintlayout:${Versions.androidx_constraintlayout}"
    const val androidx_preference = "androidx.preference:preference-ktx:${Versions.androidx_preference}"
    const val google_material = "com.google.android.material:material:${Versions.google_material}"

    const val tools_androidgradle = "com.android.tools.build:gradle:${Versions.android_gradle_plugin}"
    const val tools_kotlingradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
}
