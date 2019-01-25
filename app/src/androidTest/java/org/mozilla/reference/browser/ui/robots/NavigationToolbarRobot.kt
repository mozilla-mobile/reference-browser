/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers
import org.mozilla.reference.browser.helpers.click

/**
 * Implementation of Robot Pattern for the navigation toolbar  menu.
 */
class NavigationToolbarRobot {

    class Transition {
        fun openThreeDotMenu(interact: ThreeDotMenuRobot.() -> Unit): ThreeDotMenuRobot.Transition {
            threeDotButton().click()
            ThreeDotMenuRobot().interact()
            return ThreeDotMenuRobot.Transition()
        }
    }
}

fun navigationToolbar(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
    NavigationToolbarRobot().interact()
    return NavigationToolbarRobot.Transition()
}

private fun threeDotButton() = onView(ViewMatchers.withContentDescription("Menu"))
