/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@file:Suppress("TooManyFunctions")

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.uiautomator.UiDevice
import org.mozilla.reference.browser.helpers.click
import org.mozilla.reference.browser.R

/**
 * Implementation of Robot Pattern for three dot menu.
 */
class ThreeDotMenuRobot {

    fun verifyThreeDotMenuExists() = threeDotMenuRecyclerViewExists()

    fun verifyForwardButtonExists() = assertForwardButton()
    fun verifyReloadButtonExists() = assertRefreshButton()
    fun verifyStopButtonExists() = assertStopButton()
    fun verifyShareButtonExists() = assertShareButton()
    fun verifyRequestDesktopSiteToggleExists() = assertRequestDesktopSiteToggle()
    fun verifyFindInPageButtonExists() = assertFindInPageButton()
    fun verifyReportIssueExists() = assertReportIssueButton()
    fun verifyOpenSettingsExists() = assertSettingsButton()
    fun verifyShareButtonDoesntExist() = assertShareButtonDoesntExist()
    fun verifyRequestDesktopSiteToggleDoesntExist() = assertRequestDesktopSiteToggleDoesntExist()
    fun verifyFindInPageButtonDoesntExist() = assertFindInPageButtonDoesntExist()
    fun verifyForwardButtonDoesntExist() = assertForwardButtonDoesntExist()
    fun verifyReloadButtonDoesntExist() = assertRefreshButtonDoesntExist()
    fun verifyStopButtonDoesntExist() = assertStopButtonDoesntExist()

    class Transition {

        fun goForward(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            forwardButton().click()
            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

        fun refreshPage(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            refreshButton().click()
            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

        fun doStop(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
            stopButton().click()
            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }

        fun openShare(interact: ContentPanelRobot.() -> Unit): ContentPanelRobot.Transition {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

            shareButton().click()
            device.pressBack()
            ContentPanelRobot().interact()
            return ContentPanelRobot.Transition()
        }

        fun requestDesktopSite(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
            requestDesktopSiteToggle().click()
            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }

        fun findInPage(interact: FindInPagePanelRobot.() -> Unit): FindInPagePanelRobot.Transition {
            findInPageButton().click()
            FindInPagePanelRobot().interact()
            return FindInPagePanelRobot.Transition()
        }

        fun reportIssue(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            reportIssueButton().click()
            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

        fun openSettings(interact: SettingsViewRobot.() -> Unit): SettingsViewRobot.Transition {
            settingsButton().click()
            SettingsViewRobot().interact()
            return SettingsViewRobot.Transition()
        }
    }
}

private fun threeDotMenuRecyclerViewExists() {
    onView(withId(R.id.mozac_browser_menu_recyclerView)).check(matches(isDisplayed()))
    reportIssueButton()
}

private fun forwardButton() = onView(ViewMatchers.withContentDescription("Forward"))
private fun refreshButton() = onView(ViewMatchers.withContentDescription("Refresh"))
private fun stopButton() = onView(ViewMatchers.withContentDescription("Stop"))
private fun shareButton() = onView(ViewMatchers.withText("Share"))
private fun requestDesktopSiteToggle() = onView(ViewMatchers.withText("Request desktop site"))
private fun findInPageButton() = onView(ViewMatchers.withText("Find in Page"))
private fun reportIssueButton() = onView(ViewMatchers.withText("Report issue"))
private fun settingsButton() = onView(ViewMatchers.withText("Settings"))
private fun assertShareButtonDoesntExist() = shareButton().check(ViewAssertions.doesNotExist())
private fun assertRequestDesktopSiteToggleDoesntExist() =
        requestDesktopSiteToggle().check(ViewAssertions.doesNotExist())
private fun assertFindInPageButtonDoesntExist() = findInPageButton().check(ViewAssertions.doesNotExist())
private fun assertForwardButtonDoesntExist() = forwardButton().check(ViewAssertions.doesNotExist())
private fun assertRefreshButtonDoesntExist() = refreshButton().check(ViewAssertions.doesNotExist())
private fun assertStopButtonDoesntExist() = stopButton().check(ViewAssertions.doesNotExist())

private fun assertForwardButton() = forwardButton()
        .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertRefreshButton() = refreshButton()
        .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertStopButton() = stopButton()
        .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertShareButton() = shareButton()
        .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertRequestDesktopSiteToggle() = requestDesktopSiteToggle()
        .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertFindInPageButton() = findInPageButton()
        .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertReportIssueButton() = reportIssueButton()
        .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSettingsButton() = settingsButton()
        .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
