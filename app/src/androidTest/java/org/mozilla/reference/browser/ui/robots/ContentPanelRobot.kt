/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice

/**
 * Implementation of Robot Pattern for the content panel.
 */
class ContentPanelRobot {
    fun verifyContentPanel() = shareContentPanel()

    class Transition {
        val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        fun contentPanel(interact: ContentPanelRobot.() -> Unit): ContentPanelRobot.Transition {
            mDevice.waitForIdle()
            return ContentPanelRobot.Transition()
        }
    }
}

private fun shareContentPanel() = onView((ViewMatchers.withText("Share")))
