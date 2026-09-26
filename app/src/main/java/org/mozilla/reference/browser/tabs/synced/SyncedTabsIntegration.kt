/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabs.synced

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import mozilla.components.concept.sync.AccountObserver
import mozilla.components.concept.sync.AuthType
import mozilla.components.concept.sync.OAuthAccount
import mozilla.components.service.fxa.manager.FxaAccountManager
import org.mozilla.reference.browser.ext.components

/** Starts synced tabs storage when a Firefox account signs in and stops it when the account signs out. */
class SyncedTabsIntegration(
    private val context: Context,
    private val accountManager: FxaAccountManager,
) {
    /** The observer is bound to the process lifecycle and paused while the app is in the background. */
    fun launch() {
        val accountObserver = SyncedTabsAccountObserver(context)

        accountManager.register(
            accountObserver,
            owner = ProcessLifecycleOwner.get(),
            autoPause = true,
        )
    }
}

internal class SyncedTabsAccountObserver(private val context: Context) : AccountObserver {
    override fun onAuthenticated(
        account: OAuthAccount,
        authType: AuthType,
    ) {
        context.components.backgroundServices.syncedTabsStorage.start()
    }

    override fun onLoggedOut() {
        context.components.backgroundServices.syncedTabsStorage.stop()
    }
}
