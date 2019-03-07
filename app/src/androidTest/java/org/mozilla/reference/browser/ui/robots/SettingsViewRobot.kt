/* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.uiautomator.UiDevice
import org.mozilla.reference.browser.helpers.click

/**
 * Implementation of Robot Pattern for the settings menu.
 */
class SettingsViewRobot {

    fun verifySettingsViewExists() = assertSettingsView()
    fun verifyNavigateUp() = navigateUpButton()
    fun verifySyncSigninButton() = syncSigninButton()
    fun verifySyncHistorySummary() = syncHistorySummary()
    fun verifyPrivacyButton() = privacyButton()
    fun verifyPrivacySummary() = privacySummary()
    fun verifyMakeDefaultBrowserButton() = makeDefaultBrowserButton()
    fun verifyDeveloperToolsHeading() = developerToolsHeading()
    fun verifyRemoteDebuggingToggle() = remoteDebuggingToggle()
    fun verifyMozillaHeading() = mozillaHeading()
    fun verifyAboutReferenceBrowserButton() = aboutReferenceBrowserButton()

    class Transition {
        private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        fun exitToHomeScreen() {
            device.pressBack()
        }
        fun openSettingsViewPrivacy(interact: SettingsViewPrivacyRobot.() -> Unit): SettingsViewPrivacyRobot.Transition {
            privacyButton().click()
            SettingsViewPrivacyRobot().interact()
            return SettingsViewPrivacyRobot.Transition()
        }
    }
}

fun settingsView(interact: SettingsViewRobot.() -> Unit) {
    SettingsViewRobot().interact()
}

private fun assertSettingsView() {
    // verify that we are in the correct settings view
    Espresso.onView(ViewMatchers.withText("Settings"))
    Espresso.onView(ViewMatchers.withText("About Reference Browser"))
}

private fun navigateUpButton() = Espresso.onView(ViewMatchers.withContentDescription("Navigate up"))
private fun syncSigninButton() = Espresso.onView(ViewMatchers.withText("Sign in"))
private fun syncHistorySummary() = Espresso.onView(ViewMatchers.withText("Sync your history"))
private fun privacyButton() = Espresso.onView(ViewMatchers.withText("Privacy"))
private fun privacySummary() = Espresso.onView(ViewMatchers.withText("Tracking, cookies, data choices"))
private fun makeDefaultBrowserButton() = Espresso.onView(ViewMatchers.withText("Make default browser"))
private fun developerToolsHeading() = Espresso.onView(ViewMatchers.withText("Developer tools"))
private fun remoteDebuggingToggle() = Espresso.onView(ViewMatchers.withText("Remote debugging via USB"))
private fun mozillaHeading() = Espresso.onView(ViewMatchers.withText("Mozilla"))
private fun aboutReferenceBrowserButton() = Espresso.onView(ViewMatchers.withText("About Reference Browser"))
