/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import android.os.Build
import androidx.test.uiautomator.UiSelector
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTimeShort

/**
 * Implementation of Robot Pattern for the Add to homescreen feature.
 */
class AddToHomeScreenRobot {
    fun clickCancelAddToHomeScreenButton() {
        cancelAddToHomeScreenButton().waitForExists(waitingTime)
        cancelAddToHomeScreenButton().click()
    }

    fun clickSystemHomeScreenShortcutAddButton() {
        when (Build.VERSION.SDK_INT) {
            // For Android Oreo(API 26) to Android 11(API 30), click the "Add Automatically" button
            in Build.VERSION_CODES.O..Build.VERSION_CODES.R -> clickAddAutomaticallyButton()

            // For Android 12(API 31) to Vanilla Ice Cream(API 35), click the "Add to Home Screen" button
            in Build.VERSION_CODES.S..Build.VERSION_CODES.VANILLA_ICE_CREAM -> clickAddToHomeScreenButton()
        }
    }

    fun clickAddAutomaticallyButton() {
        addAutomaticallyToHomeScreenButton().click()
    }

    fun clickAddToHomeScreenButton() {
        mDevice.findObject(UiSelector().textContains("Add to home screen")).waitForExists(waitingTime)
        mDevice.findObject(UiSelector().textContains("Add to home screen")).clickAndWaitForNewWindow(waitingTimeShort)
    }

    class Transition {
        fun openHomeScreenShortcut(
            title: String,
            interact: BrowserRobot.() -> Unit,
        ): BrowserRobot.Transition {
            mDevice.findObject(UiSelector().textContains(title)).waitForExists(waitingTime)
            mDevice.findObject((UiSelector().textContains(title))).clickAndWaitForNewWindow(waitingTime)

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }
    }

    private fun cancelAddToHomeScreenButton() = mDevice.findObject(UiSelector().textContains("CANCEL"))

    private fun addAutomaticallyToHomeScreenButton() =
        mDevice.findObject(UiSelector().textContains("ADD AUTOMATICALLY"))
}
