/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime
import org.mozilla.reference.browser.helpers.TestHelper.appContext

/**
 * Implementation of Robot Pattern for the Jetpack Compose tabs tray.
 *
 * The tray has no view IDs to match on because it is composed, so everything here goes through the accessibility tree
 * with UiAutomator instead of Espresso.
 */
class ComposeTabsTrayRobot {
    fun verifyTabsTray() = assertNotNull(mDevice.wait(Until.findObject(goBackButton), waitingTime))

    class Transition {
        fun goBackToBrowser(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
            mDevice.wait(Until.findObject(goBackButton), waitingTime).click()

            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }
    }
}

fun composeTabsTray(interact: ComposeTabsTrayRobot.() -> Unit): ComposeTabsTrayRobot.Transition {
    ComposeTabsTrayRobot().interact()
    return ComposeTabsTrayRobot.Transition()
}

private val goBackButton = By.desc(appContext.getString(R.string.tabs_tray_go_back))
