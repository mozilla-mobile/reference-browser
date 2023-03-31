/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.content.Context
import android.view.View
import androidx.fragment.app.FragmentManager
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.EngineView
import mozilla.components.feature.contextmenu.ContextMenuCandidate
import mozilla.components.feature.contextmenu.ContextMenuCandidate.Companion.createCopyImageLocationCandidate
import mozilla.components.feature.contextmenu.ContextMenuCandidate.Companion.createCopyLinkCandidate
import mozilla.components.feature.contextmenu.ContextMenuCandidate.Companion.createOpenImageInNewTabCandidate
import mozilla.components.feature.contextmenu.ContextMenuCandidate.Companion.createSaveImageCandidate
import mozilla.components.feature.contextmenu.ContextMenuCandidate.Companion.createShareLinkCandidate
import mozilla.components.feature.contextmenu.ContextMenuFeature
import mozilla.components.feature.contextmenu.ContextMenuUseCases
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.ui.widgets.DefaultSnackbarDelegate

@Suppress("LongParameterList")
class ContextMenuIntegration(
    context: Context,
    fragmentManager: FragmentManager,
    browserStore: BrowserStore,
    tabsUseCases: TabsUseCases,
    contextMenuUseCases: ContextMenuUseCases,
    engineView: EngineView,
    parentView: View,
    sessionId: String? = null,
) : LifecycleAwareFeature {

    private val candidates = run {
        if (sessionId != null) {
            val snackbarDelegate = DefaultSnackbarDelegate()
            listOf(
                createCopyLinkCandidate(context, parentView, snackbarDelegate),
                createShareLinkCandidate(context),
                createOpenImageInNewTabCandidate(context, tabsUseCases, parentView, snackbarDelegate),
                createSaveImageCandidate(context, contextMenuUseCases),
                createCopyImageLocationCandidate(context, parentView, snackbarDelegate),
            )
        } else {
            ContextMenuCandidate.defaultCandidates(context, tabsUseCases, contextMenuUseCases, parentView)
        }
    }

    private val feature = ContextMenuFeature(
        fragmentManager,
        browserStore,
        candidates,
        engineView,
        contextMenuUseCases,
    )

    override fun start() {
        feature.start()
    }

    override fun stop() {
        feature.stop()
    }
}
