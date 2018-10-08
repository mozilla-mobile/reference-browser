/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import mozilla.components.browser.domains.DomainAutoCompleteProvider
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.feature.toolbar.ToolbarFeature
import mozilla.components.ui.autocomplete.InlineAutocompleteEditText
import org.mozilla.reference.browser.ext.components

class ToolbarIntegration(
    context: Context,
    toolbar: BrowserToolbar,
    sessionId: String? = null
) : LifecycleObserver {
    private val autoCompleteProvider = DomainAutoCompleteProvider().apply {
        initialize(context)
    }

    init {
        toolbar.setMenuBuilder(context.components.menuBuilder)

        toolbar.setAutocompleteFilter { value, view ->
            view?.let { _ ->
                val result = autoCompleteProvider.autocomplete(value)
                view.applyAutocompleteResult(
                    InlineAutocompleteEditText.AutocompleteResult(result.text, result.source, result.size) {
                        result.url
                    }
                )
            }
        }
    }

    private val toolbarFeature: ToolbarFeature = ToolbarFeature(
        toolbar,
        context.components.sessionManager,
        context.components.sessionUseCases.loadUrl,
        context.components.defaultSearchUseCase,
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
