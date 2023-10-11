/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mozilla.components.lib.crash.Crash
import mozilla.components.lib.crash.CrashReporter
import mozilla.components.support.utils.ext.registerReceiverCompat
import org.mozilla.reference.browser.BrowserApplication.Companion.NON_FATAL_CRASH_BROADCAST
import org.mozilla.reference.browser.ext.isCrashReportActive

class CrashIntegration(
    private val context: Context,
    private val crashReporter: CrashReporter,
    private val onCrash: (Crash) -> Unit,
) : DefaultLifecycleObserver {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!Crash.isCrashIntent(intent)) {
                return
            }

            val crash = Crash.fromIntent(intent)
            onCrash(crash)
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        if (isCrashReportActive) {
            context.registerReceiverCompat(
                receiver,
                IntentFilter(NON_FATAL_CRASH_BROADCAST),
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        if (isCrashReportActive) {
            context.unregisterReceiver(receiver)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun sendCrashReport(crash: Crash) {
        GlobalScope.launch {
            crashReporter.submitReport(crash)
        }
    }
}
