/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_browser.*
import mozilla.components.feature.awesomebar.AwesomeBarFeature
import mozilla.components.feature.customtabs.CustomTabsToolbarFeature
import mozilla.components.feature.downloads.DownloadsFeature
import mozilla.components.feature.prompts.PromptFeature
import mozilla.components.feature.session.FullScreenFeature
import mozilla.components.feature.session.SessionFeature
import mozilla.components.feature.tabs.toolbar.TabsToolbarFeature
import mozilla.components.support.ktx.android.arch.lifecycle.addObservers
import mozilla.components.support.ktx.android.view.enterToImmersiveMode
import mozilla.components.support.ktx.android.view.exitImmersiveModeIfNeeded
import org.mozilla.reference.browser.BackHandler
import org.mozilla.reference.browser.UserInteractionHandler
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.requireComponents
import org.mozilla.reference.browser.pip.PictureInPictureFeature
import org.mozilla.reference.browser.tabs.TabsTrayFragment

@Suppress("TooManyFunctions")
class BrowserFragment : Fragment(), BackHandler, UserInteractionHandler {
    private lateinit var sessionFeature: SessionFeature
    private lateinit var tabsToolbarFeature: TabsToolbarFeature
    private lateinit var downloadsFeature: DownloadsFeature
    private lateinit var awesomeBarFeature: AwesomeBarFeature
    private lateinit var promptsFeature: PromptFeature
    private lateinit var fullScreenFeature: FullScreenFeature
    private lateinit var pipFeature: PictureInPictureFeature
    private lateinit var customTabsToolbarFeature: CustomTabsToolbarFeature

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_browser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionId = arguments?.getString(SESSION_ID)

        sessionFeature = SessionFeature(
                requireComponents.core.sessionManager,
                requireComponents.useCases.sessionUseCases,
                engineView,
                sessionId)

        lifecycle.addObserver(ToolbarIntegration(
            requireContext(),
            toolbar,
            requireComponents.core.historyStorage,
            requireComponents.toolbar.shippedDomainsProvider,
            sessionId))

        lifecycle.addObserver(ContextMenuIntegration(
            requireContext(),
            requireFragmentManager(),
            requireComponents.core.sessionManager,
            requireComponents.useCases.tabsUseCases,
            view))

        awesomeBarFeature = AwesomeBarFeature(awesomeBar, toolbar, engineView)
            .addSearchProvider(
                requireComponents.search.searchEngineManager.getDefaultSearchEngine(requireContext()),
                requireComponents.useCases.searchUseCases.defaultSearch)
            .addSessionProvider(
                requireComponents.core.sessionManager,
                requireComponents.useCases.tabsUseCases.selectTab)
            .addHistoryProvider(
                requireComponents.core.historyStorage,
                requireComponents.useCases.sessionUseCases.loadUrl)
            .addClipboardProvider(requireContext(), requireComponents.useCases.sessionUseCases.loadUrl)

        tabsToolbarFeature = TabsToolbarFeature(
            toolbar = toolbar,
            sessionId = sessionId,
            sessionManager = requireComponents.core.sessionManager,
            showTabs = ::showTabs)

        downloadsFeature = DownloadsFeature(
                requireContext(),
                sessionManager = requireComponents.core.sessionManager,
                fragmentManager = childFragmentManager,
                onNeedToRequestPermissions = { permissions ->
                    requestPermissions(permissions, REQUEST_CODE_DOWNLOAD_PERMISSIONS)
                }
        )

        promptsFeature = PromptFeature(
            fragment = this,
            sessionManager = requireComponents.core.sessionManager,
            fragmentManager = requireFragmentManager(),
            onNeedToRequestPermissions = { permissions ->
                requestPermissions(permissions, REQUEST_CODE_PROMPT_PERMISSIONS)
            }
        )

        fullScreenFeature = FullScreenFeature(
            requireComponents.core.sessionManager,
            requireComponents.useCases.sessionUseCases,
            sessionId, ::fullScreenChanged
        )

        pipFeature = PictureInPictureFeature(requireComponents.core.sessionManager, requireActivity(), ::pipModeChanged)

        customTabsToolbarFeature = CustomTabsToolbarFeature(
            requireComponents.core.sessionManager,
            toolbar,
            sessionId,
            requireComponents.toolbar.menuBuilder
        ) { activity?.finish() }

        lifecycle.addObservers(
            sessionFeature,
            downloadsFeature,
            promptsFeature,
            fullScreenFeature,
            customTabsToolbarFeature)
    }

    private fun showTabs() {
        // For now we are performing manual fragment transactions here. Once we can use the new
        // navigation support library we may want to pass navigation graphs around.
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.container, TabsTrayFragment())
            commit()
        }
    }

    private fun fullScreenChanged(enabled: Boolean) {
        if (enabled) {
            activity?.enterToImmersiveMode()
            toolbar.visibility = View.GONE
        } else {
            activity?.exitImmersiveModeIfNeeded()
            toolbar.visibility = View.VISIBLE
        }
    }

    private fun pipModeChanged(enabled: Boolean) {
        val fullScreenMode = requireComponents.core.sessionManager.selectedSession?.fullScreenMode ?: false
        // If we're exiting PIP mode and we're in fullscreen mode, then we should exit fullscreen mode as well.
        if (!enabled && fullScreenMode) {
            onBackPressed()
            fullScreenChanged(false)
        }
    }

    @Suppress("ReturnCount")
    override fun onBackPressed(): Boolean {
        if (fullScreenFeature.onBackPressed()) {
            return true
        }

        if (toolbar.onBackPressed()) {
            return true
        }

        if (sessionFeature.handleBackPressed()) {
            return true
        }

        if (customTabsToolbarFeature.onBackPressed()) {
            return true
        }

        return false
    }

    override fun onHomePressed(): Boolean {
        if (pipFeature.onHomePressed()) {
            return true
        }
        return false
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        pipFeature.onPictureInPictureModeChanged(isInPictureInPictureMode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_DOWNLOAD_PERMISSIONS -> downloadsFeature.onPermissionsResult(permissions, grantResults)
            REQUEST_CODE_PROMPT_PERMISSIONS -> promptsFeature.onPermissionsResult(permissions, grantResults)
        }
    }

    companion object {
        private const val SESSION_ID = "session_id"
        private const val REQUEST_CODE_DOWNLOAD_PERMISSIONS = 1
        private const val REQUEST_CODE_PROMPT_PERMISSIONS = 2

        fun create(sessionId: String? = null): BrowserFragment = BrowserFragment().apply {
            arguments = Bundle().apply {
                putString(SESSION_ID, sessionId)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        promptsFeature.onActivityResult(requestCode, resultCode, data)
    }
}
