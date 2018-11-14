/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.app.Application
import mozilla.components.feature.storage.HistoryTrackingFeature
import mozilla.components.support.base.log.Log
import mozilla.components.support.base.log.sink.AndroidLogSink
import org.mozilla.reference.browser.ext.isCrashReportActive

class BrowserApplication : Application() {
    val components by lazy { Components(this) }

    val historyTrackingFeature by lazy {
        HistoryTrackingFeature(
            this.components.engine,
            this.components.placesHistoryStorage)
    }

    override fun onCreate() {
        super.onCreate()

        // We want the log messages of all builds to go to Android logcat
        Log.addSink(AndroidLogSink())

        if (isCrashReportActive) {
            components.crashReporter.install(this)
        }
    }

    companion object {
        const val NON_FATAL_CRASH_BROADCAST = "org.mozilla.reference.browser"
    }
}
