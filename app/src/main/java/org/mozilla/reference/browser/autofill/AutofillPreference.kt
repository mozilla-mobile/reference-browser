/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.autofill

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.AttributeSet
import android.view.autofill.AutofillManager
import androidx.appcompat.widget.SwitchCompat
import androidx.core.net.toUri
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import org.mozilla.reference.browser.R

/**
 * Settings entry for choosing this app as the system autofill service. The switch mirrors system state instead of a
 * stored preference because the choice is made in Android settings.
 */
class AutofillPreference
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : Preference(context, attrs) {
    private var switchView: SwitchCompat? = null

    init {
        widgetLayoutResource = R.layout.preference_autofill
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        switchView = holder.findViewById(R.id.switch_widget) as SwitchCompat

        updateSwitch()
    }

    /**
     * The user changes the autofill service in Android settings, outside this app, so settings calls this on resume to
     * keep the switch from showing a stale value after they come back.
     */
    fun updateSwitch() {
        val autofillManager = context.getSystemService(AutofillManager::class.java)
        switchView?.isChecked = autofillManager.hasEnabledAutofillServices()
    }

    override fun onClick() {
        val intent =
            Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
                data = "package:${context.packageName}".toUri()
            }
        context.startActivity(intent)
    }

    companion object {
        /** Lets settings hide the entry on devices where autofill is not supported. */
        fun isSupported(context: Context): Boolean {
            val autofillManager = context.getSystemService(AutofillManager::class.java)
            return autofillManager.isAutofillSupported
        }
    }
}
