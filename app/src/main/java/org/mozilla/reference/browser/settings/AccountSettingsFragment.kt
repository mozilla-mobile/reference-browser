/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.feature.sync.SyncStatusObserver
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.getPreferenceKey
import org.mozilla.reference.browser.ext.requireComponents
import org.mozilla.reference.browser.R.string.pref_key_sign_out
import org.mozilla.reference.browser.R.string.pref_key_sync_now
import org.mozilla.reference.browser.browser.FirefoxAccountsIntegration
import org.mozilla.reference.browser.ext.components

class AccountSettingsFragment : PreferenceFragmentCompat() {
    private val syncStatusObserver = object : SyncStatusObserver {
        override fun onIdle() {
            CoroutineScope(Dispatchers.Main).launch {
                val pref = findPreference(context?.getPreferenceKey(pref_key_sync_now))
                pref.title = getString(R.string.sync_now)
                pref.isEnabled = true
                updateLastSyncedTimePref(context!!, pref)
            }
        }

        override fun onStarted() {
            CoroutineScope(Dispatchers.Main).launch {
                val pref = findPreference(context?.getPreferenceKey(pref_key_sync_now))
                pref.title = getString(R.string.syncing)
                pref.isEnabled = false
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.account_preferences, rootKey)

        val signOutKey = context?.getPreferenceKey(pref_key_sign_out)
        val syncNowKey = context?.getPreferenceKey(pref_key_sync_now)

        // Sign Out
        val preferenceSignOut = findPreference(signOutKey)
        preferenceSignOut.onPreferenceClickListener = getClickListenerForSignOut()

        // Sync Now
        val preferenceSyncNow = findPreference(syncNowKey)
        updateLastSyncedTimePref(context!!, preferenceSyncNow)

        preferenceSyncNow.onPreferenceClickListener = getClickListenerForSyncNow()

        if (requireComponents.firefoxSyncFeature.syncRunning()) {
            preferenceSyncNow.title = getString(R.string.syncing)
            preferenceSyncNow.isEnabled = false
        } else {
            preferenceSyncNow.isEnabled = true
        }

        // NB: ObserverRegistry will take care of cleaning up internal references to 'observer' and
        // 'owner' when appropriate.
        requireComponents.firefoxSyncFeature.register(syncStatusObserver, owner = this, autoPause = true)
    }

    fun updateLastSyncedTimePref(context: Context, pref: Preference) {
        val lastSyncTime = context.components.firefoxAccountsIntegration.getLastSynced()

        if (lastSyncTime == FirefoxAccountsIntegration.FXA_NEVER_SYNCED_TS) {
            pref.summary = getString(R.string.preferences_sync_never_synced_summary)
        } else {
            pref.summary = getString(
                R.string.preferences_sync_last_synced_summary,
                DateUtils.getRelativeTimeSpanString(lastSyncTime)
            )
        }
    }

    private fun getClickListenerForSignOut(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            requireComponents.firefoxAccountsIntegration.logout()
            activity?.onBackPressed()
            true
        }
    }

    private fun getClickListenerForSyncNow(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                requireComponents.firefoxAccountsIntegration.syncNow()
            }
            true
        }
    }
}
