import org.gradle.api.Project
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

object Config {
    // Synchronized build configuration for all modules
    const val compileSdkVersion = 33
    const val minSdkVersion = 21
    const val targetSdkVersion = 33

    @JvmStatic
    fun generateDebugVersionName(): String {
        val today = Date()
        // Append the year (2 digits) and week in year (2 digits). This will make it easier to distinguish versions and
        // identify ancient versions when debugging issues. However this will still keep the same version number during
        // the week so that we do not end up with a lot of versions in tools like Sentry. As an extra this matches the
        // sections we use in the changelog (weeks).
        return SimpleDateFormat("1.0.yyww", Locale.US).format(today)
    }

    @JvmStatic
    fun releaseVersionName(project: Project): String {
        // This function is called in the configuration phase, before gradle knows which variants we'll use.
        // So, validation that "versionName" has been set happens elsewhere (at time of writing, we staple
        // validation to tasks of type "AppPreBuildTask"
        return if (project.hasProperty("versionName")) project.property("versionName") as String else ""
    }
}
