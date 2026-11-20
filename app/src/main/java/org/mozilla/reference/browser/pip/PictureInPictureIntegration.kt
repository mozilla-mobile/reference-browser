/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.pip

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import mozilla.components.browser.state.selector.findTabOrCustomTabOrSelectedTab
import mozilla.components.browser.state.state.BrowserState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.feature.pictureinpicture.PictureInPictureUseCases
import mozilla.components.lib.state.helpers.AbstractBinding
import mozilla.components.ui.icons.R as iconsR
import org.mozilla.reference.browser.R

/**
 * Adds a toolbar button that shows the video of the tab in a picture-in-picture window of its own, or closes that
 * window. The button is shown only when the tab has media.
 */
class PictureInPictureIntegration(
    context: Context,
    private val store: BrowserStore,
    private val toolbar: BrowserToolbar,
    private val pictureInPictureUseCases: PictureInPictureUseCases,
    private val customTabId: String?,
) : AbstractBinding<BrowserState>(store) {
    private val hasSystemFeature by lazy {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
    }
    private var hasMedia = false
    private var isInWindow = false

    private val pictureInPictureButton: BrowserToolbar.ToggleButton =
        BrowserToolbar.ToggleButton(
            image = ContextCompat.getDrawable(context, iconsR.drawable.mozac_ic_picture_in_picture_24)!!,
            imageSelected = ContextCompat.getDrawable(context, iconsR.drawable.mozac_ic_picture_in_picture_close_24)!!,
            contentDescription = context.getString(R.string.pip_open),
            contentDescriptionSelected = context.getString(R.string.pip_close),
            visible = { hasSystemFeature && hasMedia },
        ) { enabled ->
            if (enabled) {
                store.state.findTabOrCustomTabOrSelectedTab(customTabId)?.let {
                    pictureInPictureUseCases.request(it.id)
                }
            } else {
                pictureInPictureUseCases.closeWindow()
            }
            // The button follows the window in the store, not the tap. The request can fail if the page has no video.
            pictureInPictureButton.setSelected(isInWindow, notifyListener = false)
        }

    init {
        toolbar.addPageAction(pictureInPictureButton)
    }

    override suspend fun onState(flow: Flow<BrowserState>) {
        flow
            .map { state ->
                val tab = state.findTabOrCustomTabOrSelectedTab(customTabId)
                // Requires Bug 2092305 to fix audio-only media.
                val tabHasMedia = tab?.mediaSessionState != null
                val tabIsInWindow = tab != null && state.pictureInPictureWindow.session?.tabId == tab.id
                tabHasMedia to tabIsInWindow
            }
            .distinctUntilChanged()
            .collect { (tabHasMedia, tabIsInWindow) ->
                hasMedia = tabHasMedia
                isInWindow = tabIsInWindow
                pictureInPictureButton.setSelected(tabIsInWindow, notifyListener = false)
                toolbar.invalidateActions()
            }
    }
}
