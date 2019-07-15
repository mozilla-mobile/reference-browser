/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_browser.*
import mozilla.components.support.base.feature.BackHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import org.mozilla.reference.browser.ext.requireComponents

/**
 * Fragment used for browsing within an external app, such as for custom tabs and PWAs.
 */
class ExternalAppBrowserFragment : BaseBrowserFragment(), BackHandler {
    private val customTabsIntegration = ViewBoundFeatureWrapper<CustomTabsIntegration>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customTabsIntegration.set(
            feature = CustomTabsIntegration(
                requireContext(),
                requireComponents.core.sessionManager,
                toolbar,
                engineView,
                requireComponents.useCases.sessionUseCases,
                sessionId!!,
                activity
            ),
            owner = this,
            view = view
        )
    }

    /**
     * Calls [onBackPressed] for features in the base class first,
     * before trying to call the custom tab [BackHandler].
     */
    override fun onBackPressed(): Boolean =
        super.onBackPressed() || customTabsIntegration.onBackPressed()

    companion object {
        fun create(sessionId: String) = ExternalAppBrowserFragment().apply {
            arguments = Bundle().apply {
                putSessionId(sessionId)
            }
        }
    }
}
