/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

// If you ever need to force a toolchain rebuild (taskcluster) then edit the following comment.
// FORCE REBUILD 2021-04-12

// Synchronized version numbers for dependencies used by (some) modules
object Versions {
    object AndroidX {
        const val activity_compose = "1.7.1"
        const val appcompat = "1.6.1"
        const val compose = "1.4.3"
        const val constraintlayout = "2.1.4"
        const val core = "1.10.0"
        const val lifecycle = "2.6.1"
        const val preference = "1.2.0"
        const val swiperefreshlayout = "1.1.0"
        const val work = "2.8.1"
    }

    object Google {
        const val compose_compiler = "1.4.7"
        const val material = "1.9.0"
    }

    object Gradle {
        const val android_plugin = "7.4.2"
        const val kotlin_plugin = Kotlin.compiler
    }

    object Kotlin {
        const val compiler = "1.8.21"
        const val coroutines = "1.7.1"
    }

    object Testing {
        const val androidx_core = "1.5.0"
        const val androidx_espresso = "3.5.1"
        const val androidx_ext_junit = "1.1.5"
        const val androidx_orchestrator = "1.4.2"
        const val androidx_runner = "1.5.2"
        const val androidx_uiautomator = "2.2.0"
        const val detekt = "1.23.0"
        const val jacoco = "0.8.10"
        const val ktlint = "0.48.2"
        const val mockwebserver = "4.11.0"
    }

    object ThirdParty {
        const val sentry = "6.21.0"
    }

    // Workaround for a Gradle parsing bug that prevents using nested objects directly in Gradle files.
    // These might be removable if we switch to kts files instead.
    // https://github.com/gradle/gradle/issues/9251
    const val detekt_version = Versions.Testing.detekt
    const val google_compose_compiler = Versions.Google.compose_compiler
    const val jacoco_version = Versions.Testing.jacoco
    const val ktlint_version = Versions.Testing.ktlint
}

