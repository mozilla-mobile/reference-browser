/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabs

import android.content.Context
import android.util.AttributeSet
import mozilla.components.feature.tabs.tabstray.TabsFeature
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components

class TabsToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : androidx.appcompat.widget.Toolbar(context, attrs) {
    private var tabsFeature: TabsFeature? = null
    private var isPrivateTray = false
    private var closeTabsTray: (() -> Unit)? = null

    init {
        navigationContentDescription = "back"
        setNavigationIcon(R.drawable.mozac_ic_back)
        setNavigationOnClickListener {
            closeTabsTray?.invoke()
        }
        inflateMenu(R.menu.tabstray_menu)
        setOnMenuItemClickListener {
            val tabsUseCases = components.useCases.tabsUseCases
            when (it.itemId) {
                R.id.newTab -> {
                    when (isPrivateTray) {
                        true -> tabsUseCases.addTab.invoke("about:privatebrowsing", selectTab = true, private = true)
                        false -> tabsUseCases.addTab.invoke("about:blank", selectTab = true)
                    }
                    closeTabsTray?.invoke()
                }
                R.id.closeTab -> {
                    when (isPrivateTray) {
                        true -> tabsUseCases.removePrivateTabs.invoke()
                        false -> tabsUseCases.removeNormalTabs.invoke()
                    }
                }
            }
            true
        }
    }

    fun initialize(tabsFeature: TabsFeature?, closeTabsTray: () -> Unit) {
        this.tabsFeature = tabsFeature
        this.closeTabsTray = closeTabsTray
    }

    fun updateToolbar(isPrivate: Boolean) {
        // Store the state for the menu option
        isPrivateTray = isPrivate

        // Update the menu option text
        menu.findItem(R.id.closeTab).title = if (isPrivate) {
            context.getString(R.string.menu_action_close_tabs_private)
        } else {
            context.getString(R.string.menu_action_close_tabs)
        }
    }

    private val components = context.components
}
