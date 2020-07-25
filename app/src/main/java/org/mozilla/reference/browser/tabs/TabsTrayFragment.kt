/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabs

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_tabstray.tabsPanel
import kotlinx.android.synthetic.main.fragment_tabstray.tabsTray
import kotlinx.android.synthetic.main.fragment_tabstray.tabsToolbar
import mozilla.components.browser.tabstray.DefaultTabViewHolder
import mozilla.components.browser.tabstray.TabsAdapter
import mozilla.components.browser.tabstray.TabsTrayStyling
import mozilla.components.browser.tabstray.ViewHolderProvider
import mozilla.components.browser.thumbnails.loader.ThumbnailLoader
import mozilla.components.concept.tabstray.TabsTray
import mozilla.components.feature.tabs.tabstray.TabsFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.browser.BrowserFragment
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.ext.requireComponents

/**
 * A fragment for displaying the tabs tray.
 */
class TabsTrayFragment : Fragment(), UserInteractionHandler {
    private var tabsFeature: TabsFeature? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_tabstray, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val trayAdapter = createAndSetupTabsTray(requireContext())

        tabsFeature = TabsFeature(
            trayAdapter,
            requireComponents.core.store,
            requireComponents.useCases.tabsUseCases.selectTab,
            requireComponents.useCases.tabsUseCases.removeTab,
            { !it.content.private },
            ::closeTabsTray)

        tabsPanel.initialize(tabsFeature, updateTabsToolbar = ::updateTabsToolbar)
        tabsToolbar.initialize(tabsFeature) { closeTabsTray() }
    }

    override fun onStart() {
        super.onStart()

        tabsFeature?.start()
    }

    override fun onStop() {
        super.onStop()

        tabsFeature?.stop()
    }

    override fun onBackPressed(): Boolean {
        closeTabsTray()
        return true
    }

    private fun closeTabsTray() {
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.container, BrowserFragment.create())
            commit()
        }
    }

    private fun updateTabsToolbar(isPrivate: Boolean) {
        tabsToolbar.updateToolbar(isPrivate)
    }

    private fun createAndSetupTabsTray(context: Context): TabsTray {
        val layoutManager = LinearLayoutManager(context)
        val thumbnailLoader = ThumbnailLoader(context.components.core.thumbnailStorage)
        val trayStyling = TabsTrayStyling(itemBackgroundColor = Color.TRANSPARENT, itemTextColor = Color.WHITE)
        val viewHolderProvider: ViewHolderProvider = { viewGroup ->
            val view = LayoutInflater.from(context)
                .inflate(R.layout.browser_tabstray_item, viewGroup, false)

            DefaultTabViewHolder(view, thumbnailLoader)
        }
        val tabsAdapter = TabsAdapter(thumbnailLoader, viewHolderProvider).apply {
            styling = trayStyling
        }

        tabsTray.layoutManager = layoutManager
        tabsTray.adapter = tabsAdapter

        TabsTouchHelper(tabsAdapter).attachToRecyclerView(tabsTray)

        return tabsAdapter
    }
}
