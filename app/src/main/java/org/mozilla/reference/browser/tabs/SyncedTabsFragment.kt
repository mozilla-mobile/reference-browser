/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabs

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_synced_tabs.synced_tabs_list
import kotlinx.android.synthetic.main.fragment_synced_tabs.synced_tabs_pull_to_refresh
import kotlinx.android.synthetic.main.fragment_synced_tabs.synced_tabs_status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.browser.storage.sync.TabEntry
import mozilla.components.concept.sync.AccountObserver
import mozilla.components.concept.sync.AuthType
import mozilla.components.concept.sync.Device
import mozilla.components.concept.sync.OAuthAccount
import mozilla.components.feature.syncedtabs.SyncedTabsFeature
import mozilla.components.service.fxa.SyncEngine
import mozilla.components.service.fxa.manager.FxaAccountManager
import mozilla.components.service.fxa.manager.SyncEnginesStorage
import mozilla.components.service.fxa.manager.ext.withConstellation
import mozilla.components.service.fxa.sync.SyncReason
import mozilla.components.service.fxa.sync.SyncStatusObserver
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.requireComponents

// The tricky part in this class is to handle all possible Sync+FxA states:
//
// - No Sync account
// - Connected to FxA but not Sync (impossible state on mobile at the moment).
// - Connected to Sync, but needs reconnection.
// - Connected to Sync, but tabs syncing disabled.
// - Connected to Sync, but tabs haven't been synced yet (they stay in memory after the first sync).
// - Connected to Sync, but only one device in the account (us), so no other tab to show.
// - Connected to Sync.

