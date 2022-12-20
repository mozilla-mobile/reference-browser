/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.components

import android.content.Context
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine
import mozilla.components.feature.contextmenu.ContextMenuUseCases
import mozilla.components.feature.downloads.DownloadsUseCases
import mozilla.components.feature.pwa.WebAppShortcutManager
import mozilla.components.feature.pwa.WebAppUseCases
import mozilla.components.feature.search.SearchUseCases
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.session.SettingsUseCases
import mozilla.components.feature.tabs.CustomTabsUseCases
import mozilla.components.feature.tabs.TabsUseCases

/**
 * Component group for all use cases. Use cases are provided by feature
 * modules and can be triggered by UI interactions.
 */
class UseCases(
    private val context: Context,
    private val engine: Engine,
    private val store: BrowserStore,
    private val shortcutManager: WebAppShortcutManager,
) {
    /**
     * Use cases that provide engine interactions for a given browser session.
     */
    val sessionUseCases by lazy { SessionUseCases(store) }

    /**
     * Use cases that provide tab management.
     */
    val tabsUseCases: TabsUseCases by lazy { TabsUseCases(store) }

    /**
     * Use cases that provide search engine integration.
     */
    val searchUseCases by lazy {
        SearchUseCases(store, tabsUseCases, sessionUseCases)
    }

    /**
     * Use cases that provide settings management.
     */
    val settingsUseCases by lazy { SettingsUseCases(engine, store) }

    /**
     * Use cases that provide shortcut and progressive web app management.
     */
    val webAppUseCases by lazy { WebAppUseCases(context, store, shortcutManager) }

    /**
     * Uses cases that provides context menu
     */
    val contextMenuUseCases: ContextMenuUseCases by lazy { ContextMenuUseCases(store) }

    /**
     * Use cases related to the downloads feature.
     */
    val downloadsUseCases: DownloadsUseCases by lazy { DownloadsUseCases(store) }

    /**
     * Use cases related to Custom Tabs.
     */
    val customTabsUseCases: CustomTabsUseCases by lazy { CustomTabsUseCases(store, sessionUseCases.loadUrl) }
}
