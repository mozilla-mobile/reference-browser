/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@file:Suppress("TooManyFunctions")

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.helpers.assertIsChecked
import org.mozilla.reference.browser.helpers.click
import java.util.regex.Pattern.matches

/**
 * Implementation of Robot Pattern for the tab tray  menu.
 */

class TabTrayMenuRobot {

    fun verifyRegularBrowsingButton() = regularBrowsingButton()
    fun verifyPrivateBrowsingButton() = privateBrowsingButton()
    fun verifyGoBackButton() = goBackButton()
    fun verifyNewTabButton() = newTabButton()
    fun verifyMenuButton() = menuButton()
    fun verifyCloseButtonInTabPreview() = closeTabButtonTabTray()
    fun verifyDefaultOpenTabTitle() = openTabTabTrayTitle()
    fun verifyDefaultOpenTabThumbnail() = openTabTabTrayThumbnail()
    fun verifyRegularBrowsingButton(regularBrowsingButtonChecked: Boolean) =
            regularBrowsingButton().assertIsChecked(regularBrowsingButtonChecked)
    fun verifyPrivateBrowsingButton(privateButtonChecked: Boolean) =
            privateBrowsingButton().assertIsChecked(privateButtonChecked)

    fun verifyThereAreNotPrivateTabsOpen() = privateTabs().check(doesNotExist())
    fun verifyThereIsOnePrivateTabOpen() = privateTabs().check(matches(isDisplayed()))
    fun verifyThereIsOneTabOpen() = regularTabs().check(matches(isDisplayed()))

    fun goBackFromTabTrayTest() = goBackButton().click()

    fun openRegularBrowsing() {
        regularBrowsingButton().click()
    }

    fun openPrivateBrowsing() {
        privateBrowsingButton().click()
    }

    class Transition {

        fun openNewTab(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
            newTabButton().click()
            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }

        fun openMoreOptionsMenu(interact: TabTrayMoreOptionsMenuRobot.() -> Unit):
                TabTrayMoreOptionsMenuRobot.Transition {
            menuButton().click()
            TabTrayMoreOptionsMenuRobot().interact()
            return TabTrayMoreOptionsMenuRobot.Transition()
        }

        fun goBackFromTabTray(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot {
            goBackButton().click()
            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot()
        }

        fun closeTabXButton(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
            closeTabButtonTabTray().click()
            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }
    }
}

private fun regularBrowsingButton() = onView(withId(R.id.button_tabs))
private fun privateBrowsingButton() = onView(withId(R.id.button_private_tabs))
private fun goBackButton() = onView(ViewMatchers.withContentDescription("back"))
private fun newTabButton() = onView(ViewMatchers.withContentDescription("Add New Tab"))
private fun menuButton() = onView(ViewMatchers.withContentDescription("More options"))
private fun closeTabButtonTabTray() = onView(withId(R.id.mozac_browser_tabstray_close))
private fun openTabTabTrayTitle() = onView(withId(R.id.mozac_browser_tabstray_url))
private fun openTabTabTrayThumbnail() = onView(withId(R.id.mozac_browser_tabstray_thumbnail))
private fun privateTabs() = onView(ViewMatchers.withText("Private Browsing"))
private fun regularTabs() = onView((ViewMatchers.withText("about:blank")))
