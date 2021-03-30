/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.uiautomator.UiSelector
import junit.framework.Assert.assertTrue
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime

/**
 * Implementation of Robot Pattern for the Add to homescreen feature.
 */
class AddToHomeScreenRobot {

    fun clickCancelAddToHomeScreenButton() = cancelAddToHomeScreenButton().click()
    fun clickAddAutomaticallyToHomeScreenButton() = addAutomaticallyToHomeScreenButton().click()
    fun verifyAddToHomeScreenPopup() = assertAddToHomeScreenPopup()

    class Transition {
        fun openHomeScreenShortcut(title: String, interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            mDevice.findObject(UiSelector().text(title))
            mDevice.findObject((UiSelector().text(title))).clickAndWaitForNewWindow(waitingTime)

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }
    }

    private fun cancelAddToHomeScreenButton() = mDevice.findObject(UiSelector().textContains("CANCEL"))
    private fun addAutomaticallyToHomeScreenButton() = mDevice.findObject(UiSelector().textContains("ADD AUTOMATICALLY"))
    private fun assertAddToHomeScreenPopup() {
        assertTrue(mDevice.findObject(UiSelector().text("Touch & hold to place manually"))
            .waitForExists(waitingTime))
    }
}
