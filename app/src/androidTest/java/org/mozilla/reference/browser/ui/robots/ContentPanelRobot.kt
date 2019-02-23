/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.InstrumentationRegistry
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.uiautomator.UiDevice

/**
 * Implementation of Robot Pattern for the Content Panel.
 */
class ContentPanelRobot {

    fun verifyContentPanel() = contentPanel()

    class Transition {
        private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        fun exitToNavigationToolbar(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
            device.pressBack()

            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }
    }
}

fun contentPanel(interact: ContentPanelRobot.() -> Unit) {
    ContentPanelRobot().interact()
}

private fun contentPanel() = onView(withText("Share with..."))
