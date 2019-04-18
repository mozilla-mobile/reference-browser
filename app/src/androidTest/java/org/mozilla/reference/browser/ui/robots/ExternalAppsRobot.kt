/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.matcher.ViewMatchers.withText

class ExternalAppsRobot {
    fun verifyAndroidDefaultApps() = defaultAppsLayout()
    fun verifyFxAQrCode() = fXAQrCode()

    class Transition {
        fun externalApps(interact: ExternalAppsRobot.() -> Unit): ExternalAppsRobot.Transition {
            return ExternalAppsRobot.Transition()
        }
    }
}

private fun defaultAppsLayout() = withText("Default apps")
private fun fXAQrCode() = withText("Pairing")
