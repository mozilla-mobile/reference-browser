/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
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
import org.mozilla.reference.browser.IntentReceiverActivity
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.R.string.pref_key_sign_out
import org.mozilla.reference.browser.R.string.pref_key_sync_history
import org.mozilla.reference.browser.R.string.pref_key_sync_manage_account
import org.mozilla.reference.browser.R.string.pref_key_sync_now
import org.mozilla.reference.browser.R.string.pref_key_sync_passwords
import org.mozilla.reference.browser.R.string.pref_key_sync_tabs
import org.mozilla.reference.browser.components.BackgroundServices.Companion.SUPPORTED_SYNC_ENGINES
import org.mozilla.reference.browser.ext.getPreferenceKey
import org.mozilla.reference.browser.ext.requireComponents
import org.mozilla.reference.browser.sync.BrowserFxAEntryPoint

/**
 * [AccountSettingsFragment] is a fragment that displays and manages the account settings for
 * the user's Firefox Account (FxA). It handles preferences related to syncing, sign-out, and account management.
 *
 * The fragment provides options to manually trigger a sync, manage sync engines (like History, Passwords, Tabs),
 * and shows the last time the account was synced. It also allows the user to manage their Firefox Account using a
 * custom tab.
 */
class AccountSettingsFragment : PreferenceFragmentCompat() {
    private val syncStatusObserver = object : SyncStatusObserver {
        /**
         * Called when the sync operation starts. Disables the "Sync Now" preference and updates its title
         * to indicate that a sync is in progress.
         */
        override fun onStarted() {
            CoroutineScope(Dispatchers.Main).launch {
                val pref = findPreference<Preference>(requireContext().getPreferenceKey(pref_key_sync_now))

                pref?.title = getString(R.string.syncing)
                pref?.isEnabled = false
            }
        }

        /**
         * Called when the sync operation successfully completes. Re-enables the "Sync Now" preference and
         * updates its title and summary to reflect the last sync time.
         */
        override fun onIdle() {
            CoroutineScope(Dispatchers.Main).launch {
                val pref = findPreference<Preference>(requireContext().getPreferenceKey(pref_key_sync_now))
                pref?.title = getString(R.string.sync_now)
                pref?.isEnabled = true
                updateLastSyncedTimePref(context!!, pref, failed = false)
                updateSyncEngineStates()
            }
        }

        /**
         * Called when the sync operation encounters an error. Re-enables the "Sync Now" preference and updates
         * its summary to reflect the sync failure.
         */
        override fun onError(error: Exception?) {
            CoroutineScope(Dispatchers.Main).launch {
                val pref = findPreference<Preference>(requireContext().getPreferenceKey(pref_key_sync_now))
                pref?.title = getString(R.string.sync_now)
                pref?.isEnabled = true
                updateLastSyncedTimePref(context!!, pref, failed = true)
            }
        }
    }

    /**
     * Initializes the preferences from the XML resource and sets up listeners for the sync-related preferences.
     *
     * @param savedInstanceState The saved instance state of the fragment.
     * @param rootKey If non-null, this preference fragment should be rooted at the preference with this key.
     */
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.account_preferences, rootKey)

        val signOutKey = requireContext().getPreferenceKey(pref_key_sign_out)
        val syncNowKey = requireContext().getPreferenceKey(pref_key_sync_now)
        val manageAccountKey = requireContext().getPreferenceKey(pref_key_sync_manage_account)

        // Sign Out
        val preferenceSignOut = findPreference<CustomColorPreference>(signOutKey)
        preferenceSignOut?.onPreferenceClickListener = getClickListenerForSignOut()

        // Sync Now
        val preferenceSyncNow = findPreference<Preference>(syncNowKey)
        updateLastSyncedTimePref(requireContext(), preferenceSyncNow)

        preferenceSyncNow?.onPreferenceClickListener = getClickListenerForSyncNow()

        // Manage Account
        val preferenceManageAccount = findPreference<Preference>(manageAccountKey)
        preferenceManageAccount?.onPreferenceClickListener = getClickListenerForManageAccount()

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

    /**
     * Updates the "Last Synced" preference to show the most recent sync time or failure message.
     *
     * @param context The context to access resources and settings.
     * @param pref The preference to update the summary for, representing the sync status.
     * @param failed A boolean indicating whether the last sync attempt failed.
     */
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
                activity?.onBackPressedDispatcher?.onBackPressed()
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

    private fun getClickListenerForManageAccount(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                context?.let {
                    val account =
                        requireComponents.backgroundServices.accountManager.authenticatedAccount()
                    val url = account?.getManageAccountURL(BrowserFxAEntryPoint.AccountSettings)
                    if (url != null) {
                        val intent = createCustomTabIntent(it, url)
                        startActivity(intent)
                    }
                }
            }
            true
        }
    }

    private fun createCustomTabIntent(context: Context, url: String): Intent = CustomTabsIntent.Builder()
        .setInstantAppsEnabled(false)
        .build()
        .intent
        .setData(url.toUri())
        .setClassName(context, IntentReceiverActivity::class.java.name)
        .setPackage(context.packageName)

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
