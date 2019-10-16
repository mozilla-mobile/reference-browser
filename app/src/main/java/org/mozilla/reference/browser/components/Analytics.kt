/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.components

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import mozilla.components.lib.crash.CrashReporter
import mozilla.components.lib.crash.service.GleanCrashReporterService
import mozilla.components.lib.crash.service.MozillaSocorroService
import mozilla.components.lib.crash.service.SentryService
import mozilla.components.service.experiments.Experiments
import mozilla.components.service.glean.Glean
import mozilla.components.service.glean.config.Configuration
import mozilla.components.service.glean.net.ConceptFetchHttpUploader
import org.mozilla.geckoview.BuildConfig.MOZ_APP_BUILDID
import org.mozilla.geckoview.BuildConfig.MOZ_APP_VERSION
import org.mozilla.reference.browser.BrowserApplication
import org.mozilla.reference.browser.BuildConfig
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.settings.Settings

/**
 * Component group for all functionality related to analytics e.g. crash
 * reporting and telemetry.
 */
class Analytics(private val context: Context) {

    /**
     * A generic crash reporter component configured to use both Sentry and Socorro.
     */
    val crashReporter: CrashReporter by lazy {
        val sentryService = SentryService(
            context,
            BuildConfig.SENTRY_TOKEN,
            mapOf("geckoview" to "$MOZ_APP_VERSION-$MOZ_APP_BUILDID"),
            sendEventForNativeCrashes = true
        )

        val socorroService = MozillaSocorroService(context, "ReferenceBrowser")

        val gleanCrashReporter = GleanCrashReporterService(context)

        CrashReporter(
            services = listOf(sentryService, socorroService, gleanCrashReporter),
            shouldPrompt = CrashReporter.Prompt.ALWAYS,
            promptConfiguration = CrashReporter.PromptConfiguration(
                appName = context.getString(R.string.app_name),
                organizationName = "Mozilla"
            ),
            nonFatalCrashIntent = PendingIntent
                .getBroadcast(context, 0, Intent(BrowserApplication.NON_FATAL_CRASH_BROADCAST), 0),
            enabled = true
        )
    }

    internal fun initializeGlean() {
        Glean.setUploadEnabled(BuildConfig.TELEMETRY_ENABLED && Settings.isTelemetryEnabled(context))
        Glean.initialize(context, Configuration(
            httpClient = ConceptFetchHttpUploader(lazy { context.components.core.client })
        ))
    }

    internal fun initializeExperiments() {
        Experiments.initialize(
            context,
            mozilla.components.service.experiments.Configuration(httpClient = lazy { context.components.core.client })
        )
    }
}
