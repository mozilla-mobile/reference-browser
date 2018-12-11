/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.app.Application
import mozilla.components.support.base.log.Log
import mozilla.components.support.base.log.sink.AndroidLogSink
import org.mozilla.reference.browser.ext.isCrashReportActive
import android.app.ActivityManager
import android.content.Context

class BrowserApplication : Application() {
    val components by lazy { Components(this) }

    override fun onCreate() {
        super.onCreate()

        // We only want to run this for the application process and not for sub
        // processes e.g. the Gecko :tab process. Otherwise we'd be initializing
        // our components multiple times and we'd also create the GeckoEngine
        // and GeckoRuntime in the wrong process.
        if (isChildProcess()) {
            return
        }

        // We want the log messages of all builds to go to Android logcat
        Log.addSink(AndroidLogSink())

        if (isCrashReportActive) {
            components.crashReporter.install(this)
        }
    }

    private fun isChildProcess(): Boolean {
        val pid = android.os.Process.myPid()
        val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processInfo = activityManager.runningAppProcesses.firstOrNull { it.pid == pid }
        return processInfo?.processName?.contains(":") ?: false
    }

    companion object {
        const val NON_FATAL_CRASH_BROADCAST = "org.mozilla.reference.browser"
    }
}
