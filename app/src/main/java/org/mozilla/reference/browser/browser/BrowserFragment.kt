/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.content.Context
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
import mozilla.components.feature.findinpage.view.FindInPageView
import mozilla.components.feature.prompts.PromptFeature
import mozilla.components.feature.session.FullScreenFeature
import mozilla.components.feature.session.SessionFeature
import mozilla.components.feature.tabs.toolbar.TabsToolbarFeature
import mozilla.components.support.base.feature.BackHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.support.ktx.android.view.enterToImmersiveMode
import mozilla.components.support.ktx.android.view.exitImmersiveModeIfNeeded
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.UserInteractionHandler
import org.mozilla.reference.browser.ext.requireComponents
import org.mozilla.reference.browser.pip.PictureInPictureFeature
import org.mozilla.reference.browser.tabs.TabsTrayFragment

@Suppress("TooManyFunctions")
class BrowserFragment : Fragment(), BackHandler, UserInteractionHandler {
    private val sessionFeature = ViewBoundFeatureWrapper<SessionFeature>()
    private val toolbarIntegration = ViewBoundFeatureWrapper<ToolbarIntegration>()
    private val contextMenuIntegration = ViewBoundFeatureWrapper<ContextMenuIntegration>()
    private val downloadsFeature = ViewBoundFeatureWrapper<DownloadsFeature>()
    private val promptsFeature = ViewBoundFeatureWrapper<PromptFeature>()
    private val fullScreenFeature = ViewBoundFeatureWrapper<FullScreenFeature>()
    private val customTabsToolbarFeature = ViewBoundFeatureWrapper<CustomTabsToolbarFeature>()
    private val findInPageIntegration = ViewBoundFeatureWrapper<FindInPageIntegration>()

    private val backButtonHandler: List<ViewBoundFeatureWrapper<*>> = listOf(
        fullScreenFeature,
        findInPageIntegration,
        toolbarIntegration,
        sessionFeature,
        customTabsToolbarFeature
    )

    private lateinit var pipFeature: PictureInPictureFeature

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        pipFeature = PictureInPictureFeature(requireComponents.core.sessionManager, requireActivity(), ::pipModeChanged)
    }

    private val sessionId: String?
        get() = arguments?.getString(SESSION_ID)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_browser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sessionFeature.set(
            feature = SessionFeature(
                requireComponents.core.sessionManager,
                requireComponents.useCases.sessionUseCases,
                engineView,
                sessionId),
            owner = this,
            view = view)

        toolbarIntegration.set(
            feature = ToolbarIntegration(
                requireContext(),
                toolbar,
                requireComponents.core.historyStorage,
                requireComponents.toolbar.shippedDomainsProvider,
                sessionId),
            owner = this,
            view = view)

        contextMenuIntegration.set(
            feature = ContextMenuIntegration(
                requireContext(),
                requireFragmentManager(),
                requireComponents.core.sessionManager,
                requireComponents.useCases.tabsUseCases,
                view,
                sessionId),
            owner = this,
            view = view)

        AwesomeBarFeature(awesomeBar, toolbar, engineView)
            .addSearchProvider(
                requireComponents.search.searchEngineManager.getDefaultSearchEngine(requireContext()),
                requireComponents.useCases.searchUseCases.defaultSearch,
                requireComponents.core.client)
            .addSessionProvider(
                requireComponents.core.sessionManager,
                requireComponents.useCases.tabsUseCases.selectTab)
            .addHistoryProvider(
                requireComponents.core.historyStorage,
                requireComponents.useCases.sessionUseCases.loadUrl)
            .addClipboardProvider(requireContext(), requireComponents.useCases.sessionUseCases.loadUrl)

        TabsToolbarFeature(
            toolbar = toolbar,
            sessionId = sessionId,
            sessionManager = requireComponents.core.sessionManager,
            showTabs = ::showTabs)

        downloadsFeature.set(
            feature = DownloadsFeature(
                requireContext(),
                sessionManager = requireComponents.core.sessionManager,
                sessionId = sessionId,
                fragmentManager = childFragmentManager,
                onNeedToRequestPermissions = { permissions ->
                    requestPermissions(permissions, REQUEST_CODE_DOWNLOAD_PERMISSIONS)
                }),
            owner = this,
            view = view)

        promptsFeature.set(
            feature = PromptFeature(
                fragment = this,
                sessionManager = requireComponents.core.sessionManager,
                fragmentManager = requireFragmentManager(),
                onNeedToRequestPermissions = { permissions ->
                    requestPermissions(permissions, REQUEST_CODE_PROMPT_PERMISSIONS)
                }),
            owner = this,
            view = view)

        fullScreenFeature.set(
            feature = FullScreenFeature(
                requireComponents.core.sessionManager,
                requireComponents.useCases.sessionUseCases,
                sessionId, ::fullScreenChanged),
            owner = this,
            view = view)

        customTabsToolbarFeature.set(
            feature = CustomTabsToolbarFeature(
                requireComponents.core.sessionManager,
                toolbar,
                sessionId,
                requireComponents.toolbar.menuBuilder,
                closeListener = { activity?.finish() }),
            owner = this,
            view = view)

        findInPageIntegration.set(
            feature = FindInPageIntegration(
                requireComponents.core.sessionManager,
                findInPageBar as FindInPageView),
            owner = this,
            view = view)
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
        return backButtonHandler.firstOrNull { it.onBackPressed() } != null
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
            REQUEST_CODE_DOWNLOAD_PERMISSIONS -> downloadsFeature.withFeature {
                it.onPermissionsResult(permissions, grantResults)
            }
            REQUEST_CODE_PROMPT_PERMISSIONS -> promptsFeature.withFeature {
                it.onPermissionsResult(permissions, grantResults)
            }
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
        promptsFeature.withFeature { it.onActivityResult(requestCode, resultCode, data) }
    }
}
