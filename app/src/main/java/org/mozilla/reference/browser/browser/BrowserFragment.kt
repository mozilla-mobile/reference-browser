/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_browser.*
import kotlinx.android.synthetic.main.fragment_browser.view.*
import mozilla.components.browser.thumbnails.BrowserThumbnails
import mozilla.components.feature.awesomebar.AwesomeBarFeature
import mozilla.components.feature.awesomebar.provider.SearchSuggestionProvider
import mozilla.components.feature.search.ext.toDefaultSearchEngineProvider
import mozilla.components.feature.syncedtabs.SyncedTabsStorageSuggestionProvider
import mozilla.components.feature.tabs.toolbar.TabsToolbarFeature
import mozilla.components.feature.toolbar.WebExtensionToolbarFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.requireComponents
import org.mozilla.reference.browser.tabs.TabsTrayFragment

/**
 * Fragment used for browsing the web within the main app.
 */
class BrowserFragment : BaseBrowserFragment(), UserInteractionHandler {
    private val thumbnailsFeature = ViewBoundFeatureWrapper<BrowserThumbnails>()
    private val readerViewFeature = ViewBoundFeatureWrapper<ReaderViewIntegration>()
    private val webExtToolbarFeature = ViewBoundFeatureWrapper<WebExtensionToolbarFeature>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AwesomeBarFeature(awesomeBar, toolbar, engineView)
            .addSearchProvider(
                requireContext(),
                requireComponents.core.store.toDefaultSearchEngineProvider(),
                requireComponents.useCases.searchUseCases.defaultSearch,
                fetchClient = requireComponents.core.client,
                mode = SearchSuggestionProvider.Mode.MULTIPLE_SUGGESTIONS,
                engine = requireComponents.core.engine,
                filterExactMatch = true
            )
            .addSessionProvider(
                resources,
                requireComponents.core.store,
                requireComponents.useCases.tabsUseCases.selectTab)
            .addHistoryProvider(
                requireComponents.core.historyStorage,
                requireComponents.useCases.sessionUseCases.loadUrl)
            .addClipboardProvider(requireContext(), requireComponents.useCases.sessionUseCases.loadUrl)

        // We cannot really add a `addSyncedTabsProvider` to `AwesomeBarFeature` coz that would create
        // a dependency on feature-syncedtabs (which depends on Sync).
        awesomeBar.addProviders(
            SyncedTabsStorageSuggestionProvider(
                requireComponents.backgroundServices.syncedTabsStorage,
                requireComponents.useCases.tabsUseCases.addTab,
                requireComponents.core.icons
            )
        )

        TabsToolbarFeature(
            toolbar = toolbar,
            sessionId = sessionId,
            store = requireComponents.core.store,
            showTabs = ::showTabs,
            lifecycleOwner = this
        )

        thumbnailsFeature.set(
                feature = BrowserThumbnails(requireContext(),
                        engineView,
                        requireComponents.core.store),
                owner = this,
                view = view
        )

        readerViewFeature.set(
            feature = ReaderViewIntegration(
                requireContext(),
                requireComponents.core.engine,
                requireComponents.core.store,
                view.toolbar,
                view.readerViewBar,
                view.readerViewAppearanceButton
            ),
            owner = this,
            view = view
        )

        webExtToolbarFeature.set(
            feature = WebExtensionToolbarFeature(
                view.toolbar,
                requireContext().components.core.store
            ),
            owner = this,
            view = view
        )

        engineView.setDynamicToolbarMaxHeight(resources.getDimensionPixelSize(R.dimen.browser_toolbar_height))
    }

    private fun showTabs() {
        // For now we are performing manual fragment transactions here. Once we can use the new
        // navigation support library we may want to pass navigation graphs around.
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.container, TabsTrayFragment())
            commit()
        }
    }

    override fun onBackPressed(): Boolean =
        readerViewFeature.onBackPressed() || super.onBackPressed()

    companion object {
        fun create(sessionId: String? = null) = BrowserFragment().apply {
            arguments = Bundle().apply {
                putSessionId(sessionId)
            }
        }
    }
}
