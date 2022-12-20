/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.helpers.matchers

import android.view.View
import android.widget.TextView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.mozilla.reference.browser.R

/**
 * A custom matcher for finding tabs to match text within them.
 */
class TabMatcher<T : View>(
    val id: Int,
    val matcher: (T) -> Boolean,
) : TypeSafeMatcher<T>() {
    override fun describeTo(description: Description?) {
        description?.appendText("with expected tab item")
    }

    override fun matchesSafely(item: T): Boolean {
        val result = item.id == id

        // early return if the ID does not match makes the test run faster.
        if (!result) {
            return false
        }

        return matcher(item)
    }

    companion object {
        fun withText(text: String): Matcher<View> {
            return TabMatcher<View>(
                R.id.mozac_browser_tabstray_title,
            ) {
                (it as TextView).text == text
            }
        }
    }
}
