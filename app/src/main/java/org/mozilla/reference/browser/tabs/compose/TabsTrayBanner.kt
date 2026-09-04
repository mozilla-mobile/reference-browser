/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabs.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mozilla.components.compose.base.button.IconButton
import mozilla.components.compose.base.menu.DropdownMenu
import mozilla.components.compose.base.menu.MenuItem
import mozilla.components.compose.base.text.Text
import mozilla.components.ui.icons.R as iconsR
import mozilla.components.ui.tabcounter.TabCounter
import org.mozilla.reference.browser.R

private val BannerHeight: Dp = 48.dp

/**
 * The banner shown at the top of the tabs tray.
 *
 * @param selectedPage The page of the tray the user is looking at.
 * @param normalTabCount How many normal tabs are open, shown inside the normal tabs page indicator.
 * @param onCloseTrayClick Invoked when the user wants to leave the tray and return to the browser.
 * @param onPageClick Invoked when the user selects one of the tray pages.
 * @param onNewTabClick Invoked when the user wants a new tab for the selected page.
 * @param onCloseAllTabsClick Invoked when the user wants to close every tab of the selected page.
 */
@Composable
internal fun TabsTrayBanner(
    selectedPage: TabsTrayPage,
    normalTabCount: Int,
    onCloseTrayClick: () -> Unit,
    onPageClick: (TabsTrayPage) -> Unit,
    onNewTabClick: () -> Unit,
    onCloseAllTabsClick: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(modifier = Modifier.fillMaxWidth().statusBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCloseTrayClick, contentDescription = stringResource(R.string.tabs_tray_go_back)) {
                Icon(painter = painterResource(iconsR.drawable.mozac_ic_back_24), contentDescription = null)
            }

            PageTabs(
                selectedPage = selectedPage,
                normalTabCount = normalTabCount,
                onPageClick = onPageClick,
                modifier = Modifier.weight(1f),
            )

            IconButton(onClick = onNewTabClick, contentDescription = stringResource(R.string.menu_action_add_tab)) {
                Icon(painter = painterResource(iconsR.drawable.mozac_ic_plus_24), contentDescription = null)
            }

            MoreOptionsButton(selectedPage = selectedPage, onCloseAllTabsClick = onCloseAllTabsClick)
        }
    }
}

@Composable
private fun PageTabs(
    selectedPage: TabsTrayPage,
    normalTabCount: Int,
    onPageClick: (TabsTrayPage) -> Unit,
    modifier: Modifier,
) {
    val selectedIndex = TabsTrayPage.entries.indexOf(selectedPage)

    PrimaryTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = {
            TabRowDefaults.PrimaryIndicator(
                modifier = Modifier.tabIndicatorOffset(selectedTabIndex = selectedIndex, matchContentSize = true),
                width = Dp.Unspecified,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        divider = {},
    ) {
        PageTab(
            selected = selectedPage == TabsTrayPage.NormalTabs,
            contentDescription = stringResource(R.string.tabs_tray_normal_tabs),
            onClick = { onPageClick(TabsTrayPage.NormalTabs) },
        ) {
            TabCounter(tabCount = normalTabCount)
        }

        PageTab(
            selected = selectedPage == TabsTrayPage.PrivateTabs,
            contentDescription = stringResource(R.string.tabs_tray_private_tabs),
            onClick = { onPageClick(TabsTrayPage.PrivateTabs) },
        ) {
            Icon(painter = painterResource(iconsR.drawable.mozac_ic_private_mode_24), contentDescription = null)
        }
    }
}

@Composable
private fun PageTab(
    selected: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.height(BannerHeight).semantics { this.contentDescription = contentDescription },
        selectedContentColor = MaterialTheme.colorScheme.onSurface,
        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        content()
    }
}

@Composable
private fun MoreOptionsButton(
    selectedPage: TabsTrayPage,
    onCloseAllTabsClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val closeAllTabsText =
        when (selectedPage) {
            TabsTrayPage.NormalTabs -> R.string.menu_action_close_tabs
            TabsTrayPage.PrivateTabs -> R.string.menu_action_close_tabs_private
        }

    IconButton(
        onClick = { expanded = true },
        contentDescription = stringResource(R.string.tabs_tray_more_options),
    ) {
        Icon(painter = painterResource(iconsR.drawable.mozac_ic_ellipsis_vertical_24), contentDescription = null)

        DropdownMenu(
            menuItems =
                listOf(
                    MenuItem.IconItem(
                        text = Text.Resource(closeAllTabsText),
                        drawableRes = iconsR.drawable.mozac_ic_delete_24,
                        level = MenuItem.FixedItem.Level.Critical,
                        onClick = {
                            expanded = false
                            onCloseAllTabsClick()
                        },
                    )
                ),
            expanded = expanded,
            onDismissRequest = { expanded = false },
        )
    }
}
