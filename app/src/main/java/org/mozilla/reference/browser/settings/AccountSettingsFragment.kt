/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.os.Bundle
import android.text.format.DateUtils
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.getPreferenceKey
import org.mozilla.reference.browser.ext.requireComponents
import org.mozilla.reference.browser.R.string.pref_key_sign_out
import org.mozilla.reference.browser.R.string.pref_key_sync_now
import org.mozilla.reference.browser.browser.FirefoxAccountsIntegration

class AccountSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.account_preferences, rootKey)

        val signOutKey = context?.getPreferenceKey(pref_key_sign_out)
        val syncNowKey = context?.getPreferenceKey(pref_key_sync_now)

        // Sign Out
        val preferenceSignOut = findPreference(signOutKey)
        preferenceSignOut.onPreferenceClickListener = getClickListenerForSignOut()

        // Sync Now
        val preferenceSyncNow = findPreference(syncNowKey)
        preferenceSyncNow.isEnabled = true
        updateLastSyncedTimePref(preferenceSyncNow)

        preferenceSyncNow.onPreferenceClickListener = getClickListenerForSyncNow()
    }

    private fun updateLastSyncedTimePref(pref: Preference) {
        val lastSyncTime = requireComponents.firefoxAccountsIntegration.getLastSynced()

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
            it.title = getString(R.string.syncing)
            it.isEnabled = false

            CoroutineScope(Dispatchers.Main).launch {
                CoroutineScope(Dispatchers.IO).launch {
                    requireComponents.firefoxSyncFeature.sync(
                            requireComponents.firefoxAccountsIntegration.account.await()
                    ).await()
                }.join()

                it.title = getString(R.string.sync_now)
                it.isEnabled = true

                requireComponents.firefoxAccountsIntegration.setLastSynced(System.currentTimeMillis())
                updateLastSyncedTimePref(it)
            }
            true
        }
    }
}
