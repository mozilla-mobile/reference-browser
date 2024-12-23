/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.lib.state.ext.flow
import mozilla.components.support.base.log.logger.Logger

/**
 * ViewModel for [MainContainerFragment]. Holds state that is shared between MainContainerFragment
 * and it's children.
 *
 * @param browserStore The [BrowserStore] instance user to observe state.
 */
class MainContainerViewModel(
    private val browserStore: BrowserStore,
) : ViewModel() {

    private val logger = Logger("MainContainerViewModel")

    init {
        logger.debug("MainContainerViewModel created")
        observeSelectedTab()
    }

    private val _toolbarOffset = MutableStateFlow(0f)
    val toolbarOffset: StateFlow<Float> = _toolbarOffset.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    /**
     * Update the toolbar offset state. Call when the toolbar offset changes. Children fragments
     * can observe this state to update their UI based on the toolbar position.
     *
     * @param offset The new offset.
     */
    fun updateToolbarOffset(offset: Float) {
        _toolbarOffset.update { offset }
    }

    private fun observeSelectedTab() {
        viewModelScope.launch {
            browserStore.flow()
                .map { it.selectedTab }
                .distinctUntilChanged()
                .map { it?.content?.url }
                .distinctUntilChanged()
                .map {
                    logger.debug("Selected tab url: $it")
                    when (it) {
                        null, "about:blank" -> NavigationEvent.Home
                        else -> NavigationEvent.BrowserTab
                    }
                }
                .collect {
                    logger.debug("Navigation event: $it")
                    _navigationEvent.emit(it)
                }
        }
    }

    /**
     * Navigation events emitted by the ViewModel.
     */
    sealed interface NavigationEvent {
        /**
         * Navigation event for navigating to the browser tab.
         */
        data object BrowserTab : NavigationEvent

        /**
         * Navigation event for navigating to the home screen.
         */
        data object Home : NavigationEvent
    }

    /**
     * Factory for creating [MainContainerViewModel].
     *
     * @param browserStore The [BrowserStore] instance used to create [MainContainerViewModel].
     */
    class Factory(
        private val browserStore: BrowserStore,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainContainerViewModel(browserStore) as T
        }
    }
}
