/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.app.Activity
import android.content.Context
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.item.BrowserMenuItemToolbar
import mozilla.components.browser.menu.item.SimpleBrowserMenuItem
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.feature.customtabs.CustomTabsToolbarFeature
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.support.base.feature.BackHandler
import mozilla.components.support.base.feature.LifecycleAwareFeature
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.share

class CustomTabsIntegration(
    context: Context,
    sessionManager: SessionManager,
    toolbar: BrowserToolbar,
    sessionUseCases: SessionUseCases,
    sessionId: String,
    activity: Activity?
) : LifecycleAwareFeature, BackHandler {

    init {
        toolbar.urlBoxView = null
    }

    private val session = sessionManager.findSessionById(sessionId)

    private val menuToolbar by lazy {
        val forward = BrowserMenuItemToolbar.Button(
            mozilla.components.ui.icons.R.drawable.mozac_ic_forward,
            iconTintColorResource = R.color.icons,
            contentDescription = "Forward") {
            sessionUseCases.goForward.invoke(session)
        }

        val refresh = BrowserMenuItemToolbar.Button(
            mozilla.components.ui.icons.R.drawable.mozac_ic_refresh,
            iconTintColorResource = R.color.icons,
            contentDescription = "Refresh") {
            sessionUseCases.reload.invoke(session)
        }

        val stop = BrowserMenuItemToolbar.Button(
            mozilla.components.ui.icons.R.drawable.mozac_ic_stop,
            iconTintColorResource = R.color.icons,
            contentDescription = "Stop") {
            sessionUseCases.stopLoading.invoke(session)
        }

        BrowserMenuItemToolbar(listOf(forward, refresh, stop))
    }

    private val menuItems by lazy {
        listOf(
            menuToolbar,
            SimpleBrowserMenuItem("Share") {
                session?.url?.let { context.share(it) }
            }
        )
    }

    private val menuBuilder = BrowserMenuBuilder(menuItems)

    private val feature = CustomTabsToolbarFeature(
        sessionManager,
        toolbar,
        sessionId,
        menuBuilder,
        closeListener = { activity?.finish() })

    override fun start() {
        feature.start()
    }

    override fun stop() {
        feature.stop()
    }

    override fun onBackPressed(): Boolean {
        return feature.onBackPressed()
    }
}
