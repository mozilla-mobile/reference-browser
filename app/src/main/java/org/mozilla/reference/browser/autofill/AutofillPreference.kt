/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.autofill

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.AttributeSet
import android.view.autofill.AutofillManager
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import org.mozilla.reference.browser.R

class AutofillPreference @JvmOverloads constructor(
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

    fun updateSwitch() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val autofillManager = context.getSystemService(AutofillManager::class.java)
        switchView?.isChecked = autofillManager.hasEnabledAutofillServices()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick() {
        val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE)
        intent.data = Uri.parse("package:${context.packageName}")
        context.startActivity(intent)
    }

    companion object {
        fun isSupported(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                return false
            }

            val autofillManager = context.getSystemService(AutofillManager::class.java)
            return autofillManager.isAutofillSupported
        }
    }
}
