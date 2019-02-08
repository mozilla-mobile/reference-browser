/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.content.Context
import android.view.View
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.fragment_browser.view.*
import mozilla.components.browser.session.SessionManager
import mozilla.components.feature.contextmenu.ContextMenuCandidate
import mozilla.components.feature.contextmenu.ContextMenuFeature
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.support.base.feature.LifecycleAwareFeature

class ContextMenuIntegration(
    context: Context,
    fragmentManager: FragmentManager,
    sessionManager: SessionManager,
    tabsUseCases: TabsUseCases,
    parentView: View,
    sessionId: String? = null
) : LifecycleAwareFeature {
    private val feature = ContextMenuFeature(fragmentManager, sessionManager,
        ContextMenuCandidate.defaultCandidates(context, tabsUseCases, parentView), parentView.engineView, sessionId)

    override fun start() {
        feature.start()
    }

    override fun stop() {
        feature.stop()
    }
}
