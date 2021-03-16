/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@file:Suppress("TooManyFunctions")

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
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
    fun verifyAddToHomescreenButtonExists() = assertAddToHomescreenButton()
    fun verifyFindInPageButtonExists() = assertFindInPageButton()
    fun verifyAddOnsButtonExists() = assertAddOnsButton()
    fun verifySyncedTabsButtonExists() = assertSyncedTabsButton()
    fun verifyReportIssueExists() = assertReportIssueButton()
    fun verifyOpenSettingsExists() = assertSettingsButton()

    fun verifyShareButtonDoesntExist() = assertShareButtonDoesntExist()
    fun verifyRequestDesktopSiteToggleDoesntExist() = assertRequestDesktopSiteToggleDoesntExist()
    fun verifyFindInPageButtonDoesntExist() = assertFindInPageButtonDoesntExist()
    fun verifyForwardButtonDoesntExist() = assertForwardButtonDoesntExist()
    fun verifyReloadButtonDoesntExist() = assertRefreshButtonDoesntExist()
    fun verifyStopButtonDoesntExist() = assertStopButtonDoesntExist()
    fun verifyAddToHomescreenButtonDoesntExist() = assertAddToHomescreenButtonDoesntExist()

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
    onView(withId(R.id.mozac_browser_menu_recyclerView))
            .check(matches(isDisplayed()))
}

private fun forwardButton() = onView(withContentDescription("Forward"))
private fun refreshButton() = onView(withContentDescription("Refresh"))
private fun stopButton() = onView(withContentDescription("Stop"))
private fun shareButton() = onView(withText("Share"))
private fun requestDesktopSiteToggle() = onView(withText("Request desktop site"))
private fun findInPageButton() = onView(withText("Find in Page"))
private fun reportIssueButton() = onView(withText("Report issue"))
private fun settingsButton() = onView(withText("Settings"))
private fun addToHomescreenButton() = onView(withText("Add to homescreen"))
private fun addOnsButton() = onView(withText("Add-ons"))
private fun syncedTabsButton() = onView(withText("Synced Tabs"))

private fun assertShareButtonDoesntExist() = shareButton()
        .check(ViewAssertions.doesNotExist())
private fun assertRequestDesktopSiteToggleDoesntExist() = requestDesktopSiteToggle()
        .check(ViewAssertions.doesNotExist())
private fun assertFindInPageButtonDoesntExist() = findInPageButton()
        .check(ViewAssertions.doesNotExist())
private fun assertForwardButtonDoesntExist() = forwardButton()
        .check(ViewAssertions.doesNotExist())
private fun assertRefreshButtonDoesntExist() = refreshButton()
        .check(ViewAssertions.doesNotExist())
private fun assertStopButtonDoesntExist() = stopButton()
        .check(ViewAssertions.doesNotExist())
private fun assertAddToHomescreenButtonDoesntExist() = addToHomescreenButton()
        .check(ViewAssertions.doesNotExist())

private fun assertForwardButton() = forwardButton()
        .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
private fun assertRefreshButton() = refreshButton()
        .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
private fun assertStopButton() = stopButton()
        .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
private fun assertShareButton() = shareButton()
        .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
private fun assertRequestDesktopSiteToggle() = requestDesktopSiteToggle()
        .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
private fun assertAddToHomescreenButton() = addToHomescreenButton()
        .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
private fun assertFindInPageButton() = findInPageButton()
        .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
private fun assertAddOnsButton() = addOnsButton()
        .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
private fun assertSyncedTabsButton() = syncedTabsButton()
        .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
private fun assertReportIssueButton() = reportIssueButton()
        .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
private fun assertSettingsButton() = settingsButton()
        .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
