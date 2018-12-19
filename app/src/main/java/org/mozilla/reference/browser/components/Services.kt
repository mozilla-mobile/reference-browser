/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.components

import android.content.Context
import mozilla.components.browser.storage.sync.PlacesHistoryStorage
import mozilla.components.browser.storage.sync.SyncAuthInfo
import mozilla.components.feature.sync.FirefoxSyncFeature
import mozilla.components.feature.tabs.TabsUseCases
import org.mozilla.reference.browser.browser.FirefoxAccountsIntegration

/**
 * Component group for all application services.
 */
class Services(
    private val context: Context,
    private val tabsUseCases: TabsUseCases,
    private val placesHistoryStorage: PlacesHistoryStorage
) {
    /**
     * Feature component to integrate with Firefox Sync.
     */
    val sync by lazy {
        FirefoxSyncFeature(mapOf("history" to placesHistoryStorage)) {
            SyncAuthInfo(it.kid, it.fxaAccessToken, it.syncKey, it.tokenServerUrl)
        }
    }

    /**
     * Integration component for Firefox Accounts.
     */
    val accounts: FirefoxAccountsIntegration by lazy {
        FirefoxAccountsIntegration(context, tabsUseCases, sync)
    }
}
