/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabs.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.compose.base.button.IconButton
import mozilla.components.compose.base.theme.AcornTheme
import mozilla.components.ui.icons.R as iconsR
import org.mozilla.reference.browser.R

internal val TabListItemHeight: Dp = ThumbnailHeight + 16.dp

/**
 * A single tab in the tabs tray, showing the title and URL of the tab.
 *
 * @param tab The tab to render.
 * @param isSelected Whether this tab is the currently selected one.
 * @param onClick Invoked when the user taps the tab.
 * @param onCloseClick Invoked when the user taps the close button of the tab.
 */
@Composable
internal fun TabListItem(
    tab: TabSessionState,
    isSelected: Boolean,
    onClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    val backgroundColor =
        if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }

    Row(
        modifier =
            Modifier.fillMaxWidth()
                .height(TabListItemHeight)
                .background(backgroundColor)
                .clickable(onClick = onClick)
                .padding(horizontal = AcornTheme.layout.space.static100),
        horizontalArrangement = Arrangement.spacedBy(AcornTheme.layout.space.static100),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TabThumbnail(tabId = tab.id, isPrivate = tab.content.private)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tab.content.title.ifEmpty { tab.content.url },
                style = AcornTheme.typography.subtitle1,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = tab.content.url,
                style = AcornTheme.typography.body2,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        IconButton(onClick = onCloseClick, contentDescription = stringResource(R.string.tabs_tray_close_tab)) {
            Icon(painter = painterResource(iconsR.drawable.mozac_ic_cross_24), contentDescription = null)
        }
    }
}
