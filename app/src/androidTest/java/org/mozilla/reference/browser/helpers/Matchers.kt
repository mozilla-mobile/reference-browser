/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.helpers

import android.view.View
import android.view.ViewGroup
import org.hamcrest.BaseMatcher
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.mozilla.reference.browser.ext.children
import androidx.test.espresso.matcher.ViewMatchers.isChecked as espressoIsChecked
import androidx.test.espresso.matcher.ViewMatchers.isEnabled as espressoIsEnabled
import androidx.test.espresso.matcher.ViewMatchers.isSelected as espressoIsSelected

/**
 * The [espressoIsEnabled] function that can also handle disabled state through the boolean argument.
 */
fun isEnabled(isEnabled: Boolean): Matcher<View> = maybeInvertMatcher(espressoIsEnabled(), isEnabled)

/**
 * The [espressoIsChecked] function that can also handle unchecked state through the boolean argument.
 */
fun isChecked(isChecked: Boolean): Matcher<View> = maybeInvertMatcher(espressoIsChecked(), isChecked)

/**
 * The [espressoIsSelected] function that can also handle not selected state through the boolean argument.
 */
fun isSelected(isSelected: Boolean): Matcher<View> = maybeInvertMatcher(espressoIsSelected(), isSelected)

/**
 * Matches a View if there is a single View, grandson of the same grandfather, that matches the
 * given matcher.
 *
 * @param matcher The matcher that our view cousins will be checked against to
 */
fun hasCousin(matcher: Matcher<View>): Matcher<View> = object : BaseMatcher<View>() {
    override fun describeTo(description: Description?) {
        description?.apply {
            appendText("has cousin that matches: ")
            matcher.describeTo(description)
        }
    }

    override fun matches(item: Any?): Boolean {
        val parent = (item as? View)?.parent
        val grandParent = parent?.parent as? ViewGroup
        return grandParent
            ?.children
            ?.filter { v -> v != parent && v is ViewGroup }
            ?.filter(matchChildren(matcher))
            ?.count() == 1
    }

    private fun matchChildren(matcher: Matcher<View>): (View?) -> Boolean = {
        (it as? ViewGroup)
            ?.children
            ?.filter { v -> matcher.matches(v) }
            ?.count() == 1
    }
}

private fun maybeInvertMatcher(matcher: Matcher<View>, useUnmodifiedMatcher: Boolean): Matcher<View> = when {
    useUnmodifiedMatcher -> matcher
    else -> not(matcher)
}
