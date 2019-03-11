/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers
import org.mozilla.reference.browser.helpers.click

/**
 * Implementation of Robot Pattern for the menun in tab tray that shows more options.
 * So far only Close All Tabs is implemented.
 */

class TabTrayMoreOptionsMenuRobot {

    fun verifyCloseAllTabsButton() = closeAllTabsButton()
    fun verifyCloseAllPrivateTabsButton() = closeAllPrivateTabsButton()

    class Transition {

        fun closeAllTabs(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot {
            closeAllTabsButton().click()
            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot()
        }

        fun closeAllPrivateTabs(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
            closeAllPrivateTabsButton().click()
            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }
    }
}

private fun closeAllTabsButton() = onView(ViewMatchers.withText("Close All Tabs"))
private fun closeAllPrivateTabsButton() = onView(ViewMatchers.withText("Close Private Tabs"))
