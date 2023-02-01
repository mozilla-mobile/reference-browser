/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import mozilla.components.browser.domains.autocomplete.ShippedDomainsProvider
import mozilla.components.browser.menu2.BrowserMenuController
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.state.SessionState
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.storage.sync.PlacesHistoryStorage
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.browser.toolbar.display.DisplayToolbar
import mozilla.components.concept.menu.MenuController
import mozilla.components.concept.menu.candidate.CompoundMenuCandidate
import mozilla.components.concept.menu.candidate.ContainerStyle
import mozilla.components.concept.menu.candidate.DrawableMenuIcon
import mozilla.components.concept.menu.candidate.MenuCandidate
import mozilla.components.concept.menu.candidate.RowMenuCandidate
import mozilla.components.concept.menu.candidate.SmallMenuCandidate
import mozilla.components.concept.menu.candidate.TextMenuCandidate
import mozilla.components.feature.pwa.WebAppUseCases
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.feature.toolbar.ToolbarAutocompleteFeature
import mozilla.components.feature.toolbar.ToolbarFeature
import mozilla.components.lib.state.ext.flow
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.ktx.kotlinx.coroutines.flow.ifChanged
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.addons.AddonsActivity
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.share
import org.mozilla.reference.browser.settings.SettingsActivity
import org.mozilla.reference.browser.tabs.synced.SyncedTabsActivity

@Suppress("LongParameterList")
class ToolbarIntegration(
    private val context: Context,
    toolbar: BrowserToolbar,
    historyStorage: PlacesHistoryStorage,
    store: BrowserStore,
    private val sessionUseCases: SessionUseCases,
    private val tabsUseCases: TabsUseCases,
    private val webAppUseCases: WebAppUseCases,
    sessionId: String? = null,
) : LifecycleAwareFeature, UserInteractionHandler {
    private val shippedDomainsProvider = ShippedDomainsProvider().also {
        it.initialize(context)
    }

    private val scope = MainScope()

    private fun menuToolbar(session: SessionState?): RowMenuCandidate {
        val tint = ContextCompat.getColor(context, R.color.icons)

        val forward = SmallMenuCandidate(
            contentDescription = "Forward",
            icon = DrawableMenuIcon(
                context,
                mozilla.components.ui.icons.R.drawable.mozac_ic_forward,
                tint = tint,
            ),
            containerStyle = ContainerStyle(
                isEnabled = session?.content?.canGoForward == true,
            ),
        ) {
            sessionUseCases.goForward.invoke()
        }

        val refresh = SmallMenuCandidate(
            contentDescription = "Refresh",
            icon = DrawableMenuIcon(
                context,
                mozilla.components.ui.icons.R.drawable.mozac_ic_refresh,
                tint = tint,
            ),
        ) {
            sessionUseCases.reload.invoke()
        }

        val stop = SmallMenuCandidate(
            contentDescription = "Stop",
            icon = DrawableMenuIcon(
                context,
                mozilla.components.ui.icons.R.drawable.mozac_ic_stop,
                tint = tint,
            ),
        ) {
            sessionUseCases.stopLoading.invoke()
        }

        return RowMenuCandidate(listOf(forward, refresh, stop))
    }

    private fun sessionMenuItems(sessionState: SessionState): List<MenuCandidate> {
        return listOfNotNull(
            menuToolbar(sessionState),

            TextMenuCandidate("Share") {
                val url = sessionState.content.url
                context.share(url)
            },

            CompoundMenuCandidate(
                text = "Request desktop site",
                isChecked = sessionState.content.desktopMode,
                end = CompoundMenuCandidate.ButtonType.SWITCH,
            ) { checked ->
                sessionUseCases.requestDesktopSite.invoke(checked)
            },

            if (webAppUseCases.isPinningSupported()) {
                TextMenuCandidate(
                    text = "Add to homescreen",
                    containerStyle = ContainerStyle(
                        isVisible = webAppUseCases.isPinningSupported(),
                    ),
                ) {
                    scope.launch { webAppUseCases.addToHomescreen() }
                }
            } else {
                null
            },

            TextMenuCandidate(
                text = "Find in Page",
            ) {
                FindInPageIntegration.launch?.invoke()
            },
        )
    }

    private fun menuItems(sessionState: SessionState?): List<MenuCandidate> {
        val sessionMenuItems = if (sessionState != null) {
            sessionMenuItems(sessionState)
        } else {
            emptyList()
        }

        return sessionMenuItems + listOf(
            TextMenuCandidate(text = "Add-ons") {
                val intent = Intent(context, AddonsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            },

            TextMenuCandidate(text = "Synced Tabs") {
                val intent = Intent(context, SyncedTabsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            },

            TextMenuCandidate(text = "Report issue") {
                tabsUseCases.addTab(
                    url = "https://github.com/mozilla-mobile/reference-browser/issues/new",
                )
            },

            TextMenuCandidate(text = "Settings") {
                val intent = Intent(context, SettingsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            },
        )
    }

    private val menuController: MenuController = BrowserMenuController()

    init {
        toolbar.display.indicators = listOf(
            DisplayToolbar.Indicators.SECURITY,
            DisplayToolbar.Indicators.TRACKING_PROTECTION,
        )
        toolbar.display.displayIndicatorSeparator = true
        toolbar.display.menuController = menuController

        toolbar.display.hint = context.getString(R.string.toolbar_hint)
        toolbar.edit.hint = context.getString(R.string.toolbar_hint)

        ToolbarAutocompleteFeature(toolbar).apply {
            updateAutocompleteProviders(
                listOf(historyStorage, shippedDomainsProvider),
            )
        }

        toolbar.display.setUrlBackground(
            ResourcesCompat.getDrawable(context.resources, R.drawable.url_background, context.theme),
        )

        scope.launch {
            store.flow()
                .map { state -> state.selectedTab }
                .ifChanged()
                .collect { tab ->
                    menuController.submitList(menuItems(tab))
                }
        }
    }

    private val toolbarFeature: ToolbarFeature = ToolbarFeature(
        toolbar,
        context.components.core.store,
        context.components.useCases.sessionUseCases.loadUrl,
        { searchTerms ->
            context.components.useCases.searchUseCases.defaultSearch.invoke(
                searchTerms = searchTerms,
                searchEngine = null,
                parentSessionId = null,
            )
        },
        sessionId,
    )

    override fun start() {
        toolbarFeature.start()
    }

    override fun stop() {
        toolbarFeature.stop()
    }

    override fun onBackPressed(): Boolean {
        return toolbarFeature.onBackPressed()
    }
}
