/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.os.Bundle
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.getPreferenceKey
import org.mozilla.reference.browser.ext.requireComponents
import org.mozilla.reference.browser.R.string.pref_key_sign_out
import org.mozilla.reference.browser.R.string.pref_key_sync_now

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
        preferenceSyncNow.isEnabled = false // Make this `fxaIntegration.profile == null` when implemented
        preferenceSyncNow.summary = getString(R.string.preferences_sync_never_synced_summary) // Use Long.timeSince()
        preferenceSyncNow.onPreferenceClickListener = getClickListenerForSyncNow()
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
            // Implement this Sync Now functionality when available.
            true
        }
    }
}
