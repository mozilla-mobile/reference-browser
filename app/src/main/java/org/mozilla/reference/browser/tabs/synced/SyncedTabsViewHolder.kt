/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabs.synced

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.browser.storage.sync.Tab
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.tabs.synced.SyncedTabsAdapter.AdapterItem

/** View holders for the two row types of [SyncedTabsAdapter]. */
sealed class SyncedTabsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    /**
     * Takes the base item type so the adapter can bind without knowing the holder's type; each holder casts to the item
     * type its view type was created for. [interactor] is only used by tab rows, device headers are not clickable.
     */
    abstract fun <T : AdapterItem> bind(
        item: T,
        interactor: (Tab) -> Unit,
    )

    /** Row for one synced tab, opening it when tapped. */
    class TabViewHolder(itemView: View) : SyncedTabsViewHolder(itemView) {
        // See TODO below
        // private val image = itemView.findViewById<ImageView>(R.id.synced_tabs_item_image)
        private val title = itemView.findViewById<TextView>(R.id.synced_tabs_item_title)
        private val url = itemView.findViewById<TextView>(R.id.synced_tabs_item_desc)

        override fun <T : AdapterItem> bind(
            item: T,
            interactor: (Tab) -> Unit,
        ) {
            bindTab(item as AdapterItem.Tab)

            itemView.setOnClickListener {
                interactor(item.tab)
            }
        }

        private fun bindTab(tab: AdapterItem.Tab) {
            val active = tab.tab.active()
            title.text = active.title
            url.text = active.url

            // TODO download and set icon image.
            // Requires https://bugzilla.mozilla.org/show_bug.cgi?id=1793192
        }

        companion object {
            val LAYOUT_ID = R.layout.view_synced_tabs_item
        }
    }

    /** Header row with the name of the device whose tabs follow. */
    class DeviceViewHolder(itemView: View) : SyncedTabsViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.synced_tabs_group_name)

        override fun <T : AdapterItem> bind(
            item: T,
            interactor: (Tab) -> Unit,
        ) {
            bindHeader(item as AdapterItem.Device)
        }

        private fun bindHeader(device: AdapterItem.Device) {
            title.text = device.device.displayName
        }

        companion object {
            val LAYOUT_ID = R.layout.view_synced_tabs_group
        }
    }
}
