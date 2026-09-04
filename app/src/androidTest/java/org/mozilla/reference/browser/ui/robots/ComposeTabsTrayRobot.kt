/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTimeShort
import org.mozilla.reference.browser.helpers.TestHelper.appContext

/**
 * Implementation of Robot Pattern for the Jetpack Compose tabs tray.
 *
 * The tray has no view IDs to match on because it is composed, so everything here goes through the accessibility tree
 * with UiAutomator instead of Espresso.
 */
class ComposeTabsTrayRobot {
    fun verifyTabsTray() = assertExists(goBackButton)

    fun verifyTab(url: String) = assertExists(By.text(url))

    fun verifyNoTab(url: String) = assertDoesNotExist(By.text(url))

    fun verifyNoOpenTabs() = assertExists(By.text(appContext.getString(R.string.tabs_tray_no_tabs)))

    fun verifyTabThumbnail() = assertExists(By.desc(appContext.getString(R.string.tabs_tray_thumbnail)))

    fun closeTab(url: String) {
        // The close button is a sibling of the title and URL, so walk up to the row to find the right one.
        val row = requireNotNull(waitFor(By.text(url))).parent.parent
        row.findObject(closeTabButton).click()
    }

    class Transition {
        fun goBackToBrowser(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
            requireNotNull(waitFor(goBackButton)).click()

            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }

        fun selectTab(
            url: String,
            interact: BrowserRobot.() -> Unit,
        ): BrowserRobot.Transition {
            requireNotNull(waitFor(By.text(url))).click()

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }
    }
}

fun composeTabsTray(interact: ComposeTabsTrayRobot.() -> Unit): ComposeTabsTrayRobot.Transition {
    ComposeTabsTrayRobot().interact()
    return ComposeTabsTrayRobot.Transition()
}

private val goBackButton = By.desc(appContext.getString(R.string.tabs_tray_go_back))

private val closeTabButton = By.desc(appContext.getString(R.string.tabs_tray_close_tab))

private fun waitFor(selector: BySelector): UiObject2? = mDevice.wait(Until.findObject(selector), waitingTime)

private fun assertExists(selector: BySelector) = assertNotNull(waitFor(selector))

private fun assertDoesNotExist(selector: BySelector) =
    assertNull(mDevice.wait(Until.findObject(selector), waitingTimeShort))
