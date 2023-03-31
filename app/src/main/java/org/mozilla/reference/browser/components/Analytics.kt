/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.components

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.content.Context
import android.content.Intent
import android.os.Build
import mozilla.components.lib.crash.CrashReporter
import mozilla.components.lib.crash.sentry.SentryService
import mozilla.components.lib.crash.service.CrashReporterService
import mozilla.components.lib.crash.service.MozillaSocorroService
import org.mozilla.geckoview.BuildConfig.MOZ_APP_BUILDID
import org.mozilla.geckoview.BuildConfig.MOZ_APP_VENDOR
import org.mozilla.geckoview.BuildConfig.MOZ_APP_VERSION
import org.mozilla.geckoview.BuildConfig.MOZ_UPDATE_CHANNEL
import org.mozilla.reference.browser.BrowserApplication
import org.mozilla.reference.browser.BuildConfig
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components

/**
 * Component group for all functionality related to analytics e.g. crash
 * reporting and telemetry.
 */
class Analytics(private val context: Context) {

    /**
     * A generic crash reporter component configured to use both Sentry and Socorro.
     */
    val crashReporter: CrashReporter by lazy {
        val socorroService = MozillaSocorroService(
            context,
            appName = "ReferenceBrowser",
            version = MOZ_APP_VERSION,
            buildId = MOZ_APP_BUILDID,
            vendor = MOZ_APP_VENDOR,
            releaseChannel = MOZ_UPDATE_CHANNEL,
        )

        val services: MutableList<CrashReporterService> = mutableListOf(socorroService)

        if (isSentryEnabled()) {
            val sentryService = SentryService(
                context,
                BuildConfig.SENTRY_TOKEN,
                mapOf("geckoview" to "$MOZ_APP_VERSION-$MOZ_APP_BUILDID"),
                sendEventForNativeCrashes = true,
            )
            services.add(sentryService)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            FLAG_MUTABLE
        } else {
            0
        }

        CrashReporter(
            context = context,
            services = services,
            telemetryServices = emptyList(),
            shouldPrompt = CrashReporter.Prompt.ALWAYS,
            promptConfiguration = CrashReporter.PromptConfiguration(
                appName = context.getString(R.string.app_name),
                organizationName = "Mozilla",
            ),
            nonFatalCrashIntent = PendingIntent
                .getBroadcast(context, 0, Intent(BrowserApplication.NON_FATAL_CRASH_BROADCAST), flags),
            enabled = true,
            notificationsDelegate = context.components.notificationsDelegate,
        )
    }
}

fun isSentryEnabled() = !BuildConfig.SENTRY_TOKEN.isNullOrEmpty()
