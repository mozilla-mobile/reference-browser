/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package mozilla.components.compose.tabs

import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.concept.tabstray.Tabs

/**
 * Copied from [mozilla.components.feature.tabs.ext.toTab].
 */
internal fun TabSessionState.toTab() = mozilla.components.concept.tabstray.Tab(
    id,
    content.url,
    content.title,
    content.private,
    content.icon,
    content.thumbnail,
    mediaSessionState?.playbackState,
    mediaSessionState?.controller
)

/**
 * Copied from [mozilla.components.feature.tabs.ext.toTabs].
 */
internal fun BrowserState.toTabs(
    tabsFilter: (TabSessionState) -> Boolean = { true }
) = Tabs(
    list = tabs
        .filter(tabsFilter)
        .map { it.toTab() },
    selectedIndex = tabs
        .filter(tabsFilter)
        .indexOfFirst { it.id == selectedTabId }
)
