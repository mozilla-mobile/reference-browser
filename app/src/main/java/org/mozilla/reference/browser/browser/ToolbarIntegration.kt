/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.feature.toolbar.ToolbarFeature
import org.mozilla.reference.browser.Components

class ToolbarIntegration(
    components: Components,
    toolbar: BrowserToolbar,
    sessionId: String? = null
) : LifecycleObserver {
    init {
        toolbar.setMenuBuilder(components.menuBuilder)
    }

    private val toolbarFeature: ToolbarFeature = ToolbarFeature(
        toolbar,
        components.sessionManager,
        components.sessionUseCases.loadUrl,
        components.defaultSearchUseCase,
        sessionId
    )

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        toolbarFeature.start()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        toolbarFeature.stop()
    }
}
