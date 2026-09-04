/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabs.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import mozilla.components.compose.base.theme.AcornTheme
import mozilla.components.compose.base.theme.acornDarkColorScheme
import mozilla.components.compose.base.theme.darkColorPalette

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
    AcornTheme(colors = darkColorPalette, colorScheme = acornDarkColorScheme()) {
        Scaffold(topBar = { TabsTrayBanner(onCloseTrayClick = onCloseTrayClick) }) { contentPadding ->
            Box(modifier = Modifier.padding(contentPadding).fillMaxSize())
        }
    }
}
