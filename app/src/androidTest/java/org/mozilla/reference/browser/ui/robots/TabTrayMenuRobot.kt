/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@file:Suppress("TooManyFunctions")

package org.mozilla.reference.browser.ui.robots

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.junit.Assert
import org.junit.Assert.assertNull
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.waitAndInteract
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTimeShort
import org.mozilla.reference.browser.helpers.TestHelper
import org.mozilla.reference.browser.helpers.assertIsSelected
import org.mozilla.reference.browser.helpers.click
import org.mozilla.reference.browser.helpers.matchers.TabMatcher

/**
 * Implementation of Robot Pattern for the tab tray menu.
 */

val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

class TabTrayMenuRobot {
    val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    fun verifyRegularBrowsingTab() = assertRegularBrowsingTabs()
    fun verifyPrivateBrowsingTab() = assertPrivateBrowsingTabs()
    fun verifyGoBackButton() = assertGoBackButton()
    fun verifyNewTabButton() = assertNewTabButton()
    fun verifyRegularBrowsingTab(isSelected: Boolean) =
        regularTabs().assertIsSelected(isSelected)
    fun verifyPrivateBrowsingTab(isSelected: Boolean) =
        privateTabs().assertIsSelected(isSelected)
    fun verifyThereAreNotPrivateTabsOpen() = assertThereAreNoPrivateTabsOpen()
    fun verifyThereIsOnePrivateTabOpen() = assertPrivateTabs()
    fun verifyThereIsOneTabOpen() = tab().check(matches(isDisplayed()))
    fun verifyExistingOpenTabs(title: String) = assertExistingOpenTabs(title)

    fun goBackFromTabTrayTest() = goBackButton().click()

    fun openRegularBrowsing() {
        regularTabs().click()
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

        fun openMoreOptionsMenu(context: Context, interact: TabTrayMoreOptionsMenuRobot.() -> Unit): TabTrayMoreOptionsMenuRobot.Transition {
            // The 3dot "More options" button is actually an Android Options Menu (check tabstray_menu.xml) not a View that we treat as a menu
            openActionBarOverflowOrOptionsMenu(context)

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

        fun clickOpenTab(title: String, interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            openTab(title).clickAndWaitForNewWindow()

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }
    }
}

private fun regularTabs() = onView(ViewMatchers.withContentDescription("Tabs"))
private fun privateTabs() = onView(ViewMatchers.withContentDescription("Private tabs"))
private fun goBackButton() = onView(ViewMatchers.withContentDescription("back"))
private fun newTabButton() = onView(ViewMatchers.withContentDescription("Add New Tab"))
private fun closeTabButtonTabTray() = onView(withId(R.id.mozac_browser_tabstray_close))
private fun tab() = onView(TabMatcher.withText("about:blank"))

private fun assertRegularBrowsingTabs() = regularTabs()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertPrivateBrowsingTabs() = privateTabs()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertGoBackButton() = goBackButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertNewTabButton() = newTabButton()
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

private fun assertExistingOpenTabs(title: String) {
    mDevice.waitForIdle()
    mDevice.findObject(UiSelector().resourceId("${TestHelper.packageName}:id/tabsTray"))
        .waitForExists(waitingTime)
    Assert.assertTrue(
        mDevice.findObject(UiSelector().textContains(title)).waitForExists(waitingTime),
    )
}

private fun openTab(title: String) = mDevice.findObject(UiSelector().textContains(title))
