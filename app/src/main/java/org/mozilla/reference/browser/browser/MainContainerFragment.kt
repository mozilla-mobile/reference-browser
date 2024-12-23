/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnDrawListener
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.feature.awesomebar.AwesomeBarFeature
import mozilla.components.feature.awesomebar.provider.SearchSuggestionProvider
import mozilla.components.feature.readerview.view.ReaderViewControlsBar
import mozilla.components.feature.syncedtabs.SyncedTabsStorageSuggestionProvider
import mozilla.components.feature.tabs.toolbar.TabsToolbarFeature
import mozilla.components.feature.toolbar.WebExtensionToolbarFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.support.base.log.logger.Logger
import mozilla.components.ui.widgets.behavior.EngineViewScrollingBehavior
import mozilla.components.ui.widgets.behavior.ViewPosition
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.requireComponents
import org.mozilla.reference.browser.search.AwesomeBarWrapper
import org.mozilla.reference.browser.tabs.TabsTrayFragment

/**
 * The container fragment for the browser. This fragment is responsible for setting up the chrome,
 * handling navigation events to transition between BrowserFragment and HomeFragment.
 */
class MainContainerFragment : Fragment(), UserInteractionHandler {

    private val logger = Logger("MainContainerFragment")

    private val viewModel by viewModels<MainContainerViewModel>(
        factoryProducer = { MainContainerViewModel.Factory(requireComponents.core.store) },
    )

    // Views
    private val awesomeBar: AwesomeBarWrapper
        get() = requireView().findViewById(R.id.awesomeBar)
    private val toolbar: BrowserToolbar
        get() = requireView().findViewById(R.id.toolbar)
    private val readerViewBar: ReaderViewControlsBar
        get() = requireView().findViewById(R.id.readerViewBar)
    private val readerViewAppearanceButton: FloatingActionButton
        get() = requireView().findViewById(R.id.readerViewAppearanceButton)

    // Features
    private val webExtToolbarFeature = ViewBoundFeatureWrapper<WebExtensionToolbarFeature>()
    private val toolbarIntegration = ViewBoundFeatureWrapper<ToolbarIntegration>()
    private val readerViewFeature = ViewBoundFeatureWrapper<ReaderViewIntegration>()

    private val backButtonHandlers: List<ViewBoundFeatureWrapper<*>> = listOf(
        readerViewFeature,
        toolbarIntegration,
    )

    private val translationYOnDrawFetcher by lazy {
        TranslationYOnDrawFetcher(
            toolbar,
            onDraw = { offset -> viewModel.updateToolbarOffset(offset) }
        )
    }

    private val sessionId: String? by lazy { arguments?.getString(SESSION_ID) }

    private val navHost by lazy {
        childFragmentManager.findFragmentById(R.id.container) as NavHostFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_main_container, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(view)
        setupAwesomeBar()
        setupTabsToolbarFeature()
        setupWebExtToolbarFeature(view)
        setupReaderMode(view)

        setupFragmentResultListener()
        observeNavigationEvents()
    }

    override fun onBackPressed(): Boolean {
        logger.debug("onBackPressed")
        // Checks if any of the features handled the back press
        // and if any child fragments that are UserInteractionHandlers handled it
        return backButtonHandlers.any { it.onBackPressed() } ||
                navHost.childFragmentManager.fragments
                    .filterIsInstance<UserInteractionHandler>()
                    .any {
                        it.onBackPressed()
                    }
    }

    override fun onDestroyView() {
        toolbar.viewTreeObserver.removeOnDrawListener(translationYOnDrawFetcher)
        super.onDestroyView()
    }

    private fun setupToolbar(view: View) {
        toolbar.viewTreeObserver.addOnDrawListener(translationYOnDrawFetcher)

        //        toolbar.viewTreeObserver.addOnPreDrawListener {
        //            val currentTranslationY = toolbar.translationY
        //            viewModel.updateToolbarOffset(currentTranslationY)
        //            logger.debug("onPreDraw: $currentTranslationY")
        //            true
        //        }

        (toolbar.layoutParams as? CoordinatorLayout.LayoutParams)?.apply {
            behavior = EngineViewScrollingBehavior(
                view.context,
                null,
                ViewPosition.BOTTOM,
            )
        }

        toolbarIntegration.set(
            feature = ToolbarIntegration(
                requireContext(),
                toolbar,
                requireComponents.core.historyStorage,
                requireComponents.core.store,
                requireComponents.useCases.sessionUseCases,
                requireComponents.useCases.tabsUseCases,
                requireComponents.useCases.webAppUseCases,
                sessionId,
            ),
            owner = this,
            view = view,
        )
    }

