/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabs

import android.content.Intent
import mozilla.components.browser.state.selector.findTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.intent.processing.IntentProcessor
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.support.utils.SafeIntent
import mozilla.components.support.utils.SwitchTabIntent

/**
 * Selects the tab given by a [SwitchTabIntent], for example when the user expands the picture-in-picture window of a
 * video.
 */
class SwitchTabIntentProcessor(
    private val store: BrowserStore,
    private val selectTab: TabsUseCases.SelectTabUseCase,
) : IntentProcessor {
    override fun process(intent: Intent): Boolean {
        val safeIntent = SafeIntent(intent)
        val tabId = safeIntent.getStringExtra(SwitchTabIntent.EXTRA_TAB_ID)
        if (
            tabId == null ||
                safeIntent.action != SwitchTabIntent.ACTION_SWITCH_TAB ||
                store.state.findTab(tabId) == null
        ) {
            return false
        }
        selectTab(tabId)
        return true
    }
}
