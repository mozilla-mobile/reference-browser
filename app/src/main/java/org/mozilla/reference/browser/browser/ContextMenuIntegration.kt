/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.content.Context
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import mozilla.components.browser.session.SessionManager
import mozilla.components.feature.contextmenu.ContextMenuCandidate
import mozilla.components.feature.contextmenu.ContextMenuFeature
import mozilla.components.feature.tabs.TabsUseCases

class ContextMenuIntegration(
    context: Context,
    fragmentManager: FragmentManager,
    sessionManager: SessionManager,
    tabsUseCases: TabsUseCases,
    snackbarParentView: View
) : LifecycleObserver {
    private val feature = ContextMenuFeature(fragmentManager, sessionManager,
        ContextMenuCandidate.defaultCandidates(context, tabsUseCases, snackbarParentView))

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        feature.start()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        feature.stop()
    }
}
