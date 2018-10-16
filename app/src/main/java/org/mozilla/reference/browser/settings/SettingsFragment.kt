/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v7.preference.Preference.OnPreferenceClickListener
import android.support.v7.preference.PreferenceFragmentCompat
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.R.string.pref_key_firefox_account
import org.mozilla.reference.browser.ext.getPreferenceKey
import org.mozilla.reference.browser.R.string.pref_key_sign_in
import org.mozilla.reference.browser.R.string.pref_key_make_default_browser
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

    private fun setupPreferences() {
        val signInKey = context?.getPreferenceKey(pref_key_sign_in)
        val firefoxAccountKey = context?.getPreferenceKey(pref_key_firefox_account)
        val makeDefaultBrowserKey = context?.getPreferenceKey(pref_key_make_default_browser)
        val preferenceSignIn = findPreference(signInKey)
        val preferenceFirefoxAccount = findPreference(firefoxAccountKey)
        val preferenceMakeDefaultBrowser = findPreference(makeDefaultBrowserKey)
        val fxaIntegration = requireComponents.firefoxAccountsIntegration

        preferenceSignIn.onPreferenceClickListener = getClickListenerForSignIn()
        preferenceSignIn.isVisible = fxaIntegration.profile == null

        preferenceFirefoxAccount.onPreferenceClickListener = getClickListenerForFirefoxAccount()
        preferenceFirefoxAccount.isVisible = fxaIntegration.profile != null
        preferenceFirefoxAccount.summary = fxaIntegration?.profile?.email

        preferenceMakeDefaultBrowser.onPreferenceClickListener = getClickListenerForMakeDefaultBrowser()
    }

    private fun getClickListenerForMakeDefaultBrowser(): OnPreferenceClickListener {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            OnPreferenceClickListener { _ ->
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
        return OnPreferenceClickListener { _ ->
            activity?.finish()
            requireComponents.firefoxAccountsIntegration.authenticate()
            true
        }
    }

    private fun getClickListenerForFirefoxAccount(): OnPreferenceClickListener {
        return OnPreferenceClickListener { _ ->
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

    private fun getActionBarUpdater() = activity as ActionBarUpdater
}