class SyncedTabsFragment : Fragment(), SyncedTabsViewPresenter {
    private lateinit var accountManager: FxaAccountManager
    private lateinit var syncedTabsFeature: SyncedTabsFeature
    private lateinit var refreshLayout: SwipeRefreshLayout
    private var syncedTabsAdapter = SyncedTabsAdapter()
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_synced_tabs, container, false)
    }

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)
        refreshLayout = synced_tabs_pull_to_refresh as SwipeRefreshLayout

        accountManager = requireComponents.backgroundServices.accountManager
        syncedTabsFeature = requireComponents.backgroundServices.syncedTabs

        synced_tabs_list.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = syncedTabsAdapter
        }

        // Listen to Sync/FxA events so we can update the UI accordingly.
        accountManager.registerForSyncEvents(
            SyncedTabsSyncObserver(this),
            owner = this,
            autoPause = true
        )
        accountManager.register(SyncedTabsAccountObserver(this))
        updateUI()

        refreshLayout.setOnRefreshListener {
            scope.launch {
                accountManager.withConstellation {
                    it.refreshDevicesAsync().await()
                }
                accountManager.syncNowAsync(SyncReason.User)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Kick off a sync in the background so we get the freshest tabs.
        scope.launch {
            accountManager.syncNowAsync(SyncReason.User).await()
        }
    }

    private fun updateUI() {
        val accountManager = requireComponents.backgroundServices.accountManager
        val account = accountManager.authenticatedAccount()

        when {
            // Signed-in, no problems.
            account != null && !accountManager.accountNeedsReauth() -> {
                if (SyncEnginesStorage(requireContext()).getStatus()[SyncEngine.Tabs] == true) {
                    // Tabs engine enabled.
                    showTabs()
                } else {
                    // Tab engine disabled
                    showInfoMessage("Please enable tab syncing.")
                }
            }
            // Signed-in, need to re-authenticate.
            account != null && accountManager.accountNeedsReauth() -> {
                showInfoMessage("Please re-authenticate.")
            }
            // Signed-out.
            else -> {
                showInfoMessage("Connect with a Firefox Account.")
            }
        }
    }

    private fun showInfoMessage(msg: String) {
        synced_tabs_status.text = msg
        synced_tabs_list.visibility = View.GONE
        synced_tabs_status.visibility = View.VISIBLE
    }

    private fun showTabs() {
        synced_tabs_list.visibility = View.VISIBLE
        synced_tabs_status.visibility = View.GONE
        scope.launch {
            val syncedTabs = syncedTabsFeature.getSyncedTabs()
            scope.launch(Dispatchers.Main) {
                if (syncedTabs.isEmpty() &&
                    accountManager.authenticatedAccount()?.deviceConstellation()?.state()?.otherDevices?.isEmpty()
                    == true) {
                    showInfoMessage("Connect another device.")
                } else {
                    val items = ArrayList<DeviceOrTabHolder>()
                    syncedTabs.forEach { (device, tabs) ->
                        if (tabs.isNotEmpty()) {
                            items.add(DeviceOrTabHolder.DeviceHolder(device))
                            tabs.forEach { tab ->
                                items.add(DeviceOrTabHolder.TabEntryHolder(tab.active()))
                            }
                        }
                    }
                    syncedTabsAdapter.submitList(items)
                }
            }
        }
    }

    override fun onSyncedTabStatusUpdate() {
        this.updateUI()
    }

    override fun onSyncedTabsSyncStart() {
        this.refreshLayout.isRefreshing = true
    }

    override fun onSyncedTabsSyncEnd() {
        this.showTabs()
        this.refreshLayout.isRefreshing = false
    }

    private class SyncedTabsAccountObserver(private val presenter: SyncedTabsViewPresenter) : AccountObserver {
        override fun onLoggedOut() {
            presenter.onSyncedTabStatusUpdate()
        }

        override fun onAuthenticated(account: OAuthAccount, authType: AuthType) {
            presenter.onSyncedTabStatusUpdate()
        }

        override fun onAuthenticationProblems() {
            presenter.onSyncedTabStatusUpdate()
        }
    }

    private class SyncedTabsSyncObserver(private val presenter: SyncedTabsViewPresenter) : SyncStatusObserver {
        override fun onIdle() {
            presenter.onSyncedTabsSyncEnd()
        }

        override fun onError(error: Exception?) {
            // Do nothing.
        }

        override fun onStarted() {
            presenter.onSyncedTabsSyncStart()
        }
    }

    private class SyncedTabsAdapter : ListAdapter<DeviceOrTabHolder, CustomViewHolder>(DiffCallback) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            return when (viewType) {
                VIEW_HOLDER_TYPE_DEVICE -> createDeviceViewHolder(parent)
                VIEW_HOLDER_TYPE_TAB -> createTabViewHolder(parent)
                else -> throw IllegalArgumentException("Unrecognized viewType")
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when (getItem(position)) {
                is DeviceOrTabHolder.DeviceHolder -> VIEW_HOLDER_TYPE_DEVICE
                is DeviceOrTabHolder.TabEntryHolder -> VIEW_HOLDER_TYPE_TAB
                else -> throw IllegalArgumentException("items[position] has unrecognized type")
            }
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            val item = getItem(position)

            when (holder) {
                is CustomViewHolder.DeviceViewHolder ->
                    bindDevice(holder, (item as DeviceOrTabHolder.DeviceHolder).device)
                is CustomViewHolder.TabViewHolder ->
                    bindTab(holder, (item as DeviceOrTabHolder.TabEntryHolder).tabEntry)
            }
        }

        private fun createDeviceViewHolder(parent: ViewGroup): CustomViewHolder {
            val context = parent.context
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.synced_tabs_group, parent, false)
            val deviceNameView = view.findViewById<TextView>(R.id.synced_tabs_group_name)
            return CustomViewHolder.DeviceViewHolder(view, deviceNameView)
        }

        private fun createTabViewHolder(parent: ViewGroup): CustomViewHolder {
            val context = parent.context
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.synced_tabs_item, parent, false)
            val tabTitleView = view.findViewById<TextView>(R.id.synced_tabs_item_title)
            val tabUrlView = view.findViewById<TextView>(R.id.synced_tabs_item_desc)
            return CustomViewHolder.TabViewHolder(view, tabTitleView, tabUrlView)
        }

        private fun bindDevice(holder: CustomViewHolder.DeviceViewHolder, device: Device) {
            holder.deviceNameView.text = device.displayName
        }

        private fun bindTab(holder: CustomViewHolder.TabViewHolder, tab: TabEntry) {
            holder.titleView.text = tab.title
            holder.urlView.text = tab.url
            holder.itemView.setOnClickListener { view ->
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(tab.url))
                view.context.startActivity(browserIntent)
            }
        }

        private object DiffCallback : DiffUtil.ItemCallback<DeviceOrTabHolder>() {

            override fun areItemsTheSame(oldItem: DeviceOrTabHolder, newItem: DeviceOrTabHolder): Boolean {
                return oldItem.javaClass.simpleName == newItem.javaClass.simpleName
            }

            override fun areContentsTheSame(oldItem: DeviceOrTabHolder, newItem: DeviceOrTabHolder): Boolean {
                return oldItem.haveSameContentAs(newItem)
            }
        }
    }

    /**
     * A base view holder.
     */
    sealed class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class DeviceViewHolder(
            view: View,
            val deviceNameView: TextView
        ) : CustomViewHolder(view)

        class TabViewHolder(
            view: View,
            val titleView: TextView,
            val urlView: TextView
        ) : CustomViewHolder(view)
    }

    sealed class DeviceOrTabHolder {
        class DeviceHolder(val device: Device) : DeviceOrTabHolder() {
            fun isEquiv(other: DeviceOrTabHolder): Boolean {
                return when (other) {
                    is TabEntryHolder -> false
                    is DeviceHolder -> device.displayName == other.device.displayName
                }
            }
        }
        class TabEntryHolder(val tabEntry: TabEntry) : DeviceOrTabHolder() {
            fun isEquiv(other: DeviceOrTabHolder): Boolean {
                return when (other) {
                    is DeviceHolder -> false
                    is TabEntryHolder -> tabEntry.title == other.tabEntry.title &&
                        tabEntry.url == other.tabEntry.url
                }
            }
        }

        // Equals is annoying to implement.
        fun haveSameContentAs(other: DeviceOrTabHolder): Boolean {
            return when (other) {
                is DeviceHolder -> other.isEquiv(this)
                is TabEntryHolder -> other.isEquiv(this)
            }
        }
    }

    companion object {
        private const val VIEW_HOLDER_TYPE_DEVICE = 0
        private const val VIEW_HOLDER_TYPE_TAB = 1
    }
}

interface SyncedTabsViewPresenter {
    fun onSyncedTabStatusUpdate()
    fun onSyncedTabsSyncStart()
    fun onSyncedTabsSyncEnd()
}
