/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.helpers

import androidx.annotation.StringRes
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import org.junit.rules.ExternalResource

/**
 * A [org.junit.Rule] that sets a boolean preference before the test runs and restores it afterwards.
 *
 * Chain this outside of [BrowserActivityTestRule] so that the preference is already in place when the activity, and
 * therefore the fragment reading it, is created:
 * ```
 * @get:Rule val rules = RuleChain.outerRule(BooleanPreferenceRule(key, true)).around(BrowserActivityTestRule())
 * ```
 *
 * @param key The string resource holding the preference key.
 * @param value The value to set for the duration of the test.
 */
class BooleanPreferenceRule(
    @StringRes private val key: Int,
    private val value: Boolean,
) : ExternalResource() {
    private var previousValue: Boolean? = null

    override fun before() {
        val context = TestHelper.appContext
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val name = context.getString(key)

        previousValue = if (preferences.contains(name)) preferences.getBoolean(name, false) else null
        preferences.edit { putBoolean(name, value) }
    }

    override fun after() {
        val context = TestHelper.appContext
        val name = context.getString(key)

        PreferenceManager.getDefaultSharedPreferences(context).edit {
            previousValue?.let { putBoolean(name, it) } ?: remove(name)
        }
    }
}
