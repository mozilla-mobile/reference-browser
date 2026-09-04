/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabs.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import mozilla.components.compose.base.button.IconButton
import mozilla.components.ui.icons.R as iconsR
import org.mozilla.reference.browser.R

/**
 * The banner shown at the top of the tabs tray.
 *
 * @param onCloseTrayClick Invoked when the user wants to leave the tray and return to the browser.
 */
@Composable
internal fun TabsTrayBanner(onCloseTrayClick: () -> Unit) {
    Row(
        modifier =
            Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHigh).statusBarsPadding(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onCloseTrayClick, contentDescription = stringResource(R.string.tabs_tray_go_back)) {
            Icon(painter = painterResource(iconsR.drawable.mozac_ic_back_24), contentDescription = null)
        }
    }
}
