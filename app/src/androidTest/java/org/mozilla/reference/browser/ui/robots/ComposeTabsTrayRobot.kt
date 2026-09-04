/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import kotlin.math.abs
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
    fun verifyTabsTray() = assertExists(goBackButton)

    fun verifyTab(url: String) = assertExists(By.text(url))

    fun verifyNoTab(url: String) = assertDoesNotExist(By.text(url))

    fun verifyNoOpenTabs() = assertExists(By.text(appContext.getString(R.string.tabs_tray_no_tabs)))

    fun verifyTabThumbnail() = assertExists(By.desc(appContext.getString(R.string.tabs_tray_thumbnail)))

    fun verifyNormalTabsPage() = assertExists(normalTabsPage)

    fun verifyPrivateTabsPage() = assertExists(privateTabsPage)

    fun openNormalTabsPage() {
        requireNotNull(waitFor(normalTabsPage)).click()
    }

    fun openPrivateTabsPage() {
        requireNotNull(waitFor(privateTabsPage)).click()
    }

    fun swipeTabAway(url: String) {
        val centerY = rowCenterY(url)
        val startX = (mDevice.displayWidth * SWIPE_START_FRACTION).toInt()
        val endX = (mDevice.displayWidth * SWIPE_END_FRACTION).toInt()

        mDevice.swipe(startX, centerY, endX, centerY, SWIPE_STEPS)
    }

    fun closeTab(url: String) {
        val centerY = rowCenterY(url)

        mDevice.findObjects(closeTabButton).minBy { abs(it.visibleBounds.centerY() - centerY) }.click()
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

        fun openNewTab(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
            requireNotNull(waitFor(newTabButton)).click()

            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }

        fun closeAllTabs(interact: ComposeTabsTrayRobot.() -> Unit): ComposeTabsTrayRobot.Transition {
            requireNotNull(waitFor(moreOptionsButton)).click()
            requireNotNull(waitFor(By.text(appContext.getString(R.string.menu_action_close_tabs)))).click()

            ComposeTabsTrayRobot().interact()
            return Transition()
        }

        fun closeAllPrivateTabs(interact: ComposeTabsTrayRobot.() -> Unit): ComposeTabsTrayRobot.Transition {
            requireNotNull(waitFor(moreOptionsButton)).click()
            requireNotNull(waitFor(By.text(appContext.getString(R.string.menu_action_close_tabs_private)))).click()

            ComposeTabsTrayRobot().interact()
            return Transition()
        }
    }
}

fun composeTabsTray(interact: ComposeTabsTrayRobot.() -> Unit): ComposeTabsTrayRobot.Transition {
    ComposeTabsTrayRobot().interact()
    return ComposeTabsTrayRobot.Transition()
}

// Compose does not publish a tab row as a single node, only its title, URL and close button, so a row has to be
// addressed by the geometry of the text inside it.
//
// The swipe has to travel more than half the width of the row to pass the dismiss threshold, and it has to start and
// end away from the edges of the screen or the system back gesture swallows it before the app sees anything. The step
// count is deliberately low: UiDevice.swipe is documented as 5ms per step but is far slower than that on an emulator,
// and too slow a drag carries no velocity at release, so Compose settles it back instead of dismissing it.
private const val SWIPE_STEPS = 20
private const val SWIPE_START_FRACTION = 0.85
private const val SWIPE_END_FRACTION = 0.1

private val goBackButton = By.desc(appContext.getString(R.string.tabs_tray_go_back))

private val closeTabButton = By.desc(appContext.getString(R.string.tabs_tray_close_tab))

private val newTabButton = By.desc(appContext.getString(R.string.menu_action_add_tab))

private val moreOptionsButton = By.desc(appContext.getString(R.string.tabs_tray_more_options))

private val normalTabsPage = By.desc(appContext.getString(R.string.tabs_tray_normal_tabs))

private val privateTabsPage = By.desc(appContext.getString(R.string.tabs_tray_private_tabs))

private fun waitFor(selector: BySelector): UiObject2? = mDevice.wait(Until.findObject(selector), waitingTime)

private fun rowCenterY(url: String): Int = requireNotNull(waitFor(By.text(url))).visibleBounds.centerY()

private fun assertExists(selector: BySelector) = assertNotNull(waitFor(selector))

// Closing a tab is not instant: the dismiss animation has to settle, the store has to be dispatched to and the tray
// has to recompose, so wait for the row to go rather than asserting it has already gone.
private fun assertDoesNotExist(selector: BySelector) = assertTrue(mDevice.wait(Until.gone(selector), waitingTime))
