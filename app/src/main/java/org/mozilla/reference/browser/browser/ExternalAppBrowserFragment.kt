/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.fragment_browser.*
import mozilla.components.concept.engine.manifest.WebAppManifest
import mozilla.components.feature.customtabs.CustomTabWindowFeature
import mozilla.components.feature.pwa.ext.getWebAppManifest
import mozilla.components.feature.pwa.ext.putWebAppManifest
import mozilla.components.feature.pwa.feature.WebAppActivityFeature
import mozilla.components.feature.pwa.feature.WebAppHideToolbarFeature
import mozilla.components.feature.pwa.feature.WebAppSiteControlsFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.support.ktx.android.arch.lifecycle.addObservers
import org.mozilla.reference.browser.ext.requireComponents

/**
 * Fragment used for browsing within an external app, such as for custom tabs and PWAs.
 */
class ExternalAppBrowserFragment : BaseBrowserFragment(), UserInteractionHandler {
    private val customTabsIntegration = ViewBoundFeatureWrapper<CustomTabsIntegration>()
    private val windowFeature = ViewBoundFeatureWrapper<CustomTabWindowFeature>()
    private val hideToolbarFeature = ViewBoundFeatureWrapper<WebAppHideToolbarFeature>()

    private val manifest: WebAppManifest?
        get() = arguments?.getWebAppManifest()
    private val trustedScopes: List<Uri>
        get() = arguments?.getParcelableArrayList<Uri>(ARG_TRUSTED_SCOPES).orEmpty()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val manifest = this.manifest
        val sessionId = this.sessionId

        customTabsIntegration.set(
            feature = CustomTabsIntegration(
                requireContext(),
                requireComponents.core.sessionManager,
                requireComponents.core.store,
                toolbar,
                engineView,
                requireComponents.useCases.sessionUseCases,
                requireComponents.useCases.customTabsUseCases,
                sessionId!!,
                activity
            ),
            owner = this,
            view = view
        )

        windowFeature.set(
            feature = CustomTabWindowFeature(
                requireActivity(),
                requireComponents.core.store,
                sessionId
            ) { uri ->
                requireComponents.analytics.crashReporter.submitCaughtException(Exception("Unknown scheme error $uri"))
            },
            owner = this,
            view = view
        )

        hideToolbarFeature.set(
            feature = WebAppHideToolbarFeature(
                requireComponents.core.store,
                requireComponents.core.customTabsStore,
                sessionId,
                manifest
            ) { toolbarVisible ->
                toolbar.isVisible = toolbarVisible
                webAppToolbarShouldBeVisible = toolbarVisible
                if (!toolbarVisible) { engineView.setDynamicToolbarMaxHeight(0) }
            },
            owner = this,
            view = toolbar
        )

        if (manifest != null) {
            val activity = requireActivity()

            activity.lifecycle.addObservers(
                WebAppActivityFeature(
                    activity,
                    requireComponents.core.icons,
                    manifest
                ),
                WebAppSiteControlsFeature(
                    requireContext().applicationContext,
                    requireComponents.core.store,
                    requireComponents.useCases.sessionUseCases.reload,
                    sessionId,
                    manifest
                )
            )
        }
    }

    /**
     * Calls [onBackPressed] for features in the base class first,
     * before trying to call the custom tab [UserInteractionHandler].
     */
    override fun onBackPressed(): Boolean =
        super.onBackPressed() || customTabsIntegration.onBackPressed()

    companion object {
        private const val ARG_TRUSTED_SCOPES = "org.mozilla.samples.browser.TRUSTED_SCOPES"

        fun create(
            sessionId: String,
            manifest: WebAppManifest?,
            trustedScopes: List<Uri>
        ) = ExternalAppBrowserFragment().apply {
            arguments = Bundle().apply {
                putSessionId(sessionId)
                putWebAppManifest(manifest)
                putParcelableArrayList(ARG_TRUSTED_SCOPES, ArrayList(trustedScopes))
            }
        }
    }
}
