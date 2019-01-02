/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.settings

import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RawRes
import org.mozilla.reference.browser.R
import org.mozilla.geckoview.BuildConfig
import org.mozilla.reference.browser.ext.replace

object AboutPage {
    fun createAboutPage(context: Context): String {
        val substitutionMap = mutableMapOf<String, String>()
        val appName = context.resources.getString(R.string.app_name)

        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val geckoVersion = packageInfo.versionCode.toString() + " \uD83E\uDD8E " +
                BuildConfig.MOZ_APP_VERSION + "-" + BuildConfig.MOZ_APP_BUILDID
            String.format(
                "%s (Build #%s)",
                packageInfo.versionName,
                geckoVersion
            ).also { aboutVersion ->
                substitutionMap["%about-version%"] = aboutVersion
            }
        } catch (e: PackageManager.NameNotFoundException) {
            // Nothing to do if we can't find the package name.
        }

        context.resources.getString(R.string.about_content, appName).also { content ->
            substitutionMap["%about-content%"] = content
        }

        return loadResourceFile(context, R.raw.about, substitutionMap)
    }

    private fun loadResourceFile(context: Context, @RawRes resId: Int, replacements: Map<String, String>): String {
        context.resources.openRawResource(resId)
            .bufferedReader()
            .use { it.readText() }
            .also { return it.replace(replacements) }
    }
}
