/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.mozilla.reference.browser.ext.waitAndInteract
import org.mozilla.reference.browser.helpers.click

/**
 * Implementation of Robot Pattern for menu in tab tray that shows more options.
 * So far only Close All Tabs is implemented.
 */

class TabTrayMoreOptionsMenuRobot {

    fun verifyCloseAllTabsButton() = assertCloseAllTabsButton()
    fun verifyCloseAllPrivateTabsButton() = assertCloseAllPrivateTabsButton()

    class Transition {
        val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        fun closeAllTabs(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot {
            mDevice.waitForIdle()
            closeAllTabsButton().click()
            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot()
        }

        fun closeAllPrivateTabs(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
            mDevice.waitForIdle()
            closeAllPrivateTabsButton().click()
            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }
    }
}

private fun closeAllTabsButton() = onView(ViewMatchers.withText("Close All Tabs"))
private fun closeAllPrivateTabsButton() = onView(ViewMatchers.withText("Close Private Tabs"))
private fun assertCloseAllTabsButton() {
    val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    mDevice.waitAndInteract(Until.findObject(By.text("Close All Tabs"))) {}
}
private fun assertCloseAllPrivateTabsButton() = closeAllPrivateTabsButton()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
