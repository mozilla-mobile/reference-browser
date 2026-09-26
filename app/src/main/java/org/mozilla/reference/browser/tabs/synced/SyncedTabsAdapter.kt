/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabs.synced

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import mozilla.components.browser.storage.sync.Tab as SyncTab
import mozilla.components.concept.sync.Device as SyncDevice
import org.mozilla.reference.browser.tabs.synced.SyncedTabsViewHolder.DeviceViewHolder
import org.mozilla.reference.browser.tabs.synced.SyncedTabsViewHolder.TabViewHolder

/** Lists the tabs of the user's other synced devices, each device's tabs under a header naming it. */
class SyncedTabsAdapter(private val listener: (SyncTab) -> Unit) :
    ListAdapter<SyncedTabsAdapter.AdapterItem, SyncedTabsViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SyncedTabsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        return when (viewType) {
            DeviceViewHolder.LAYOUT_ID -> DeviceViewHolder(itemView)
            TabViewHolder.LAYOUT_ID -> TabViewHolder(itemView)
            else -> throw IllegalStateException()
        }
    }

    override fun onBindViewHolder(
        holder: SyncedTabsViewHolder,
        position: Int,
    ) {
        val item =
            when (holder) {
                is DeviceViewHolder -> getItem(position) as AdapterItem.Device
                is TabViewHolder -> getItem(position) as AdapterItem.Tab
            }
        holder.bind(item, listener)
    }

    override fun getItemViewType(position: Int): Int {
        val item: AdapterItem = getItem(position)
        return when (item) {
            is AdapterItem.Device -> DeviceViewHolder.LAYOUT_ID
            is AdapterItem.Tab -> TabViewHolder.LAYOUT_ID
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<AdapterItem>() {
        override fun areItemsTheSame(
            oldItem: AdapterItem,
            newItem: AdapterItem,
        ) = areContentsTheSame(oldItem, newItem)

        override fun areContentsTheSame(
            oldItem: AdapterItem,
            newItem: AdapterItem,
        ) = oldItem == newItem
    }

    /** A row of the synced tabs list: either a device header or one of that device's tabs. */
    sealed class AdapterItem {
        /** Header row naming the device whose tabs follow. */
        data class Device(val device: SyncDevice) : AdapterItem()

        /** A tab open on a synced device. */
        data class Tab(val tab: SyncTab) : AdapterItem()
    }
}
