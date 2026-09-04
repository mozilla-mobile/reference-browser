/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.thumbnails.storage.ThumbnailStorage
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.tabs.TabsUseCases
import org.mozilla.reference.browser.ext.components

/** Composable helper for providing the [BrowserStore] instance of this application. */
@Composable fun browserStore(): BrowserStore = LocalContext.current.components.core.store

@Composable fun sessionUseCases(): SessionUseCases = LocalContext.current.components.useCases.sessionUseCases

/** Composable helper for providing the [TabsUseCases] instance of this application. */
@Composable fun tabsUseCases(): TabsUseCases = LocalContext.current.components.useCases.tabsUseCases

/** Composable helper for providing the [ThumbnailStorage] instance of this application. */
@Composable fun thumbnailStorage(): ThumbnailStorage = LocalContext.current.components.core.thumbnailStorage
