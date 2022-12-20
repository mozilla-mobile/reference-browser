/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabs.synced

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import mozilla.components.browser.storage.sync.Tab
import mozilla.components.feature.syncedtabs.SyncedTabsFeature
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components

class SyncedTabsFragment : Fragment() {
    private val syncedTabsFeature = ViewBoundFeatureWrapper<SyncedTabsFeature>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.fragment_synced_tabs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backgroundServices = requireContext().components.backgroundServices

        syncedTabsFeature.set(
            feature = SyncedTabsFeature(
                context = requireContext(),
                storage = backgroundServices.syncedTabsStorage,
                accountManager = backgroundServices.accountManager,
                view = view.findViewById<SyncedTabsLayout>(R.id.synced_tabs_layout),
                lifecycleOwner = this,
                onTabClicked = ::handleTabClicked,
            ),
            owner = this,
            view = view,
        )
    }

    private fun handleTabClicked(tab: Tab) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(tab.active().url))
        requireContext().startActivity(browserIntent)
    }
}
