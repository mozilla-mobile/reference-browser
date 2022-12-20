/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import mozilla.components.browser.menu2.BrowserMenuController
import mozilla.components.browser.state.selector.findCustomTab
import mozilla.components.browser.state.state.CustomTabSessionState
import mozilla.components.browser.state.state.SessionState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.concept.engine.EngineView
import mozilla.components.concept.menu.MenuController
import mozilla.components.concept.menu.candidate.CompoundMenuCandidate
import mozilla.components.concept.menu.candidate.DrawableMenuIcon
import mozilla.components.concept.menu.candidate.MenuCandidate
import mozilla.components.concept.menu.candidate.RowMenuCandidate
import mozilla.components.concept.menu.candidate.SmallMenuCandidate
import mozilla.components.concept.menu.candidate.TextMenuCandidate
import mozilla.components.feature.customtabs.CustomTabsToolbarFeature
import mozilla.components.feature.customtabs.menu.createCustomTabMenuCandidates
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.tabs.CustomTabsUseCases
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged
import org.mozilla.reference.browser.BrowserActivity
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.share

@Suppress("LongParameterList")
class CustomTabsIntegration(
    private val context: Context,
    store: BrowserStore,
    toolbar: BrowserToolbar,
    private val engineView: EngineView,
    private val sessionUseCases: SessionUseCases,
    private val customTabsUseCases: CustomTabsUseCases,
    sessionId: String,
    private val activity: Activity?,
) : LifecycleAwareFeature, UserInteractionHandler {

    private val session = store.state.findCustomTab(sessionId)
    private val logger = Logger("CustomTabsIntegration")

    init {
        if (session == null) {
            logger.warn("The session for this ID, no longer exists. Finishing activity.")
            activity?.finish()
        }
        toolbar.display.setUrlBackground(null)
    }

    private fun menuToolbar(session: CustomTabSessionState?): RowMenuCandidate {
        val tint = ContextCompat.getColor(context, R.color.icons)
        val tabId = session?.id

        val forward = SmallMenuCandidate(
            contentDescription = "Forward",
            icon = DrawableMenuIcon(
                context,
                mozilla.components.ui.icons.R.drawable.mozac_ic_forward,
                tint = tint,
            ),
        ) {
            sessionUseCases.goForward.invoke(tabId)
        }

        val refresh = SmallMenuCandidate(
            contentDescription = "Refresh",
            icon = DrawableMenuIcon(
                context,
                mozilla.components.ui.icons.R.drawable.mozac_ic_refresh,
                tint = tint,
            ),
        ) {
            sessionUseCases.reload.invoke(tabId)
        }

        val stop = SmallMenuCandidate(
            contentDescription = "Stop",
            icon = DrawableMenuIcon(
                context,
                mozilla.components.ui.icons.R.drawable.mozac_ic_stop,
                tint = tint,
            ),
        ) {
            sessionUseCases.stopLoading.invoke(tabId)
        }

        return RowMenuCandidate(listOf(forward, refresh, stop))
    }

    private fun menuItems(sessionState: SessionState?): List<MenuCandidate> {
        return listOf(
            menuToolbar(session),

            TextMenuCandidate("Share") {
                val url = sessionState?.content?.url.orEmpty()
                context.share(url)
            },

            CompoundMenuCandidate(
                text = "Request desktop site",
                isChecked = sessionState?.content?.desktopMode == true,
                end = CompoundMenuCandidate.ButtonType.SWITCH,
            ) { checked ->
                sessionUseCases.requestDesktopSite.invoke(checked, sessionState?.id)
            },

            TextMenuCandidate("Find in Page") {
                FindInPageIntegration.launch?.invoke()
            },

            TextMenuCandidate("Open in Browser") {
                // Release the session from this view so that it can immediately be rendered by a different view
                engineView.release()

                // Stip the CustomTabConfig to turn this Session into a regular tab and then select it
                (sessionState as? CustomTabSessionState)?.let { session ->
                    customTabsUseCases.migrate(session.id)
                }

                // Close this activity since it is no longer displaying any session
                activity?.finish()

                // Now switch to the actual browser which should now display our new selected session
                val intent = Intent(context, BrowserActivity::class.java)
                context.startActivity(intent)
            },
        )
    }

    private val menuController: MenuController = BrowserMenuController()

    private val feature = CustomTabsToolbarFeature(
        store,
        toolbar,
        sessionId,
        customTabsUseCases,
        window = activity?.window,
        closeListener = { activity?.finish() },
    )

    init {
        toolbar.display.menuController = menuController

        store.flowScoped { flow ->
            flow.map { state -> state.findCustomTab(sessionId) }
                .ifChanged()
                .collect { tab ->
                    val items = menuItems(tab)
                    val customTabItems = tab?.createCustomTabMenuCandidates(context).orEmpty()
                    menuController.submitList(items + customTabItems)
                }
        }
    }

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
