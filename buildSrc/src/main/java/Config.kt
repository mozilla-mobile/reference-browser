/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.gradle.api.Project

/** SDK levels, build tools and version names for the app's Gradle build. */
object Config {
    const val buildToolsVersion = "37.0.0"
    const val compileSdkMajorVersion = 37
    const val compileSdkMinorVersion = 2
    const val minSdkVersion = 26
    const val targetSdkVersion = 37
    const val jvmTargetCompatibility = 17

    /**
     * Appends the year and week of year (two digits each), so ancient builds are easy to spot when debugging while the
     * version stays the same all week and tools like Sentry do not fill up with versions.
     */
    @JvmStatic fun generateDebugVersionName(): String = SimpleDateFormat("1.0.yyww", Locale.US).format(Date())

    /**
     * Nightly tasks pass `-PversionName`. This runs at configuration time, before Gradle knows which variant is being
     * built, so it returns an empty string instead of failing when the property is absent.
     */
    @JvmStatic fun releaseVersionName(project: Project): String = project.findProperty("versionName") as String? ?: ""
}
