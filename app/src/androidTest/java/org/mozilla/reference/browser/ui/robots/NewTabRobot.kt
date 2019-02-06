/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers
import org.mozilla.reference.browser.R
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withText

/**
 * Implementation of Robot Pattern for new tab.
 */

class NewTabRobot {

    fun verifyNewTabView() = newTab()
    fun verifyNumberOfTabs() = numberOfTabs()

    fun checkNumberOfTabsTabCounter(numTabs: String) = numberOfOpenTabsTabCounter.check(matches(withText(numTabs)))
}

private fun newTab() = onView(ViewMatchers.withText("about:blank"))
private fun numberOfTabs() = onView(ViewMatchers.withId(R.id.counter_text)).check(matches(withText("2")))
private var numberOfOpenTabsTabCounter = onView(ViewMatchers.withId(R.id.counter_text))
