/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

// If you ever need to force a toolchain rebuild (taskcluster) then edit the following comment.
// FORCE REBUILD 2021-04-12

// Synchronized version numbers for dependencies used by (some) modules
private object Versions {
    const val kotlin = "1.5.10"
    const val coroutines = "1.5.0"

    const val androidx_appcompat = "1.3.0-rc01"
    const val androidx_constraintlayout = "1.1.3"
    const val androidx_preference = "1.0.0"

    const val workmanager = "2.7.1"
    const val google_material = "1.0.0"

    const val android_gradle_plugin = "7.0.0"

    const val mozilla_android_components = AndroidComponents.VERSION

    const val thirdparty_sentry = "5.6.2"

    const val espresso_core = "3.1.0"
    const val espresso_version = "3.4.0"
    const val mockwebserver = "4.9.0"
    const val orchestrator = "1.4.1"
    const val tools_test_rules = "1.1.0"
    const val tools_test_runner = "1.4.0"
    const val uiautomator = "2.2.0"
    const val junit_ktx = "1.1.3"

    const val compose_version = "1.0.0-rc02"

    object AndroidX {
        const val activity_compose = "1.3.0-rc02"
        const val core = "1.1.0"
        const val compose = compose_version
        const val lifecycle = "2.2.0"
        const val swiperefreshlayout = "1.1.0"
    }
}

// Synchronized dependencies used by (some) modules
object Deps {
    const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    const val kotlin_coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"

    const val mozilla_concept_awesomebar = "org.mozilla.components:concept-awesomebar:${Versions.mozilla_android_components}"
    const val mozilla_concept_engine = "org.mozilla.components:concept-engine:${Versions.mozilla_android_components}"
    const val mozilla_concept_menu = "org.mozilla.components:concept-menu:${Versions.mozilla_android_components}"
    const val mozilla_concept_tabstray = "org.mozilla.components:concept-tabstray:${Versions.mozilla_android_components}"
    const val mozilla_concept_toolbar = "org.mozilla.components:concept-toolbar:${Versions.mozilla_android_components}"
    const val mozilla_concept_storage = "org.mozilla.components:concept-storage:${Versions.mozilla_android_components}"
    const val mozilla_concept_sync = "org.mozilla.components:concept-sync:${Versions.mozilla_android_components}"
    const val mozilla_concept_push = "org.mozilla.components:concept-push:${Versions.mozilla_android_components}"

    const val mozilla_compose_awesomebar = "org.mozilla.components:compose-awesomebar:${Versions.mozilla_android_components}"

    const val mozilla_browser_engine_gecko = "org.mozilla.components:browser-engine-gecko:${Versions.mozilla_android_components}"
    const val mozilla_browser_domains = "org.mozilla.components:browser-domains:${Versions.mozilla_android_components}"
    const val mozilla_browser_session_storage = "org.mozilla.components:browser-session-storage:${Versions.mozilla_android_components}"
    const val mozilla_browser_state = "org.mozilla.components:browser-state:${Versions.mozilla_android_components}"
    const val mozilla_browser_tabstray = "org.mozilla.components:browser-tabstray:${Versions.mozilla_android_components}"
    const val mozilla_browser_toolbar = "org.mozilla.components:browser-toolbar:${Versions.mozilla_android_components}"
    const val mozilla_browser_menu = "org.mozilla.components:browser-menu:${Versions.mozilla_android_components}"
    const val mozilla_browser_menu2 = "org.mozilla.components:browser-menu2:${Versions.mozilla_android_components}"
    const val mozilla_browser_errorpages = "org.mozilla.components:browser-errorpages:${Versions.mozilla_android_components}"
    const val mozilla_browser_storage_sync = "org.mozilla.components:browser-storage-sync:${Versions.mozilla_android_components}"
    const val mozilla_browser_icons = "org.mozilla.components:browser-icons:${Versions.mozilla_android_components}"
    const val mozilla_browser_thumbnails = "org.mozilla.components:browser-thumbnails:${Versions.mozilla_android_components}"

