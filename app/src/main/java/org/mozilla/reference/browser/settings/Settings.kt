/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import org.mozilla.reference.browser.R

/** Typed access to preferences read from code. */
object Settings {
    /** Empty when unset, so callers can test for an override with [isAmoCollectionOverrideConfigured]. */
    fun getOverrideAmoUser(context: Context): String =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getString(
                context.getString(R.string.pref_key_override_amo_user),
                "",
            ) ?: ""

    /** Empty when unset, so callers can test for an override with [isAmoCollectionOverrideConfigured]. */
    fun getOverrideAmoCollection(context: Context): String =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getString(
                context.getString(R.string.pref_key_override_amo_collection),
                "",
            ) ?: ""

    /**
     * Takes effect only after a restart, because the add-on provider reads the override once per process. The settings
     * dialog that calls this exits the process for that reason.
     */
    fun setOverrideAmoUser(
        context: Context,
        value: String,
    ) {
        val key = context.getString(R.string.pref_key_override_amo_user)
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(key, value)
        }
    }

    /** Like [setOverrideAmoUser], takes effect only after a restart. */
    fun setOverrideAmoCollection(
        context: Context,
        value: String,
    ) {
        val key = context.getString(R.string.pref_key_override_amo_collection)
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            putString(key, value)
        }
    }

    /** Both values are required because an AMO collection is addressed by its owner and its name together. */
    fun isAmoCollectionOverrideConfigured(context: Context): Boolean =
        getOverrideAmoUser(context).isNotEmpty() && getOverrideAmoCollection(context).isNotEmpty()
}
