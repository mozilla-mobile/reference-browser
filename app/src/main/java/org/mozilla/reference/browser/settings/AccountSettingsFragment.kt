/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.service.fxa.SyncEngine
import mozilla.components.service.fxa.manager.SyncEnginesStorage
import mozilla.components.service.fxa.sync.SyncReason
import mozilla.components.service.fxa.sync.SyncStatusObserver
import mozilla.components.service.fxa.sync.getLastSynced
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.R.string.pref_key_sign_out
import org.mozilla.reference.browser.R.string.pref_key_sync_history
import org.mozilla.reference.browser.R.string.pref_key_sync_now
import org.mozilla.reference.browser.R.string.pref_key_sync_passwords
import org.mozilla.reference.browser.R.string.pref_key_sync_tabs
import org.mozilla.reference.browser.components.BackgroundServices.Companion.SUPPORTED_SYNC_ENGINES
import org.mozilla.reference.browser.ext.getPreferenceKey
import org.mozilla.reference.browser.ext.requireComponents

class AccountSettingsFragment : PreferenceFragmentCompat() {
    private val syncStatusObserver = object : SyncStatusObserver {
        override fun onStarted() {
            CoroutineScope(Dispatchers.Main).launch {
                val pref = findPreference<Preference>(requireContext().getPreferenceKey(pref_key_sync_now))

                pref?.title = getString(R.string.syncing)
                pref?.isEnabled = false
            }
        }

        // Sync stopped successfully.
        override fun onIdle() {
            CoroutineScope(Dispatchers.Main).launch {
                val pref = findPreference<Preference>(requireContext().getPreferenceKey(pref_key_sync_now))
                pref?.title = getString(R.string.sync_now)
                pref?.isEnabled = true
                updateLastSyncedTimePref(context!!, pref, failed = false)
                updateSyncEngineStates()
            }
        }

        // Sync stopped after encountering a problem.
        override fun onError(error: Exception?) {
            CoroutineScope(Dispatchers.Main).launch {
                val pref = findPreference<Preference>(requireContext().getPreferenceKey(pref_key_sync_now))
                pref?.title = getString(R.string.sync_now)
                pref?.isEnabled = true
                updateLastSyncedTimePref(context!!, pref, failed = true)
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.account_preferences, rootKey)

        val signOutKey = requireContext().getPreferenceKey(pref_key_sign_out)
        val syncNowKey = requireContext().getPreferenceKey(pref_key_sync_now)

        // Sign Out
        val preferenceSignOut = findPreference<CustomColorPreference>(signOutKey)
        preferenceSignOut?.onPreferenceClickListener = getClickListenerForSignOut()

        // Sync Now
        val preferenceSyncNow = findPreference<Preference>(syncNowKey)
        updateLastSyncedTimePref(requireContext(), preferenceSyncNow)

        preferenceSyncNow?.onPreferenceClickListener = getClickListenerForSyncNow()

        SUPPORTED_SYNC_ENGINES.forEach {
            val preferenceKey = requireContext().getPreferenceKey(it.prefId())
            (findPreference<CheckBoxPreference>(preferenceKey) as CheckBoxPreference).apply {
                setOnPreferenceChangeListener { _, newValue ->
                    updateSyncEngineState(context, it, newValue as Boolean)
                    true
                }
            }
        }

        updateSyncEngineStates()

        // NB: ObserverRegistry will take care of cleaning up internal references to 'observer' and
        // 'owner' when appropriate.
        requireComponents.backgroundServices.accountManager.registerForSyncEvents(
            syncStatusObserver,
            owner = this,
            autoPause = true,
        )
    }

    fun updateLastSyncedTimePref(context: Context, pref: Preference?, failed: Boolean = false) {
        val lastSyncTime = getLastSynced(context)

        pref?.summary = if (!failed && lastSyncTime == 0L) {
            // Never tried to sync.
            getString(R.string.preferences_sync_never_synced_summary)
        } else if (failed && lastSyncTime == 0L) {
            // Failed to sync, never succeeded before.
            getString(R.string.preferences_sync_failed_never_synced_summary)
        } else if (!failed && lastSyncTime != 0L) {
            // Successfully synced.
            getString(
                R.string.preferences_sync_last_synced_summary,
                DateUtils.getRelativeTimeSpanString(lastSyncTime),
            )
        } else {
            // Failed to sync, succeeded before.
            getString(
                R.string.preferences_sync_failed_summary,
                DateUtils.getRelativeTimeSpanString(lastSyncTime),
            )
        }
    }

    private fun getClickListenerForSignOut(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                requireComponents.backgroundServices.accountManager.logout()
                activity?.onBackPressed()
            }
            true
        }
    }

    private fun getClickListenerForSyncNow(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                // Trigger a sync & update devices.
                requireComponents.backgroundServices.accountManager.syncNow(SyncReason.User)
                // Poll for device events.
                requireComponents.backgroundServices.accountManager.authenticatedAccount()
                    ?.deviceConstellation()?.run {
                        refreshDevices()
                        pollForCommands()
                    }
            }
            true
        }
    }

    private fun updateSyncEngineState(context: Context, engine: SyncEngine, newState: Boolean) {
        SyncEnginesStorage(context).setStatus(engine, newState)
        CoroutineScope(Dispatchers.Main).launch {
            requireComponents.backgroundServices.accountManager.syncNow(SyncReason.EngineChange)
        }
    }

    private fun updateSyncEngineStates() {
        val syncEnginesStatus = SyncEnginesStorage(requireContext()).getStatus()
        SUPPORTED_SYNC_ENGINES.forEach { engine ->
            val preferenceKey = requireContext().getPreferenceKey(engine.prefId())
            (findPreference<CheckBoxPreference>(preferenceKey) as CheckBoxPreference).apply {
                isEnabled = syncEnginesStatus.containsKey(engine)
                isChecked = syncEnginesStatus.getOrElse(engine) { true }
            }
        }
    }

    private fun SyncEngine.prefId(): Int = when (this) {
        SyncEngine.History -> pref_key_sync_history
        SyncEngine.Passwords -> pref_key_sync_passwords
        SyncEngine.Tabs -> pref_key_sync_tabs
        else -> throw IllegalStateException("Accessing unsupported sync engines")
    }
}
