/* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@file:Suppress("TooManyFunctions")

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.hamcrest.CoreMatchers.allOf
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.helpers.TestAssetHelper
import org.mozilla.reference.browser.helpers.click
import org.mozilla.reference.browser.helpers.hasCousin

/**
 * Implementation of Robot Pattern for the settings menu.
 */
class SettingsViewRobot {
    fun verifySettingsViewExists() = assertSettingsView()

    fun verifyNavigateUp() = assertNavigateUpButton()
    fun verifySyncSigninButton() = assertSyncSigninButton()
    fun verifySyncHistorySummary() = assertSyncHistorySummary()
    fun verifySyncQrCodeButton() = assertSyncQrCodeButton()
    fun verifySyncQrSummary() = assertSyncQrSummary()
    fun verifyPrivacyButton() = assertPrivacyButton()
    fun verifyPrivacySummary() = assertPrivacySummary()
    fun verifyOpenLinksInApps() = assertOpenLinksInApps()
    fun verifyMakeDefaultBrowserButton() = assertMakeDefaultBrowserButton()
    fun verifyDeveloperToolsHeading() = assertDeveloperToolsHeading()
    fun verifyRemoteDebugging() = assertRemoteDebugging()
    fun verifyMozillaHeading() = assertMozillaHeading()
    fun verifyAboutReferenceBrowserButton() = assertAboutReferenceBrowserButton()

    // toggleRemoteDebugging does not yet verify that the debug service is started
    // server runs on port 6000
    fun toggleRemoteDebuggingOn() = {
        Espresso.onView(withText("OFF")).check(matches(isDisplayed()))
        remoteDebuggingToggle().click()
        Espresso.onView(withText("ON")).check(matches(isDisplayed()))
    }

    fun toggleRemoteDebuggingOff() = {
        Espresso.onView(withText("ON")).check(matches(isDisplayed()))
        remoteDebuggingToggle().click()
        Espresso.onView(withText("OFF")).check(matches(isDisplayed()))
    }

    class Transition {
        fun openSettingsViewPrivacy(interact: SettingsViewPrivacyRobot.() -> Unit):
                SettingsViewPrivacyRobot.Transition {
            privacyButton().click()
            SettingsViewPrivacyRobot().interact()
            return SettingsViewPrivacyRobot.Transition()
        }

        fun openFXASignin(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            syncSigninButton().click()
            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

        fun openFXAQrCode(interact: ExternalAppsRobot.() -> Unit): ExternalAppsRobot.Transition {
            syncQrCodeButton().click()
            ExternalAppsRobot().interact()
            return ExternalAppsRobot.Transition()
        }

        fun makeDefaultBrowser(interact: ExternalAppsRobot.() -> Unit):
                ExternalAppsRobot.Transition {
            makeDefaultBrowserButton().click()
            ExternalAppsRobot().interact()
            return ExternalAppsRobot.Transition()
        }

        fun openAboutReferenceBrowser(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            aboutReferenceBrowserButton().click()
            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }
    }
}

private fun assertSettingsView() {
    // verify that we are in the correct settings view
    Espresso.onView(withText("Settings"))
    Espresso.onView(withText("About Reference Browser"))
}

private fun syncSigninButton() = Espresso.onView(withText("Sign in"))
private fun syncHistorySummary() = Espresso.onView(withText("Sync your history"))
private fun syncQrCodeButton() = Espresso.onView(withText("Sign in with a QR code"))
private fun syncQrSummary() = Espresso.onView(withText("Pair with Firefox Desktop"))
private fun privacyButton() = Espresso.onView(withText("Privacy"))
private fun privacySummary() = Espresso.onView(withText("Tracking, cookies, data choices"))
private fun openLinksInAppsToggle() = Espresso.onView(allOf(withId(R.id.switchWidget), hasCousin(withText("Open links in apps"))))
private fun makeDefaultBrowserButton() = Espresso.onView(withText("Make default browser"))
private fun developerToolsHeading() = Espresso.onView(withText("Developer tools"))
private fun remoteDebuggingToggle() = Espresso.onView(allOf(withId(R.id.switchWidget), hasCousin(withText("Remote debugging via USB"))))
private fun mozillaHeading() = Espresso.onView(withText("Mozilla"))
private fun aboutReferenceBrowserButton() = Espresso.onView(withText("About Reference Browser"))

private fun assertNavigateUpButton() {
    mDevice.wait(Until.findObject(By.text("Navigate up")), TestAssetHelper.waitingTimeShort)
}
private fun assertSyncSigninButton() = syncSigninButton()
        .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSyncHistorySummary() = syncHistorySummary()
        .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSyncQrCodeButton() = syncQrCodeButton()
        .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSyncQrSummary() = syncQrSummary()
        .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertPrivacyButton() = privacyButton()
        .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertPrivacySummary() = privacySummary()
        .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertOpenLinksInApps() = openLinksInAppsToggle()
        .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertMakeDefaultBrowserButton() = makeDefaultBrowserButton()
        .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertDeveloperToolsHeading() = developerToolsHeading()
        .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertRemoteDebugging() = remoteDebuggingToggle()
        .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertMozillaHeading() = mozillaHeading()
        .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertAboutReferenceBrowserButton() = aboutReferenceBrowserButton()
        .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
