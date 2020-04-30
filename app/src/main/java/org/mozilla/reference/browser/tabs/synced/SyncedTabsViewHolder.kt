/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabs.synced

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mozilla.components.browser.storage.sync.Tab
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.tabs.synced.SyncedTabsAdapter.AdapterItem

sealed class SyncedTabsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun <T : AdapterItem> bind(item: T, interactor: (Tab) -> Unit)

    class TabViewHolder(itemView: View) : SyncedTabsViewHolder(itemView) {
        private val image = itemView.findViewById<ImageView>(R.id.synced_tabs_item_image)
        private val title = itemView.findViewById<TextView>(R.id.synced_tabs_item_title)
        private val url = itemView.findViewById<TextView>(R.id.synced_tabs_item_desc)

        override fun <T : AdapterItem> bind(item: T, interactor: (Tab) -> Unit) {
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
            //  requires https://github.com/mozilla-mobile/android-components/issues/5179
        }

        companion object {
            const val LAYOUT_ID = R.layout.view_synced_tabs_item
        }
    }

    class DeviceViewHolder(itemView: View) : SyncedTabsViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.synced_tabs_group_name)

        override fun <T : AdapterItem> bind(item: T, interactor: (Tab) -> Unit) {
            bindHeader(item as AdapterItem.Device)
        }

        private fun bindHeader(device: AdapterItem.Device) {
            title.text = device.device.displayName
        }

        companion object {
            const val LAYOUT_ID = R.layout.view_synced_tabs_group
        }
    }
}