    private fun setupAwesomeBar() {
        // EngineView was passed to AwesomeBarFeature to update it's visibility,
        // but it seems odd that awesome bar needs to know about engine view, think about decoupling this
        // maybe using the same approach as Home/BrowserTab
        AwesomeBarFeature(awesomeBar, toolbar, null)
            .addSearchProvider(
                requireContext(),
                requireComponents.core.store,
                requireComponents.useCases.searchUseCases.defaultSearch,
                fetchClient = requireComponents.core.client,
                mode = SearchSuggestionProvider.Mode.MULTIPLE_SUGGESTIONS,
                engine = requireComponents.core.engine,
                limit = 5,
                filterExactMatch = true,
            )
            .addSessionProvider(
                resources,
                requireComponents.core.store,
                requireComponents.useCases.tabsUseCases.selectTab,
            )
            .addHistoryProvider(
                requireComponents.core.historyStorage,
                requireComponents.useCases.sessionUseCases.loadUrl,
            )
            .addClipboardProvider(
                requireContext(),
                requireComponents.useCases.sessionUseCases.loadUrl
            )

        // We cannot really add a `addSyncedTabsProvider` to `AwesomeBarFeature` coz that would create
        // a dependency on feature-syncedtabs (which depends on Sync).
        awesomeBar.addProviders(
            SyncedTabsStorageSuggestionProvider(
                requireComponents.backgroundServices.syncedTabsStorage,
                requireComponents.useCases.tabsUseCases.addTab,
                requireComponents.core.icons,
            ),
        )
    }

    private fun setupTabsToolbarFeature() {
        TabsToolbarFeature(
            toolbar = toolbar,
            sessionId = sessionId,
            store = requireComponents.core.store,
            showTabs = ::showTabs,
            lifecycleOwner = this,
        )
    }

    private fun setupWebExtToolbarFeature(view: View) {
        webExtToolbarFeature.set(
            feature = WebExtensionToolbarFeature(
                toolbar,
                requireContext().components.core.store,
            ),
            owner = this,
            view = view,
        )
    }

    private fun setupReaderMode(view: View) {
        readerViewFeature.set(
            feature = ReaderViewIntegration(
                requireContext(),
                requireComponents.core.engine,
                requireComponents.core.store,
                toolbar,
                readerViewBar,
                readerViewAppearanceButton,
            ),
            owner = this,
            view = view,
        )
    }

    private fun observeNavigationEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { event ->
                    logger.debug("Navigation event: $event")
                    logger.debug("Current destination: ${navHost.navController.currentDestination}")
                    when (event) {
                        MainContainerViewModel.NavigationEvent.BrowserTab -> {
                            navHost.navController.navigate(R.id.browserFragment)
                        }

                        MainContainerViewModel.NavigationEvent.Home -> {
                            navHost.navController.navigate(R.id.homeFragment)
                        }
                    }
                }
            }
        }
    }

    /**
     * Fetches the translationY of the toolbar on every draw event.
     *
     * @param view The view whose translationY is to be fetched.
     * @param onDraw The callback to be called on every draw event with the translationY.
     */
    class TranslationYOnDrawFetcher(
        private val view: View,
        private val onDraw: (Float) -> Unit,
    ) : OnDrawListener {

        private val logger: Logger = Logger("ToolbarOffsetListener")

        override fun onDraw() {
            val currentTranslationY = view.translationY
            logger.debug("onDraw: $currentTranslationY")
            onDraw(currentTranslationY)
        }
    }

    private fun setupFragmentResultListener() {
        childFragmentManager.setFragmentResultListener(
            BROWSER_TO_MAIN_FRAGMENT_RESULT_KEY,
            this,
        ) { _, bundle ->
            val isFullScreen = bundle.getBoolean(FULL_SCREEN_MODE_CHANGED, false)
            toolbar.isVisible = !isFullScreen
        }
    }

    private fun showTabs() {
        // For now we are performing manual fragment transactions here. Once we can use the new
        // navigation support library we may want to pass navigation graphs around.
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.container, TabsTrayFragment())
            commit()
        }
    }

    /**
     * @see [MainContainerFragment]
     */
    companion object {
        @JvmStatic
        private fun Bundle.putSessionId(sessionId: String?) {
            putString(SESSION_ID, sessionId)
        }

        /**
         * Create a new instance of [MainContainerFragment].
         *
         * @param sessionId The session id to use for the browser.
         */
        fun create(sessionId: String? = null) = MainContainerFragment().apply {
            arguments = Bundle().apply {
                putSessionId(sessionId)
            }
        }
    }
}
