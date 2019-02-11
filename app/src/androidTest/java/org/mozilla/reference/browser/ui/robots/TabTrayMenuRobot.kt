/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.InstrumentationRegistry
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.uiautomator.UiDevice
import org.mozilla.reference.browser.helpers.click
import org.mozilla.reference.browser.R

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

    class Transition {
        private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        fun exitToHomeScreen() {
            device.pressBack()
        }

        fun openNewTab(interact: NewTabRobot.() -> Unit): NewTabRobot {
            newTabButton().click()
            NewTabRobot().interact()
            return NewTabRobot()
        }

        fun openMoreOptionsMenu(interact: TabTrayMoreOptionsMenuRobot.() -> Unit): TabTrayMoreOptionsMenuRobot.Transition {
            menuButton().click()
            TabTrayMoreOptionsMenuRobot().interact()
            return TabTrayMoreOptionsMenuRobot.Transition()
        }
    }
}

fun tabTray(interact: TabTrayMenuRobot.() -> Unit) {
    TabTrayMenuRobot().interact()
}

private fun regularBrowsingButton() = onView(withId(R.id.button_tabs))
private fun privateBrowsingButton() = onView(withId(R.id.button_private_tabs))
private fun goBackButton() = onView(ViewMatchers.withContentDescription("back"))
private fun newTabButton() = onView(ViewMatchers.withContentDescription("Add New Tab"))
private fun menuButton() = onView(ViewMatchers.withContentDescription("More options"))
private fun closeTabButtonTabTray() = onView(withId(R.id.mozac_browser_tabstray_close))
private fun openTabTabTrayTitle() = onView(withId(R.id.mozac_browser_tabstray_url))
private fun openTabTabTrayThumbnail() = onView(withId(R.id.mozac_browser_tabstray_thumbnail))
