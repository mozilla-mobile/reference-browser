/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.View
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.Preference.OnPreferenceClickListener
import androidx.preference.PreferenceFragmentCompat
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.amo_collection_override_dialog.view.custom_amo_collection
import kotlinx.android.synthetic.main.amo_collection_override_dialog.view.custom_amo_user
import mozilla.components.support.ktx.android.view.showKeyboard
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.R.string.pref_key_firefox_account
import org.mozilla.reference.browser.ext.getPreferenceKey
import org.mozilla.reference.browser.R.string.pref_key_sign_in
import org.mozilla.reference.browser.R.string.pref_key_pair_sign_in
import org.mozilla.reference.browser.R.string.pref_key_make_default_browser
import org.mozilla.reference.browser.R.string.pref_key_remote_debugging
import org.mozilla.reference.browser.R.string.pref_key_about_page
import org.mozilla.reference.browser.R.string.pref_key_privacy
import org.mozilla.reference.browser.R.string.pref_key_override_amo_collection
import org.mozilla.reference.browser.ext.requireComponents
import kotlin.system.exitProcess

private typealias RBSettings = org.mozilla.reference.browser.settings.Settings

@Suppress("TooManyFunctions")
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
        val signInPairKey = context?.getPreferenceKey(pref_key_pair_sign_in)
        val firefoxAccountKey = context?.getPreferenceKey(pref_key_firefox_account)
        val makeDefaultBrowserKey = context?.getPreferenceKey(pref_key_make_default_browser)
        val remoteDebuggingKey = context?.getPreferenceKey(pref_key_remote_debugging)
        val aboutPageKey = context?.getPreferenceKey(pref_key_about_page)
        val privacyKey = context?.getPreferenceKey(pref_key_privacy)
        val customAddonsKey = context?.getPreferenceKey(pref_key_override_amo_collection)

        val preferenceSignIn = findPreference(signInKey)
        val preferencePairSignIn = findPreference(signInPairKey)
        val preferenceFirefoxAccount = findPreference(firefoxAccountKey)
        val preferenceMakeDefaultBrowser = findPreference(makeDefaultBrowserKey)
        val preferenceRemoteDebugging = findPreference(remoteDebuggingKey)
        val preferenceAboutPage = findPreference(aboutPageKey)
        val preferencePrivacy = findPreference(privacyKey)
        val preferenceCustomAddons = findPreference(customAddonsKey)

        val accountManager = requireComponents.backgroundServices.accountManager
        if (accountManager.authenticatedAccount() != null) {
            preferenceSignIn.isVisible = false
            preferencePairSignIn.isVisible = false
            preferenceFirefoxAccount.summary = accountManager.accountProfile()?.email.orEmpty()
            preferenceFirefoxAccount.onPreferenceClickListener = getClickListenerForFirefoxAccount()
        } else {
            preferenceSignIn.isVisible = true
            preferenceFirefoxAccount.isVisible = false
            preferenceFirefoxAccount.onPreferenceClickListener = null
            preferenceSignIn.onPreferenceClickListener = getClickListenerForSignIn()
            preferencePairSignIn.isVisible = true
            preferencePairSignIn.onPreferenceClickListener = getClickListenerForPairingSignIn()
        }

        preferenceMakeDefaultBrowser.onPreferenceClickListener = getClickListenerForMakeDefaultBrowser()
        preferenceRemoteDebugging.onPreferenceChangeListener = getChangeListenerForRemoteDebugging()
        preferenceAboutPage.onPreferenceClickListener = getAboutPageListener()
        preferencePrivacy.onPreferenceClickListener = getClickListenerForPrivacy()
        preferenceCustomAddons.onPreferenceClickListener = getClickListenerForCustomAddons()
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
            requireComponents.services.accountsAuthFeature.beginAuthentication(requireContext())
            activity?.finish()
            true
        }
    }

    private fun getClickListenerForPairingSignIn(): OnPreferenceClickListener {
        return OnPreferenceClickListener {
            fragmentManager?.beginTransaction()
                    ?.replace(android.R.id.content, PairSettingsFragment())
                    ?.addToBackStack(null)
                    ?.commit()
            getActionBarUpdater().apply {
                updateTitle(R.string.pair_preferences)
            }
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
            fragmentManager?.beginTransaction()
                ?.replace(android.R.id.content, AboutFragment())
                ?.addToBackStack(null)
                ?.commit()
            true
        }
    }

    private fun getActionBarUpdater() = activity as ActionBarUpdater

    private fun getClickListenerForCustomAddons(): OnPreferenceClickListener {

        return OnPreferenceClickListener {
            val context = requireContext()
            val dialogView = View.inflate(context, R.layout.amo_collection_override_dialog, null)

            AlertDialog.Builder(context).apply {
                setTitle(context.getString(R.string.preferences_customize_amo_collection))
                setView(dialogView)
                setNegativeButton(R.string.customize_addon_collection_cancel) { dialog: DialogInterface, _ ->
                    dialog.cancel()
                }

                setPositiveButton(R.string.customize_addon_collection_ok) { _, _ ->
                    RBSettings.setOverrideAmoUser(context, dialogView.custom_amo_user.text.toString())
                    RBSettings.setOverrideAmoCollection(context, dialogView.custom_amo_collection.text.toString())

                    Toast.makeText(
                            context,
                            getString(R.string.toast_customize_addon_collection_done),
                            Toast.LENGTH_LONG
                    ).show()

                    Handler().postDelayed({
                        exitProcess(0)
                    }, AMO_COLLECTION_OVERRIDE_EXIT_DELAY)
                }

                dialogView.custom_amo_collection.setText(RBSettings.getOverrideAmoCollection(context))
                dialogView.custom_amo_user.setText(RBSettings.getOverrideAmoUser(context))
                dialogView.custom_amo_user.requestFocus()
                dialogView.custom_amo_user.showKeyboard()
                create()
            }.show()
            true
        }
    }
    companion object {
        private const val AMO_COLLECTION_OVERRIDE_EXIT_DELAY = 3000L
    }
}
