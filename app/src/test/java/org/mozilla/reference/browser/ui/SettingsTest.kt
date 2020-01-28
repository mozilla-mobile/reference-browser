/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui

import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.selectedDescendantsMatch
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.settings.SettingsActivity

@RunWith(AndroidJUnit4::class)
class SettingsTest {

    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(SettingsActivity::class.java)

    @Test
    fun settingsItemsTest() {
        assertPreferenceWithText(R.string.sign_in)
        assertPreferenceWithText(R.string.pair_sign_in)
        assertPreferenceWithText(R.string.privacy)
        assertSwitchPreferenceWithText(R.string.open_links_in_apps)
        assertPreferenceWithText(R.string.preferences_make_default_browser)
        assertCategoryWithText(R.string.developer_tools_category)
        assertSwitchPreferenceWithText(R.string.preferences_remote_debugging)
        assertCategoryWithText(R.string.mozilla_category)
        assertPreferenceWithText(R.string.preferences_about_page)
    }
}

private val recyclerView = withClassName(containsString("RecyclerView"))
private val textIsDisplayed = { str: Int -> selectedDescendantsMatch(withText(str), isDisplayed()) }
private val hasDescendantWithText = { str: Int -> hasDescendant(withText(str)) }
private val hasSwitchDescendant = hasDescendant(withClassName(containsString("SwitchCompat")))

private fun assertPreferenceWithText(@StringRes str: Int) =
        onView(recyclerView)
                .perform(scrollTo<ViewHolder>(hasDescendantWithText(str)))
                .check(textIsDisplayed(str))

private fun assertCategoryWithText(@StringRes str: Int) = assertPreferenceWithText(str)

private fun assertSwitchPreferenceWithText(@StringRes str: Int) =
        onView(recyclerView)
                .perform(scrollTo<ViewHolder>(allOf(hasDescendantWithText(str), hasSwitchDescendant)))
                .check(textIsDisplayed(str))
