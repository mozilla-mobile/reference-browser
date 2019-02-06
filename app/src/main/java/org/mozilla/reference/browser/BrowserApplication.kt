/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.app.Application
import android.content.Context
import mozilla.components.service.glean.Glean
import mozilla.components.support.base.log.Log
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.base.log.sink.AndroidLogSink
import org.mozilla.reference.browser.ext.isCrashReportActive
import org.mozilla.reference.browser.settings.Settings

open class BrowserApplication : Application() {
    val components by lazy { Components(this) }

    override fun onCreate() {
        super.onCreate()

        setupLogging()
        setupCrashReporting(this)
        setupGlean(this)
        setupMegazord()
    }

    companion object {
        const val NON_FATAL_CRASH_BROADCAST = "org.mozilla.reference.browser"
    }
}

private fun setupLogging() {
    // We want the log messages of all builds to go to Android logcat
    Log.addSink(AndroidLogSink())
}

private fun setupGlean(context: Context) {
    Glean.initialize(context)
    Glean.setMetricsEnabled(BuildConfig.TELEMETRY_ENABLED && Settings.isTelemetryEnabled(context))
}

private fun setupCrashReporting(application: BrowserApplication) {
    if (isCrashReportActive) {
        application
            .components
            .analytics
            .crashReporter.install(application)
    }
}

/**
 * Initiate Megazord sequence! Megazord Battle Mode!
 *
 * Mozilla Application Services publishes many native (Rust) code libraries that stand alone: each published Android
 * ARchive (AAR) contains managed code (classes.jar) and multiple .so library files (one for each supported
 * architecture). That means consuming multiple such libraries entails at least two .so libraries, and each of those
 * libraries includes the entire Rust standard library as well as (potentially many) duplicated dependencies. To save
 * space and allow cross-component native-code Link Time Optimization (LTO, i.e., inlining, dead code elimination, etc)
 * Application Services also publishes composite libraries -- so called megazord libraries or just megazords -- that
 * compose multiple Rust components into a single optimized .so library file.
 */
private fun setupMegazord() {
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
