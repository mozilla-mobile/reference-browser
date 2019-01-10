/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots
import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.uiautomator.UiDevice
import org.mozilla.reference.browser.helpers.click


class NavigationToolbarRobot {

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    fun openMainMenu() = threeDotButton().click()
}

fun navigationToolbar(interact: NavigationToolbarRobot.() -> Unit) {
    NavigationToolbarRobot().interact()
}

private fun threeDotButton() = onView(ViewMatchers.withContentDescription("Menu"))

