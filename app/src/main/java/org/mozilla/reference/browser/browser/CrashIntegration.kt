/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.launch
import mozilla.components.lib.crash.Crash
import mozilla.components.lib.crash.CrashReporter
import org.mozilla.reference.browser.BrowserApplication.Companion.NON_FATAL_CRASH_BROADCAST
import org.mozilla.reference.browser.ext.isCrashReportActive

class CrashIntegration(
    private val context: Context,
    private val crashReporter: CrashReporter,
    private val onCrash: (Crash) -> Unit
) : LifecycleObserver {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!Crash.isCrashIntent(intent)) {
                return
            }

            val crash = Crash.fromIntent(intent)
            onCrash(crash)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        if (isCrashReportActive) {
            context.registerReceiver(receiver, IntentFilter(NON_FATAL_CRASH_BROADCAST))
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        if (isCrashReportActive) {
            context.unregisterReceiver(receiver)
        }
    }

    fun sendCrashReport(crash: Crash) {
        CoroutineScope(Dispatchers.Default).launch {
            crashReporter.submitReport(crash)
        }
    }
}
