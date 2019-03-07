/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.assertion.ViewAssertions.matches
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.helpers.click

/**
 * Implementation of Robot Pattern for the navigation toolbar  menu.
 */
class NavigationToolbarRobot {

    fun verifyNewTabAddressView() = newTabAddressText()
    fun checkNumberOfTabsTabCounter(numTabs: String) = numberOfOpenTabsTabCounter.check(matches(withText(numTabs)))

    class Transition {
        fun openThreeDotMenu(interact: ThreeDotMenuRobot.() -> Unit): ThreeDotMenuRobot.Transition {
            threeDotButton().click()
            ThreeDotMenuRobot().interact()
            return ThreeDotMenuRobot.Transition()
        }

        fun openTabTrayMenu(interact: TabTrayMenuRobot.() -> Unit): TabTrayMenuRobot.Transition {
            openTabTray().click()
            TabTrayMenuRobot().interact()
            return TabTrayMenuRobot.Transition()
        }
    }
}

fun navigationToolbar(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
    NavigationToolbarRobot().interact()
    return NavigationToolbarRobot.Transition()
}

private fun threeDotButton() = onView(ViewMatchers.withContentDescription("Menu"))
private fun openTabTray() = onView(ViewMatchers.withId(R.id.counter_box))
private fun newTabAddressText() = onView(ViewMatchers.withText("about:blank"))
private var numberOfOpenTabsTabCounter = onView(ViewMatchers.withId(R.id.counter_text))
