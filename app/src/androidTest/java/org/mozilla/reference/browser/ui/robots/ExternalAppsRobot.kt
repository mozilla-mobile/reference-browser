/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTimeShort

/**
 * Implementation of Robot Pattern for any non-Reference Browser (external) apps.
 */
class ExternalAppsRobot {
    fun verifyAndroidDefaultApps() = assertDefaultAppsLayout()
    fun verifyFxAQrCode() = assertFXAQrCode()

    class Transition {
        fun externalApps(interact: ExternalAppsRobot.() -> Unit): ExternalAppsRobot.Transition {
            return ExternalAppsRobot.Transition()
        }
    }
}

private fun fXAQrCode() = onView(ViewMatchers.withText("Pairing"))

private fun assertDefaultAppsLayout() {
    mDevice.wait(Until.findObject(By.text("Default apps")), waitingTimeShort)
}
private fun assertFXAQrCode() = fXAQrCode()
        .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
