/* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.uiautomator.UiDevice

/**
 * Implementation of Robot Pattern for the settings menu.
 */
class SettingsViewRobot {

    fun assertSettingsView() = verifySettingsView()
    fun assertNavigateUp() = navigateUpButton()
    fun assertSyncSigninButton() = syncSigninButton()
    fun assertSyncHistorySummary() = syncHistorySummary()
    fun assertUseTelemetryToggle() = useTelemetryToggle()
    fun assertTelemetrySummary() = telemetrySummary()
    fun assertMakeDefaultBrowserButton() = makeDefaultBrowserButton()
    fun assertDeveloperToolsHeading() = developerToolsHeading()
    fun assertRemoteDebuggingToggle() = remoteDebuggingToggle()
    fun assertMozillaHeading() = mozillaHeading()
    fun assertAboutReferenceBrowserButton() = aboutReferenceBrowserButton()

    class Transition {
        private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        fun exitToHomeScreen() {
            device.pressBack()
        }
    }
}

fun settingsView(interact: SettingsViewRobot.() -> Unit) {
    SettingsViewRobot().interact()
}

private fun verifySettingsView() {
    // verify that we are in the correct settings view
    Espresso.onView(ViewMatchers.withText("Settings"))
    Espresso.onView(ViewMatchers.withText("About Reference Browser"))
}

private fun navigateUpButton() = Espresso.onView(ViewMatchers.withContentDescription("Navigate up"))
private fun syncSigninButton() = Espresso.onView(ViewMatchers.withText("Sign in"))
private fun syncHistorySummary() = Espresso.onView(ViewMatchers.withText("Sync your history"))
private fun useTelemetryToggle() = Espresso.onView(ViewMatchers.withText("Use Telemetry"))
private fun telemetrySummary() = Espresso.onView(ViewMatchers.withText("Send usage data"))
private fun makeDefaultBrowserButton() = Espresso.onView(ViewMatchers.withText("Make default browser"))
private fun developerToolsHeading() = Espresso.onView(ViewMatchers.withText("Developer tools"))
private fun remoteDebuggingToggle() = Espresso.onView(ViewMatchers.withText("Remote debugging via USB"))
private fun mozillaHeading() = Espresso.onView(ViewMatchers.withText("Mozilla"))
private fun aboutReferenceBrowserButton() = Espresso.onView(ViewMatchers.withText("About Reference Browser"))
