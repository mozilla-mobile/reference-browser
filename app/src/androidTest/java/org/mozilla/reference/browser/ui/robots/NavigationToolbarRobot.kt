/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.helpers.click
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime

/**
 * Implementation of Robot Pattern for the navigation toolbar menu.
 */
class NavigationToolbarRobot {

    fun verifyNewTabAddressView() = assertNewTabAddressText()
    fun checkNumberOfTabsTabCounter(numTabs: String) = numberOfOpenTabsTabCounter.check(matches(withText(numTabs)))

    class Transition {

        val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        fun enterUrlAndEnterToBrowser(url: Uri, interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            mDevice.waitForIdle()
            urlBar().perform(click())
            awesomeBar().perform(replaceText(url.toString()),
                    pressImeActionButton())

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

        fun openThreeDotMenu(interact: ThreeDotMenuRobot.() -> Unit): ThreeDotMenuRobot.Transition {
            mDevice.wait(Until.findObject(By.text("Menu")), waitingTime)
            threeDotButton().click()
            ThreeDotMenuRobot().interact()
            return ThreeDotMenuRobot.Transition()
        }

        fun openTabTrayMenu(interact: TabTrayMenuRobot.() -> Unit): TabTrayMenuRobot.Transition {
            openTabTray().click()
            TabTrayMenuRobot().interact()
            return TabTrayMenuRobot.Transition()
        }
    }
}

fun navigationToolbar(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
    NavigationToolbarRobot().interact()
    return NavigationToolbarRobot.Transition()
}

private fun threeDotButton() = onView(ViewMatchers.withContentDescription("Menu"))
private fun openTabTray() = onView(ViewMatchers.withId(R.id.counter_box))
private var numberOfOpenTabsTabCounter = onView(ViewMatchers.withId(R.id.counter_text))
private fun urlBar() = onView(ViewMatchers.withId(R.id.mozac_browser_toolbar_url_view))
private fun awesomeBar() = onView(withId(R.id.mozac_browser_toolbar_edit_url_view))

private fun assertNewTabAddressText() {
    mDevice.wait(Until.findObject(By.text("Search or enter address")), waitingTime)
}
