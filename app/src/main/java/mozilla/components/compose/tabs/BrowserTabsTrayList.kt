/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.compose.tabs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.tabstray.TabsTray
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.lib.state.observeAsState

/**
 * Observes the tabs that the [TabsTray] API tells us to in [TabsTray.updateTabs].
 */
@Composable
fun BrowserTabsTrayList(
    adapter: ComposableTrayAdapter,
    useCases: TabsUseCases? = null,
    closeTabsTray: () -> Unit = {}
) {
    val tabs = remember { adapter.observe() }

    TabsTrayList(tabs.value, useCases, closeTabsTray)
}

/**
 * Observes the [BrowserState.tabs] directly to display all of them.
 */
@Composable
fun BrowserTabsTrayList(
    browserStore: BrowserStore,
    useCases: TabsUseCases? = null,
    closeTabsTray: () -> Unit = {}
) {
    val tabs by browserStore.observeAsState { state -> state.toTabs() }

    tabs?.let { TabsTrayList(it, useCases, closeTabsTray) }
}
