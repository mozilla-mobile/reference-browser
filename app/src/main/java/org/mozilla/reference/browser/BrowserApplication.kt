/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.app.Application
import mozilla.components.service.glean.Glean
import mozilla.components.support.base.log.Log
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.base.log.sink.AndroidLogSink
import org.mozilla.reference.browser.ext.isCrashReportActive
import org.mozilla.reference.browser.settings.Settings

class BrowserApplication : Application() {
    val components by lazy { Components(this) }

    override fun onCreate() {
        super.onCreate()

        // We want the log messages of all builds to go to Android logcat
        Log.addSink(AndroidLogSink())

        Glean.initialize(this)
        Glean.setMetricsEnabled(BuildConfig.TELEMETRY_ENABLED && Settings.isTelemetryEnabled(this))

        if (isCrashReportActive) {
            components.analytics.crashReporter.install(this)
        }

        // mozilla.appservices.ReferenceBrowserMegazord will be missing if we're doing an application-services
        // dependency substitution locally. That class is supplied dynamically by the org.mozilla.appservices
        // gradle plugin, and that won't happen if we're not megazording. We won't megazord if we're
        // locally substituting every module that's part of the megazord's definition, which is what
        // happens during a local substitution of application-services.
        // As a workaround, use reflections to conditionally initialize the megazord in case it's present.
        // See https://github.com/mozilla-mobile/reference-browser/pull/356.
        try {
            val megazordClass = Class.forName("mozilla.appservices.ReferenceBrowserMegazord")
            val megazordInitMethod = megazordClass.getDeclaredMethod("init")
            megazordInitMethod.invoke(megazordClass)
        } catch (e: ClassNotFoundException) {
            Logger.info("mozilla.appservices.ReferenceBrowserMegazord not found; skipping megazord init.")
        }
    }

    companion object {
        const val NON_FATAL_CRASH_BROADCAST = "org.mozilla.reference.browser"
    }
}
