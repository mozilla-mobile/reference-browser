/* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@file:Suppress("TooManyFunctions")

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.helpers.click

/**
 * Implementation of Robot Pattern for the settings menu.
 */
class SettingsViewRobot {
    fun verifySettingsViewExists() = assertSettingsView()
    fun verifyNavigateUp() = navigateUpButton()
    fun verifySyncSigninButton() = syncSigninButton()
    fun verifySyncHistorySummary() = syncHistorySummary()
    fun verifySyncQrCodeButton() = syncQrCodeButton()
    fun verifySyncQrSummary() = syncQrSummary()
    fun verifyPrivacyButton() = privacyButton()
    fun verifyPrivacySummary() = privacySummary()
    fun verifyMakeDefaultBrowserButton() = makeDefaultBrowserButton()
    fun verifyDeveloperToolsHeading() = developerToolsHeading()
    fun verifyRemoteDebuggingText() = remoteDebuggingText()
    fun verifyRemoteDebuggingToggle() = remoteDebuggingToggle()
    fun verifyMozillaHeading() = mozillaHeading()
    fun verifyAboutReferenceBrowserButton() = aboutReferenceBrowserButton()

    // toggleRemoteDebugging does not yet verify that the debug service is started
    // server runs on port 6000
    fun toggleRemoteDebuggingOn() = {
        Espresso.onView(ViewMatchers.withText("OFF")).check(matches(isDisplayed()))
        remoteDebuggingToggle().click()
        Espresso.onView(ViewMatchers.withText("ON")).check(matches(isDisplayed()))
    }

    fun toggleRemoteDebuggingOff() = {
        Espresso.onView(ViewMatchers.withText("ON")).check(matches(isDisplayed()))
        remoteDebuggingToggle().click()
        Espresso.onView(ViewMatchers.withText("OFF")).check(matches(isDisplayed()))
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
    Espresso.onView(ViewMatchers.withText("Settings"))
    Espresso.onView(ViewMatchers.withText("About Reference Browser"))
}

private fun navigateUpButton() = Espresso.onView(ViewMatchers.withContentDescription("Navigate up"))
private fun syncSigninButton() = Espresso.onView(ViewMatchers.withText("Sign in"))
private fun syncHistorySummary() = Espresso.onView(ViewMatchers.withText("Sync your history"))
private fun syncQrCodeButton() = Espresso.onView(ViewMatchers.withText("Sign in with a QR code"))
private fun syncQrSummary() = Espresso.onView(ViewMatchers.withText("Pair with Firefox Desktop"))
private fun privacyButton() = Espresso.onView(ViewMatchers.withText("Privacy"))
private fun privacySummary() = Espresso.onView(ViewMatchers.withText("Tracking, cookies, data choices"))
private fun makeDefaultBrowserButton() = Espresso.onView(ViewMatchers.withText("Make default browser"))
private fun developerToolsHeading() = Espresso.onView(ViewMatchers.withText("Developer tools"))
private fun remoteDebuggingText() = Espresso.onView(ViewMatchers.withText("Remote debugging via USB"))
private fun remoteDebuggingToggle() = Espresso.onView(ViewMatchers.withId(R.id.switchWidget))
private fun mozillaHeading() = Espresso.onView(ViewMatchers.withText("Mozilla"))
private fun aboutReferenceBrowserButton() = Espresso.onView(ViewMatchers.withText("About Reference Browser"))
