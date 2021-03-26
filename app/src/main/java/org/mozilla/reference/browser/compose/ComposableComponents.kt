/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.session.SessionUseCases
import org.mozilla.reference.browser.ext.components

/**
 * Composable helper for providing the [BrowserStore] instance of this application.
 */
@Composable
fun browserStore(): BrowserStore {
    return LocalContext.current.components.core.store
}

@Composable
fun sessionUseCases(): SessionUseCases {
    return LocalContext.current.components.useCases.sessionUseCases
}
