/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime
import org.mozilla.reference.browser.helpers.TestHelper.packageName
import org.mozilla.reference.browser.helpers.click

/**
 *  Implementation of the robot pattern for Custom tabs
 */
class CustomTabRobot {

    fun verifyCloseButton() = assertCloseButton()
    fun verifyTrackingProtectionIcon() = assertTrackingProtectionIcon()
    fun verifySecurityIndicator() = assertSecurityIndicator()
    fun verifyPageTitle(title: String) = assertCustomTabTitle(title)
    fun verifyPageUrl(url: String) = assertCustomTabUrl(url)
    fun verifyActionButton() = assertActionButton()
    fun verifyMenuButton() = assertMenuButton()
    fun verifyForwardButton() = assertForwardButton()
    fun verifyRefreshButton() = assertRefreshButton()
    fun verifyStopButton() = assertStopButton()
    fun verifyShareButton() = assertShareButton()
    fun verifyRequestDesktopButton() = assertRequestDesktopButton()
    fun verifyFindInPageButton() = assertFindInPageButton()
    fun verifyOpenInBrowserButton() = assertOpenInBrowserButton()
    fun verifyRequestDesktopSiteIsTurnedOff() = assertRequestDesktopSiteIsTurnedOff()
    fun verifyRequestDesktopSiteIsTurnedOn() = assertRequestDesktopSiteIsTurnedOn()
    fun clickForwardButton() = forwardButton().click()

    fun clickGenericLink(expectedText: String) {
        mDevice.findObject(UiSelector().resourceId("$packageName:id/engineView"))
            .waitForExists(waitingTime)
        mDevice.findObject(UiSelector().textContains(expectedText)).waitForExists(waitingTime)
        val link = mDevice.findObject(By.textContains(expectedText))
        link.click()
    }

    @Suppress("SwallowedException")
    fun switchRequestDesktopSiteToggle() {
        try {
            // Click the Request desktop site toggle
            mDevice.findObject(UiSelector().textContains("Request desktop site"))
                .waitForExists(waitingTime)
            requestDesktopButton().click()
            mDevice.waitForIdle()
            assertTrue(
                mDevice.findObject(
                    UiSelector()
                        .resourceId("$packageName:id/mozac_browser_menu_recyclerView"),
                ).waitUntilGone(waitingTime),
            )
        } catch (e: AssertionError) {
            println("Failed to click request desktop toggle")
            // If the click didn't succeed the main menu remains displayed and should be dismissed
            mDevice.pressBack()
            customTabScreen {
            }.openMainMenu {
            }
            // Click again the Request desktop site toggle
            mDevice.findObject(UiSelector().textContains("Request desktop site"))
                .waitForExists(waitingTime)
            requestDesktopButton().click()
            mDevice.waitForIdle()
        }
    }

    class Transition {
        fun openMainMenu(interact: CustomTabRobot.() -> Unit): Transition {
            mDevice.findObject(UiSelector().resourceId("$packageName:id/mozac_browser_toolbar_menu"))
                .waitForExists(waitingTime)

            menuButton().click()

            CustomTabRobot().interact()
            return Transition()
        }

        fun goBack(interact: CustomTabRobot.() -> Unit): Transition {
            mDevice.pressBack()

            CustomTabRobot().interact()
            return Transition()
        }

        fun clickShareButton(interact: ContentPanelRobot.() -> Unit): ContentPanelRobot.Transition {
            shareButton().click()

            ContentPanelRobot().interact()
            return ContentPanelRobot.Transition()
        }

        fun clickOpenInBrowserButton(interact: BrowserRobot.() -> Unit): Transition {
            openInBrowserButton().click()

            BrowserRobot().interact()
            return Transition()
        }
    }
}

fun customTabScreen(interact: CustomTabRobot.() -> Unit): CustomTabRobot.Transition {
    mDevice.findObject(UiSelector().resourceId("$packageName:id/toolbar")).waitForExists(waitingTime)
    CustomTabRobot().interact()
    return CustomTabRobot.Transition()
}

private fun closeButton() = onView(withId(R.id.mozac_browser_toolbar_navigation_actions))
private fun trackingProtectionIcon() = onView(withId(R.id.mozac_browser_toolbar_tracking_protection_indicator))
private fun securityIndicator() = onView(withId(R.id.mozac_browser_toolbar_security_indicator))
private fun menuButton() = onView(withId(R.id.mozac_browser_toolbar_menu))
private fun actionButton() = onView(withContentDescription("Share link"))
private fun forwardButton() = onView(withContentDescription("Forward"))
private fun refreshButton() = onView(withContentDescription("Refresh"))
private fun stopButton() = onView(withContentDescription("Stop"))
private fun shareButton() = mDevice.findObject(UiSelector().textContains("Share"))
private fun requestDesktopButton() = onView(withSubstring("Request desktop site"))
private fun findInPage() = onView(withText("Find in Page"))
private fun openInBrowserButton() = onView(withText("Open in Browser"))

private fun assertCloseButton() =
    closeButton().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertTrackingProtectionIcon() =
    trackingProtectionIcon().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSecurityIndicator() =
    securityIndicator().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertMenuButton() =
    menuButton().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertActionButton() =
    actionButton().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertCustomTabTitle(title: String) {
    mDevice.findObject(UiSelector().resourceId("$packageName:id/mozac_browser_toolbar_title_view"))
        .waitForExists(waitingTime)
    assertTrue(mDevice.findObject(UiSelector().textContains(title)).waitForExists(waitingTime))
}
private fun assertCustomTabUrl(url: String) {
    mDevice.findObject(UiSelector().resourceId("$packageName:id/mozac_browser_toolbar_url_view"))
        .waitForExists(waitingTime)
    assertTrue(mDevice.findObject(UiSelector().textContains(url)).waitForExists(waitingTime))
}
private fun assertForwardButton() =
    forwardButton().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertRefreshButton() =
    refreshButton().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertStopButton() =
    stopButton().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertShareButton() =
    assertTrue(shareButton().waitForExists(waitingTime))
private fun assertRequestDesktopButton() =
    requestDesktopButton().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertFindInPageButton() =
    findInPage().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertOpenInBrowserButton() =
    openInBrowserButton().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertRequestDesktopSiteIsTurnedOff() {
    assertFalse(
        mDevice.findObject(UiSelector().textContains("Request desktop site")).isChecked,
    )
}
private fun assertRequestDesktopSiteIsTurnedOn() {
    assertTrue(
        mDevice.findObject(UiSelector().textContains("Request desktop site")).isChecked,
    )
}
