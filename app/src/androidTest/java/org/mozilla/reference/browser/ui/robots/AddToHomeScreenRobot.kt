/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.uiautomator.UiSelector
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime

/**
 * Implementation of Robot Pattern for the Add to homescreen feature.
 */
class AddToHomeScreenRobot {

    fun clickCancelAddToHomeScreenButton() {
        cancelAddToHomeScreenButton().waitForExists(waitingTime)
        cancelAddToHomeScreenButton().click()
    }
    fun clickAddAutomaticallyToHomeScreenButton() {
        addAutomaticallyToHomeScreenButton().waitForExists(waitingTime)
        addAutomaticallyToHomeScreenButton().click()
    }

    class Transition {
        fun openHomeScreenShortcut(title: String, interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            mDevice.findObject(UiSelector().textContains(title)).waitForExists(waitingTime)
            mDevice.findObject((UiSelector().textContains(title))).clickAndWaitForNewWindow(waitingTime)

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }
    }

    private fun cancelAddToHomeScreenButton() = mDevice.findObject(UiSelector().textContains("CANCEL"))
    private fun addAutomaticallyToHomeScreenButton() = mDevice.findObject(UiSelector().textContains("ADD AUTOMATICALLY"))
}