    const val mozilla_feature_accounts = "org.mozilla.components:feature-accounts:${Versions.mozilla_android_components}"
    const val mozilla_feature_accounts_push = "org.mozilla.components:feature-accounts-push:${Versions.mozilla_android_components}"
    const val mozilla_feature_addons = "org.mozilla.components:feature-addons:${Versions.mozilla_android_components}"
    const val mozilla_feature_app_links = "org.mozilla.components:feature-app-links:${Versions.mozilla_android_components}"
    const val mozilla_feature_autofill = "org.mozilla.components:feature-autofill:${Versions.mozilla_android_components}"
    const val mozilla_feature_awesomebar = "org.mozilla.components:feature-awesomebar:${Versions.mozilla_android_components}"
    const val mozilla_feature_contextmenu = "org.mozilla.components:feature-contextmenu:${Versions.mozilla_android_components}"
    const val mozilla_feature_customtabs = "org.mozilla.components:feature-customtabs:${Versions.mozilla_android_components}"
    const val mozilla_feature_findinpage = "org.mozilla.components:feature-findinpage:${Versions.mozilla_android_components}"
    const val mozilla_feature_media = "org.mozilla.components:feature-media:${Versions.mozilla_android_components}"
    const val mozilla_feature_sitepermissions = "org.mozilla.components:feature-sitepermissions:${Versions.mozilla_android_components}"
    const val mozilla_feature_intent = "org.mozilla.components:feature-intent:${Versions.mozilla_android_components}"
    const val mozilla_feature_search = "org.mozilla.components:feature-search:${Versions.mozilla_android_components}"
    const val mozilla_feature_session = "org.mozilla.components:feature-session:${Versions.mozilla_android_components}"
    const val mozilla_feature_toolbar = "org.mozilla.components:feature-toolbar:${Versions.mozilla_android_components}"
    const val mozilla_feature_tabs = "org.mozilla.components:feature-tabs:${Versions.mozilla_android_components}"
    const val mozilla_feature_downloads = "org.mozilla.components:feature-downloads:${Versions.mozilla_android_components}"
    const val mozilla_feature_storage = "org.mozilla.components:feature-storage:${Versions.mozilla_android_components}"
    const val mozilla_feature_prompts = "org.mozilla.components:feature-prompts:${Versions.mozilla_android_components}"
    const val mozilla_feature_push = "org.mozilla.components:feature-push:${Versions.mozilla_android_components}"
    const val mozilla_feature_pwa = "org.mozilla.components:feature-pwa:${Versions.mozilla_android_components}"
    const val mozilla_feature_qr = "org.mozilla.components:feature-qr:${Versions.mozilla_android_components}"
    const val mozilla_feature_readerview = "org.mozilla.components:feature-readerview:${Versions.mozilla_android_components}"
    const val mozilla_feature_syncedtabs = "org.mozilla.components:feature-syncedtabs:${Versions.mozilla_android_components}"
    const val mozilla_feature_webauthn = "org.mozilla.components:feature-webauthn:${Versions.mozilla_android_components}"
    const val mozilla_feature_webcompat = "org.mozilla.components:feature-webcompat:${Versions.mozilla_android_components}"
    const val mozilla_feature_webnotifications = "org.mozilla.components:feature-webnotifications:${Versions.mozilla_android_components}"

    const val mozilla_ui_autocomplete = "org.mozilla.components:ui-autocomplete:${Versions.mozilla_android_components}"
    const val mozilla_ui_colors = "org.mozilla.components:ui-colors:${Versions.mozilla_android_components}"
    const val mozilla_ui_icons = "org.mozilla.components:ui-icons:${Versions.mozilla_android_components}"
    const val mozilla_ui_tabcounter = "org.mozilla.components:ui-tabcounter:${Versions.mozilla_android_components}"

    const val mozilla_service_firefox_accounts = "org.mozilla.components:service-firefox-accounts:${Versions.mozilla_android_components}"
    const val mozilla_service_location = "org.mozilla.components:service-location:${Versions.mozilla_android_components}"
    const val mozilla_service_sync_logins = "org.mozilla.components:service-sync-logins:${Versions.mozilla_android_components}"

