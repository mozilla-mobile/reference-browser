/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabs.synced

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import mozilla.components.browser.storage.sync.SyncedDeviceTabs
import mozilla.components.feature.syncedtabs.view.SyncedTabsView
import mozilla.components.feature.syncedtabs.view.SyncedTabsView.ErrorType.MULTIPLE_DEVICES_UNAVAILABLE
import mozilla.components.feature.syncedtabs.view.SyncedTabsView.ErrorType.NO_TABS_AVAILABLE
import mozilla.components.feature.syncedtabs.view.SyncedTabsView.ErrorType.SYNC_ENGINE_UNAVAILABLE
import mozilla.components.feature.syncedtabs.view.SyncedTabsView.ErrorType.SYNC_NEEDS_REAUTHENTICATION
import mozilla.components.feature.syncedtabs.view.SyncedTabsView.ErrorType.SYNC_UNAVAILABLE
import org.mozilla.reference.browser.R

class SyncedTabsLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr), SyncedTabsView {

    override var listener: SyncedTabsView.Listener? = null

    private val adapter = SyncedTabsAdapter() { listener?.onTabClicked(it) }

    private val syncedTabsList: RecyclerView
        get() = findViewById(R.id.synced_tabs_list)
    private val syncedTabsPullToRefresh: SwipeRefreshLayout
        get() = findViewById(R.id.synced_tabs_pull_to_refresh)
    private val syncedTabsStatus: TextView
        get() = findViewById(R.id.synced_tabs_status)

    init {
        inflate(getContext(), R.layout.layout_synced_tabs, this)

        syncedTabsList.layoutManager = LinearLayoutManager(context)
        syncedTabsList.adapter = adapter

        syncedTabsPullToRefresh.setOnRefreshListener { listener?.onRefresh() }
    }

    override fun onError(error: SyncedTabsView.ErrorType) {
        val stringResId = when (error) {
            MULTIPLE_DEVICES_UNAVAILABLE, NO_TABS_AVAILABLE -> R.string.synced_tabs_connect_another_device
            SYNC_ENGINE_UNAVAILABLE -> R.string.synced_tabs_enable_tab_syncing
            SYNC_UNAVAILABLE -> R.string.synced_tabs_connect_to_sync_account
            SYNC_NEEDS_REAUTHENTICATION -> R.string.synced_tabs_reauth
        }

        syncedTabsStatus.text = context.getText(stringResId)

        syncedTabsList.visibility = View.GONE
        syncedTabsStatus.visibility = View.VISIBLE
    }

    override fun displaySyncedTabs(syncedTabs: List<SyncedDeviceTabs>) {
        syncedTabsList.visibility = View.VISIBLE
        syncedTabsStatus.visibility = View.GONE

        val allDeviceTabs = syncedTabs.filter {
            it.tabs.isEmpty()
        }.flatMap { (device, tabs) ->
            val deviceTabs = tabs.map { SyncedTabsAdapter.AdapterItem.Tab(it) }

            listOf(SyncedTabsAdapter.AdapterItem.Device(device)) + deviceTabs
        }

        adapter.submitList(allDeviceTabs)
    }

    override fun startLoading() {
        syncedTabsList.visibility = View.VISIBLE
        syncedTabsStatus.visibility = View.GONE

        syncedTabsPullToRefresh.isRefreshing = true
    }

    override fun stopLoading() {
        syncedTabsPullToRefresh.isRefreshing = false
    }
}
