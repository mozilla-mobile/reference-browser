/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@file:Suppress("TooManyFunctions")

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNull
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.waitAndInteract
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTimeShort
import org.mozilla.reference.browser.helpers.assertIsChecked
import org.mozilla.reference.browser.helpers.click

/**
 * Implementation of Robot Pattern for the tab tray menu.
 */

val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

class TabTrayMenuRobot {
    val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    fun verifyRegularBrowsingButton() = assertRegularBrowsingButton()
    fun verifyPrivateBrowsingButton() = assertPrivateBrowsingButton()
    fun verifyGoBackButton() = assertGoBackButton()
    fun verifyNewTabButton() = assertNewTabButton()
    fun verifyMenuButton() = assertMenuButton()
    fun verifyRegularBrowsingButton(regularBrowsingButtonChecked: Boolean) =
            regularBrowsingButton().assertIsChecked(regularBrowsingButtonChecked)
    fun verifyPrivateBrowsingButton(privateButtonChecked: Boolean) =
            privateBrowsingButton().assertIsChecked(privateButtonChecked)
    fun verifyThereAreNotPrivateTabsOpen() = assertThereAreNoPrivateTabsOpen()
    fun verifyThereIsOnePrivateTabOpen() = assertPrivateTabs()
    fun verifyThereIsOneTabOpen() = regularTabs().check(matches(isDisplayed()))

    fun goBackFromTabTrayTest() = goBackButton().click()

    fun openRegularBrowsing() {
        regularBrowsingButton().click()
    }

    fun openPrivateBrowsing() {
        mDevice.waitAndInteract(Until.findObject(By.desc("Private tabs"))) {
            click()
        }
    }

    class Transition {

        fun openNewTab(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
            newTabButton().click()
            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }

        fun openMoreOptionsMenu(interact: TabTrayMoreOptionsMenuRobot.() -> Unit): TabTrayMoreOptionsMenuRobot.Transition {
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
private fun regularTabs() = onView((ViewMatchers.withText("about:blank")))

private fun assertRegularBrowsingButton() = regularBrowsingButton()
        .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertPrivateBrowsingButton() = privateBrowsingButton()
        .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertGoBackButton() = goBackButton()
        .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertNewTabButton() = newTabButton()
        .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertMenuButton() = menuButton()
        .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertPrivateTabs() {
    mDevice.wait(Until.findObject(By.text("Private Browsing")), waitingTime)
}
private fun assertThereAreNoPrivateTabsOpen() {
    val obj = mDevice.wait(Until.findObject(By.text("Private Browsing")), waitingTimeShort)
    try {
        assertNull(obj)
    } finally {
        obj?.recycle()
    }
}
