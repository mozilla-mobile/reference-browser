/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.app.Application
import android.content.Context
import mozilla.components.service.glean.Glean
import mozilla.components.service.glean.config.Configuration
import mozilla.components.service.experiments.Experiments
import mozilla.components.service.experiments.Configuration as ExperimentsConfiguration
import mozilla.components.support.base.facts.register
import mozilla.components.support.base.log.Log
import mozilla.components.support.base.log.sink.AndroidLogSink
import mozilla.components.support.ktx.android.content.isMainProcess
import mozilla.components.support.ktx.android.content.runOnlyInMainProcess
import mozilla.components.support.rustlog.RustLog
import mozilla.components.support.rusthttp.RustHttpConfig
import org.mozilla.reference.browser.GleanMetrics.ExperimentsMetrics
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.isCrashReportActive
import org.mozilla.reference.browser.settings.Settings
import org.mozilla.reference.browser.telemetry.GleanFactProcessor

open class BrowserApplication : Application() {
    val components by lazy { Components(this) }

    override fun onCreate() {
        super.onCreate()

        setupCrashReporting(this)

        RustHttpConfig.setClient(lazy { components.core.client })
        setupLogging()

        if (!isMainProcess()) {
            // If this is not the main process then do not continue with the initialization here. Everything that
            // follows only needs to be done in our app's main process and should not be done in other processes like
            // a GeckoView child process or the crash handling process. Most importantly we never want to end up in a
            // situation where we create a GeckoRuntime from the Gecko child process (
            return
        }

        components.core.engine.warmUp()

        setupGlean(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        runOnlyInMainProcess {
            components.core.sessionManager.onLowMemory()
        }
    }

    companion object {
        const val NON_FATAL_CRASH_BROADCAST = "org.mozilla.reference.browser"
    }
}

private fun setupLogging() {
    // We want the log messages of all builds to go to Android logcat
    Log.addSink(AndroidLogSink())
    RustLog.enable()
}

private fun setupGlean(context: Context) {
    Glean.setUploadEnabled(BuildConfig.TELEMETRY_ENABLED && Settings.isTelemetryEnabled(context))
    Glean.initialize(context, Configuration(httpClient = lazy { context.components.core.client }))
    GleanFactProcessor().register()
    Experiments.initialize(
        context,
        ExperimentsConfiguration(httpClient = lazy { context.components.core.client })
    )

    // Recording the experiment ID in Glean through the use of the `withExperiment` function should
    // allow us to validate the automatically recorded enrollment in the experiment, as well as the
    // functionality of doing something within the client app based on the specific branch. It
    // should be noted that the first time that the client is enrolled in the experiment, the
    // following code will not be executed as this only gets called on startup, so it will be on
    // the next application launch following enrollment that the code below will be executed and the
    // metric recorded.
    Experiments.withExperiment("reference-browser-test") { branchName ->
        ExperimentsMetrics.activeExperiment.set(branchName)
    }
}

private fun setupCrashReporting(application: BrowserApplication) {
    if (isCrashReportActive) {
        application
            .components
            .analytics
            .crashReporter.install(application)
    }
}