    const val mozilla_support_images = "org.mozilla.components:support-images:${Versions.mozilla_android_components}"
    const val mozilla_support_utils = "org.mozilla.components:support-utils:${Versions.mozilla_android_components}"
    const val mozilla_support_ktx= "org.mozilla.components:support-ktx:${Versions.mozilla_android_components}"
    const val mozilla_support_rustlog = "org.mozilla.components:support-rustlog:${Versions.mozilla_android_components}"
    const val mozilla_support_rusthttp = "org.mozilla.components:support-rusthttp:${Versions.mozilla_android_components}"
    const val mozilla_support_webextensions = "org.mozilla.components:support-webextensions:${Versions.mozilla_android_components}"

    const val mozilla_lib_crash = "org.mozilla.components:lib-crash:${Versions.mozilla_android_components}"
    const val mozilla_lib_crash_sentry = "org.mozilla.components:lib-crash-sentry:${Versions.mozilla_android_components}"
    const val mozilla_lib_push_firebase = "org.mozilla.components:lib-push-firebase:${Versions.mozilla_android_components}"
    const val mozilla_lib_dataprotect = "org.mozilla.components:lib-dataprotect:${Versions.mozilla_android_components}"
    const val mozilla_lib_publicsuffixlist = "org.mozilla.components:lib-publicsuffixlist:${Versions.mozilla_android_components}"

    const val thirdparty_sentry = "io.sentry:sentry-android:${Versions.thirdparty_sentry}"

    const val androidx_appcompat = "androidx.appcompat:appcompat:${Versions.androidx_appcompat}"
    const val androidx_core_ktx = "androidx.core:core-ktx:${Versions.AndroidX.core}"
    const val androidx_constraintlayout = "androidx.constraintlayout:constraintlayout:${Versions.androidx_constraintlayout}"
    const val androidx_lifecycle_process = "androidx.lifecycle:lifecycle-process:${Versions.AndroidX.lifecycle}"
    const val androidx_preference_ktx = "androidx.preference:preference-ktx:${Versions.androidx_preference}"
    const val androidx_work_runtime_ktx = "androidx.work:work-runtime-ktx:${Versions.workmanager}"
    const val androidx_compose_ui = "androidx.compose.ui:ui:${Versions.AndroidX.compose}"
    const val androidx_compose_ui_tooling = "androidx.compose.ui:ui-tooling:${Versions.AndroidX.compose}"
    const val androidx_compose_foundation = "androidx.compose.foundation:foundation:${Versions.AndroidX.compose}"
    const val androidx_compose_material = "androidx.compose.material:material:${Versions.AndroidX.compose}"
    const val androidx_activity_compose = "androidx.activity:activity-compose:${Versions.AndroidX.activity_compose}"
    const val androidx_swiperefreshlayout = "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.AndroidX.swiperefreshlayout}"

    const val google_material = "com.google.android.material:material:${Versions.google_material}"

    const val tools_androidgradle = "com.android.tools.build:gradle:${Versions.android_gradle_plugin}"
    const val tools_kotlingradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"

    const val espresso_contrib = "androidx.test.espresso:espresso-contrib:${Versions.espresso_version}"
    const val espresso_core = "androidx.test.espresso:espresso-core:${Versions.espresso_core}"
    const val espresso_idling_resources = "androidx.test.espresso:espresso-idling-resource:${Versions.espresso_version}"
    const val espresso_web = "androidx.test.espresso:espresso-web:${Versions.espresso_version}"
    const val mockwebserver = "com.squareup.okhttp3:mockwebserver:${Versions.mockwebserver}"
    const val orchestrator =  "androidx.test:orchestrator:${Versions.orchestrator}"
    const val tools_test_rules = "androidx.test:rules:${Versions.tools_test_rules}"
    const val tools_test_runner = "androidx.test:runner:${Versions.tools_test_runner}"
    const val uiautomator = "androidx.test.uiautomator:uiautomator:${Versions.uiautomator}"
    const val junit_ktx = "androidx.test.ext:junit-ktx:${Versions.junit_ktx}"
}
