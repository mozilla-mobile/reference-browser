/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabs.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.compose.base.theme.AcornTheme
import mozilla.components.compose.base.theme.acornDarkColorScheme
import mozilla.components.compose.base.theme.acornPrivateColorScheme
import mozilla.components.compose.base.theme.darkColorPalette
import mozilla.components.compose.base.theme.privateColorPalette
import mozilla.components.lib.state.ext.observeAsComposableState
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.compose.browserStore
import org.mozilla.reference.browser.compose.tabsUseCases

private const val NEW_NORMAL_TAB_URL = "about:blank"
private const val NEW_PRIVATE_TAB_URL = "about:privatebrowsing"

/**
 * The tabs tray screen.
 *
 * The theme is applied here rather than by the hosting fragment so that the tray can re-theme itself when the user
 * switches to the private tabs page.
 *
 * @param onCloseTrayClick Invoked when the user wants to leave the tray and return to the browser.
 */
@Composable
internal fun TabsTray(onCloseTrayClick: () -> Unit) {
    val store = browserStore()
    val useCases = tabsUseCases()

    var selectedPage by rememberSaveable { mutableStateOf(TabsTrayPage.NormalTabs) }
    val isPrivate = selectedPage == TabsTrayPage.PrivateTabs

    // The list is filtered here rather than inside observeAsComposableState: that keys its subscription on the store
    // alone, so a new mapping function is ignored and switching page would keep showing the previous page's tabs until
    // the next unrelated store change.
    val allTabs by store.observeAsComposableState { state -> state.tabs }
    val selectedTabId by store.observeAsComposableState { state -> state.selectedTabId }

    val tabs = remember(allTabs, isPrivate) { allTabs.filter { it.content.private == isPrivate } }
    val normalTabCount = remember(allTabs) { allTabs.count { !it.content.private } }

    AcornTheme(
        colors = if (isPrivate) privateColorPalette else darkColorPalette,
        colorScheme = if (isPrivate) acornPrivateColorScheme() else acornDarkColorScheme(),
    ) {
        Scaffold(
            topBar = {
                TabsTrayBanner(
                    selectedPage = selectedPage,
                    normalTabCount = normalTabCount,
                    onCloseTrayClick = onCloseTrayClick,
                    onPageClick = { page -> selectedPage = page },
                    onNewTabClick = {
                        useCases.addTab(
                            url = if (isPrivate) NEW_PRIVATE_TAB_URL else NEW_NORMAL_TAB_URL,
                            selectTab = true,
                            private = isPrivate,
                        )
                        onCloseTrayClick()
                    },
                    onCloseAllTabsClick = {
                        if (isPrivate) useCases.removePrivateTabs() else useCases.removeNormalTabs()
                    },
                )
            }
        ) { contentPadding ->
            TabList(
                tabs = tabs,
                selectedTabId = selectedTabId,
                modifier = Modifier.padding(contentPadding).fillMaxSize(),
                onTabClick = { tab ->
                    useCases.selectTab(tab.id)
                    onCloseTrayClick()
                },
                onTabCloseClick = { tab -> useCases.removeTab(tab.id) },
            )
        }
    }
}

@Composable
private fun TabList(
    tabs: List<TabSessionState>,
    selectedTabId: String?,
    modifier: Modifier,
    onTabClick: (TabSessionState) -> Unit,
    onTabCloseClick: (TabSessionState) -> Unit,
) {
    if (tabs.isEmpty()) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.tabs_tray_no_tabs),
                style = AcornTheme.typography.headline7,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(modifier = modifier.background(MaterialTheme.colorScheme.surface)) {
        items(items = tabs, key = { tab -> tab.id }) { tab ->
            TabListItem(
                tab = tab,
                isSelected = tab.id == selectedTabId,
                onClick = { onTabClick(tab) },
                onCloseClick = { onTabCloseClick(tab) },
            )
        }
    }
}
