/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.compose.tabs

import androidx.compose.runtime.mutableStateOf
import mozilla.components.concept.tabstray.Tabs
import mozilla.components.concept.tabstray.TabsTray
import mozilla.components.feature.tabs.tabstray.TabsFeature
import mozilla.components.support.base.observer.Observable
import mozilla.components.support.base.observer.ObserverRegistry
import androidx.compose.runtime.MutableState as ComposeState

/**
 * An implementation of [TabsTray] that that can be use to [observe] from a Composable UI.
 *
 * See also [TabsFeature].
 */
class ComposableTrayAdapter(
    delegate: ObserverRegistry<TabsTray.Observer> = ObserverRegistry()
) : TabsTray, Observable<TabsTray.Observer> by delegate {

    val state = mutableStateOf(Tabs(emptyList(), 0))

    override fun updateTabs(tabs: Tabs) {
        state.value = tabs
    }

    fun observe(): ComposeState<Tabs> {
        return state
    }

    override fun isTabSelected(tabs: Tabs, position: Int) = false
    override fun onTabsChanged(position: Int, count: Int) = Unit
    override fun onTabsInserted(position: Int, count: Int) = Unit
    override fun onTabsMoved(fromPosition: Int, toPosition: Int) = Unit
    override fun onTabsRemoved(position: Int, count: Int) = Unit
}
