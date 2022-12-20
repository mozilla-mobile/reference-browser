/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.content.Context
import androidx.preference.PreferenceManager
import org.mozilla.reference.browser.R

object Settings {
    fun isTelemetryEnabled(context: Context): Boolean =
        PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.pref_key_telemetry),
            true,
        )

    fun getOverrideAmoUser(context: Context): String =
        PreferenceManager.getDefaultSharedPreferences(context).getString(
            context.getString(R.string.pref_key_override_amo_user),
            "",
        ) ?: ""

    fun getOverrideAmoCollection(context: Context): String =
        PreferenceManager.getDefaultSharedPreferences(context).getString(
            context.getString(R.string.pref_key_override_amo_collection),
            "",
        ) ?: ""

    fun setOverrideAmoUser(context: Context, value: String) {
        val key = context.getString(R.string.pref_key_override_amo_user)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(key, value)
            .apply()
    }

    fun setOverrideAmoCollection(context: Context, value: String) {
        val key = context.getString(R.string.pref_key_override_amo_collection)
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(key, value)
            .apply()
    }

    fun isAmoCollectionOverrideConfigured(context: Context): Boolean {
        return getOverrideAmoUser(context).isNotEmpty() && getOverrideAmoCollection(context).isNotEmpty()
    }
}
