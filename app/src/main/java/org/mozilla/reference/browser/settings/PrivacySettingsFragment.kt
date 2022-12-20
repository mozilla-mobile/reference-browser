/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.os.Bundle
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import mozilla.components.concept.engine.EngineSession.TrackingProtectionPolicy
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.getPreferenceKey
import org.mozilla.reference.browser.ext.requireComponents

class PrivacySettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.privacy_preferences, rootKey)

        val context = requireContext()
        val telemetryKey = context.getPreferenceKey(R.string.pref_key_telemetry)
        val trackingProtectionNormalKey = context.getPreferenceKey(R.string.pref_key_tracking_protection_normal)
        val trackingProtectionPrivateKey = context.getPreferenceKey(R.string.pref_key_tracking_protection_private)

        val prefTelemetry = findPreference<SwitchPreferenceCompat>(telemetryKey)
        val prefTrackingProtectionNormal = findPreference<SwitchPreferenceCompat>(trackingProtectionNormalKey)
        val prefTrackingProtectionPrivate = findPreference<SwitchPreferenceCompat>(trackingProtectionPrivateKey)

        prefTelemetry?.onPreferenceChangeListener = getChangeListenerForTelemetry()
        prefTrackingProtectionNormal?.onPreferenceChangeListener = getChangeListenerForTrackingProtection { enabled ->
            requireComponents.core.createTrackingProtectionPolicy(normalMode = enabled)
        }
        prefTrackingProtectionPrivate?.onPreferenceChangeListener = getChangeListenerForTrackingProtection { enabled ->
            requireComponents.core.createTrackingProtectionPolicy(privateMode = enabled)
        }
    }

    private fun getChangeListenerForTelemetry(): OnPreferenceChangeListener {
        return OnPreferenceChangeListener { _, _ ->
            true
        }
    }

    private fun getChangeListenerForTrackingProtection(
        createTrackingProtectionPolicy: (Boolean) -> TrackingProtectionPolicy,
    ): OnPreferenceChangeListener {
        return OnPreferenceChangeListener { _, value ->
            val policy = createTrackingProtectionPolicy(value as Boolean)
            requireComponents.useCases.settingsUseCases.updateTrackingProtection.invoke(policy)
            true
        }
    }
}
