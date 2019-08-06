/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.components

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mozilla.components.feature.accounts.FirefoxAccountsAuthFeature
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.service.fxa.manager.FxaAccountManager

/**
 * Component group which encapsulates foreground-friendly services.
 */
class Services(
    private val accountManager: FxaAccountManager,
    private val tabsUseCases: TabsUseCases
) {
    val accountsAuthFeature by lazy {
        FirefoxAccountsAuthFeature(
            accountManager,
            redirectUrl = BackgroundServices.REDIRECT_URL
        ) {
            _, authUrl ->
                MainScope().launch {
                    tabsUseCases.addTab.invoke(authUrl)
                }
        }
    }
}