// Synchronized dependencies used by (some) modules
object Deps {
    const val kotlin_coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.Kotlin.coroutines}"

    const val mozilla_concept_awesomebar = "org.mozilla.components:concept-awesomebar:${AndroidComponents.VERSION}"
    const val mozilla_concept_engine = "org.mozilla.components:concept-engine:${AndroidComponents.VERSION}"
    const val mozilla_concept_menu = "org.mozilla.components:concept-menu:${AndroidComponents.VERSION}"
    const val mozilla_concept_tabstray = "org.mozilla.components:concept-tabstray:${AndroidComponents.VERSION}"
    const val mozilla_concept_toolbar = "org.mozilla.components:concept-toolbar:${AndroidComponents.VERSION}"
    const val mozilla_concept_storage = "org.mozilla.components:concept-storage:${AndroidComponents.VERSION}"
    const val mozilla_concept_sync = "org.mozilla.components:concept-sync:${AndroidComponents.VERSION}"
    const val mozilla_concept_push = "org.mozilla.components:concept-push:${AndroidComponents.VERSION}"

    const val mozilla_compose_awesomebar = "org.mozilla.components:compose-awesomebar:${AndroidComponents.VERSION}"

    const val mozilla_browser_engine_gecko = "org.mozilla.components:browser-engine-gecko:${AndroidComponents.VERSION}"
    const val mozilla_browser_domains = "org.mozilla.components:browser-domains:${AndroidComponents.VERSION}"
    const val mozilla_browser_session_storage = "org.mozilla.components:browser-session-storage:${AndroidComponents.VERSION}"
    const val mozilla_browser_state = "org.mozilla.components:browser-state:${AndroidComponents.VERSION}"
    const val mozilla_browser_tabstray = "org.mozilla.components:browser-tabstray:${AndroidComponents.VERSION}"
    const val mozilla_browser_toolbar = "org.mozilla.components:browser-toolbar:${AndroidComponents.VERSION}"
    const val mozilla_browser_menu = "org.mozilla.components:browser-menu:${AndroidComponents.VERSION}"
    const val mozilla_browser_menu2 = "org.mozilla.components:browser-menu2:${AndroidComponents.VERSION}"
    const val mozilla_browser_errorpages = "org.mozilla.components:browser-errorpages:${AndroidComponents.VERSION}"
    const val mozilla_browser_storage_sync = "org.mozilla.components:browser-storage-sync:${AndroidComponents.VERSION}"
    const val mozilla_browser_icons = "org.mozilla.components:browser-icons:${AndroidComponents.VERSION}"
    const val mozilla_browser_thumbnails = "org.mozilla.components:browser-thumbnails:${AndroidComponents.VERSION}"

    const val mozilla_feature_accounts = "org.mozilla.components:feature-accounts:${AndroidComponents.VERSION}"
    const val mozilla_feature_accounts_push = "org.mozilla.components:feature-accounts-push:${AndroidComponents.VERSION}"
    const val mozilla_feature_addons = "org.mozilla.components:feature-addons:${AndroidComponents.VERSION}"
    const val mozilla_feature_app_links = "org.mozilla.components:feature-app-links:${AndroidComponents.VERSION}"
    const val mozilla_feature_autofill = "org.mozilla.components:feature-autofill:${AndroidComponents.VERSION}"
    const val mozilla_feature_awesomebar = "org.mozilla.components:feature-awesomebar:${AndroidComponents.VERSION}"
    const val mozilla_feature_contextmenu = "org.mozilla.components:feature-contextmenu:${AndroidComponents.VERSION}"
    const val mozilla_feature_customtabs = "org.mozilla.components:feature-customtabs:${AndroidComponents.VERSION}"
    const val mozilla_feature_findinpage = "org.mozilla.components:feature-findinpage:${AndroidComponents.VERSION}"
    const val mozilla_feature_media = "org.mozilla.components:feature-media:${AndroidComponents.VERSION}"
    const val mozilla_feature_sitepermissions = "org.mozilla.components:feature-sitepermissions:${AndroidComponents.VERSION}"
    const val mozilla_feature_intent = "org.mozilla.components:feature-intent:${AndroidComponents.VERSION}"
    const val mozilla_feature_search = "org.mozilla.components:feature-search:${AndroidComponents.VERSION}"
    const val mozilla_feature_session = "org.mozilla.components:feature-session:${AndroidComponents.VERSION}"
    const val mozilla_feature_toolbar = "org.mozilla.components:feature-toolbar:${AndroidComponents.VERSION}"
    const val mozilla_feature_tabs = "org.mozilla.components:feature-tabs:${AndroidComponents.VERSION}"
    const val mozilla_feature_downloads = "org.mozilla.components:feature-downloads:${AndroidComponents.VERSION}"
    const val mozilla_feature_storage = "org.mozilla.components:feature-storage:${AndroidComponents.VERSION}"
    const val mozilla_feature_prompts = "org.mozilla.components:feature-prompts:${AndroidComponents.VERSION}"
    const val mozilla_feature_push = "org.mozilla.components:feature-push:${AndroidComponents.VERSION}"
    const val mozilla_feature_pwa = "org.mozilla.components:feature-pwa:${AndroidComponents.VERSION}"
    const val mozilla_feature_qr = "org.mozilla.components:feature-qr:${AndroidComponents.VERSION}"
    const val mozilla_feature_readerview = "org.mozilla.components:feature-readerview:${AndroidComponents.VERSION}"
    const val mozilla_feature_syncedtabs = "org.mozilla.components:feature-syncedtabs:${AndroidComponents.VERSION}"
    const val mozilla_feature_webauthn = "org.mozilla.components:feature-webauthn:${AndroidComponents.VERSION}"
    const val mozilla_feature_webcompat = "org.mozilla.components:feature-webcompat:${AndroidComponents.VERSION}"
    const val mozilla_feature_webnotifications = "org.mozilla.components:feature-webnotifications:${AndroidComponents.VERSION}"

    const val mozilla_ui_autocomplete = "org.mozilla.components:ui-autocomplete:${AndroidComponents.VERSION}"
    const val mozilla_ui_colors = "org.mozilla.components:ui-colors:${AndroidComponents.VERSION}"
    const val mozilla_ui_icons = "org.mozilla.components:ui-icons:${AndroidComponents.VERSION}"
    const val mozilla_ui_tabcounter = "org.mozilla.components:ui-tabcounter:${AndroidComponents.VERSION}"
    const val mozilla_ui_widgets = "org.mozilla.components:ui-widgets:${AndroidComponents.VERSION}"

    const val mozilla_service_firefox_accounts = "org.mozilla.components:service-firefox-accounts:${AndroidComponents.VERSION}"
    const val mozilla_service_location = "org.mozilla.components:service-location:${AndroidComponents.VERSION}"
    const val mozilla_service_sync_logins = "org.mozilla.components:service-sync-logins:${AndroidComponents.VERSION}"

    const val mozilla_support_images = "org.mozilla.components:support-images:${AndroidComponents.VERSION}"
    const val mozilla_support_utils = "org.mozilla.components:support-utils:${AndroidComponents.VERSION}"
    const val mozilla_support_ktx= "org.mozilla.components:support-ktx:${AndroidComponents.VERSION}"
    const val mozilla_support_rustlog = "org.mozilla.components:support-rustlog:${AndroidComponents.VERSION}"
    const val mozilla_support_rusthttp = "org.mozilla.components:support-rusthttp:${AndroidComponents.VERSION}"
    const val mozilla_support_webextensions = "org.mozilla.components:support-webextensions:${AndroidComponents.VERSION}"

    const val mozilla_lib_crash = "org.mozilla.components:lib-crash:${AndroidComponents.VERSION}"
    const val mozilla_lib_crash_sentry = "org.mozilla.components:lib-crash-sentry:${AndroidComponents.VERSION}"
    const val mozilla_lib_push_firebase = "org.mozilla.components:lib-push-firebase:${AndroidComponents.VERSION}"
    const val mozilla_lib_dataprotect = "org.mozilla.components:lib-dataprotect:${AndroidComponents.VERSION}"
    const val mozilla_lib_publicsuffixlist = "org.mozilla.components:lib-publicsuffixlist:${AndroidComponents.VERSION}"

    const val thirdparty_sentry = "io.sentry:sentry-android:${Versions.ThirdParty.sentry}"

    const val androidx_appcompat = "androidx.appcompat:appcompat:${Versions.AndroidX.appcompat}"
    const val androidx_core_ktx = "androidx.core:core-ktx:${Versions.AndroidX.core}"
    const val androidx_constraintlayout = "androidx.constraintlayout:constraintlayout:${Versions.AndroidX.constraintlayout}"
    const val androidx_lifecycle_process = "androidx.lifecycle:lifecycle-process:${Versions.AndroidX.lifecycle}"
    const val androidx_preference_ktx = "androidx.preference:preference-ktx:${Versions.AndroidX.preference}"
    const val androidx_work_runtime_ktx = "androidx.work:work-runtime-ktx:${Versions.AndroidX.work}"
    const val androidx_compose_ui = "androidx.compose.ui:ui:${Versions.AndroidX.compose}"
    const val androidx_compose_ui_tooling = "androidx.compose.ui:ui-tooling:${Versions.AndroidX.compose}"
    const val androidx_compose_foundation = "androidx.compose.foundation:foundation:${Versions.AndroidX.compose}"
    const val androidx_compose_material = "androidx.compose.material:material:${Versions.AndroidX.compose}"
    const val androidx_activity_compose = "androidx.activity:activity-compose:${Versions.AndroidX.activity_compose}"
    const val androidx_swiperefreshlayout = "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.AndroidX.swiperefreshlayout}"

    const val google_material = "com.google.android.material:material:${Versions.Google.material}"

    const val tools_androidgradle = "com.android.tools.build:gradle:${Versions.Gradle.android_plugin}"
    const val tools_kotlingradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.Gradle.kotlin_plugin}"

    const val espresso_contrib = "androidx.test.espresso:espresso-contrib:${Versions.Testing.androidx_espresso}"
    const val espresso_core = "androidx.test.espresso:espresso-core:${Versions.Testing.androidx_espresso}"
    const val espresso_idling_resources = "androidx.test.espresso:espresso-idling-resource:${Versions.Testing.androidx_espresso}"
    const val espresso_web = "androidx.test.espresso:espresso-web:${Versions.Testing.androidx_espresso}"
    const val mockwebserver = "com.squareup.okhttp3:mockwebserver:${Versions.Testing.mockwebserver}"
    const val orchestrator =  "androidx.test:orchestrator:${Versions.Testing.androidx_orchestrator}"
    const val tools_test_rules = "androidx.test:rules:${Versions.Testing.androidx_core}"
    const val tools_test_runner = "androidx.test:runner:${Versions.Testing.androidx_runner}"
    const val uiautomator = "androidx.test.uiautomator:uiautomator:${Versions.Testing.androidx_uiautomator}"
    const val junit_ktx = "androidx.test.ext:junit-ktx:${Versions.Testing.androidx_ext_junit}"
}
