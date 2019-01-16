/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import mozilla.components.browser.session.Session
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.R.string.pref_key_firefox_account
import org.mozilla.reference.browser.ext.getPreferenceKey
import org.mozilla.reference.browser.R.string.pref_key_sign_in
import org.mozilla.reference.browser.R.string.pref_key_make_default_browser
import org.mozilla.reference.browser.R.string.pref_key_remote_debugging
import org.mozilla.reference.browser.R.string.pref_key_about_page
import org.mozilla.reference.browser.R.string.pref_key_privacy
import org.mozilla.reference.browser.ext.requireComponents

class SettingsFragment : PreferenceFragmentCompat() {

    interface ActionBarUpdater {
        fun updateTitle(titleResId: Int)
    }

    private val defaultClickListener = OnPreferenceClickListener { preference ->
        Toast.makeText(context, "${preference.title} Clicked", LENGTH_SHORT).show()
        true
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()

        setupPreferences()
        getActionBarUpdater().apply {
            updateTitle(R.string.settings)
        }
    }

    @Suppress("LongMethod") // Yep, this should be refactored.
    private fun setupPreferences() {
        val signInKey = context?.getPreferenceKey(pref_key_sign_in)
        val firefoxAccountKey = context?.getPreferenceKey(pref_key_firefox_account)
        val makeDefaultBrowserKey = context?.getPreferenceKey(pref_key_make_default_browser)
        val remoteDebuggingKey = context?.getPreferenceKey(pref_key_remote_debugging)
        val aboutPageKey = context?.getPreferenceKey(pref_key_about_page)
        val privacyKey = context?.getPreferenceKey(pref_key_privacy)

        val preferenceSignIn = findPreference(signInKey)
        val preferenceFirefoxAccount = findPreference(firefoxAccountKey)
        val preferenceMakeDefaultBrowser = findPreference(makeDefaultBrowserKey)
        val preferenceRemoteDebugging = findPreference(remoteDebuggingKey)
        val preferenceAboutPage = findPreference(aboutPageKey)
        val preferencePrivacy = findPreference(privacyKey)

        val fxaIntegration = requireComponents.services.accounts
        preferenceSignIn.onPreferenceClickListener = getClickListenerForSignIn()
        preferenceSignIn.isVisible = fxaIntegration.profile == null
        preferenceFirefoxAccount.onPreferenceClickListener = getClickListenerForFirefoxAccount()
        preferenceFirefoxAccount.isVisible = fxaIntegration.profile != null
        preferenceFirefoxAccount.summary = fxaIntegration.profile?.email
        preferenceMakeDefaultBrowser.onPreferenceClickListener = getClickListenerForMakeDefaultBrowser()
        preferenceRemoteDebugging.onPreferenceChangeListener = getChangeListenerForRemoteDebugging()
        preferenceAboutPage.onPreferenceClickListener = getAboutPageListener()
        preferencePrivacy.onPreferenceClickListener = getClickListenerForPrivacy()
    }

    private fun getClickListenerForMakeDefaultBrowser(): OnPreferenceClickListener {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            OnPreferenceClickListener {
                val intent = Intent(
                    Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS
                )
                startActivity(intent)
                true
            }
        } else {
            defaultClickListener
        }
    }

    private fun getClickListenerForSignIn(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            activity?.finish()
            requireComponents.services.accounts.authenticate()
            true
        }
    }

    private fun getClickListenerForFirefoxAccount(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            fragmentManager?.beginTransaction()
                    ?.replace(android.R.id.content, AccountSettingsFragment())
                    ?.addToBackStack(null)
                    ?.commit()
            getActionBarUpdater().apply {
                updateTitle(R.string.account_settings)
            }
            true
        }
    }

    private fun getClickListenerForPrivacy(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            fragmentManager?.beginTransaction()
                    ?.replace(android.R.id.content, PrivacySettingsFragment())
                    ?.addToBackStack(null)
                    ?.commit()
            getActionBarUpdater().apply {
                updateTitle(R.string.privacy_settings)
            }
            true
        }
    }

    private fun getChangeListenerForRemoteDebugging(): OnPreferenceChangeListener {
        return OnPreferenceChangeListener { _, newValue ->
            requireComponents.core.engine.settings.remoteDebuggingEnabled = newValue as Boolean
            true
        }
    }

    private fun getAboutPageListener(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            activity?.finish()
            requireComponents.core.sessionManager.add(Session("about:version"), true)
            true
        }
    }

    private fun getActionBarUpdater() = activity as ActionBarUpdater
}
