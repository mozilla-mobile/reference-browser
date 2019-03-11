/* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers

/**
 * Implementation of Robot Pattern for the settings privacy menu.
 */
class SettingsViewPrivacyRobot {

    fun verifyPrivacyUpButton() = privacyUpButton()
    fun verifyPrivacySettings() = assertPrivacySettingsView()
    fun verifyTrackingProtectionHeading() = trackingProtectionHeading()
    fun verifyTPEnableInNormalBrowsing() = tpEnableInNormalBrowsing()
    fun verifyTPEnableinPrivateBrowsing() = tpEnableInPrivateBrowsing()
    fun verifyDataChoicesHeading() = dataChoicesHeading()
    // verifyUseTelemetryToggle does not yet check that the client telemetry is disabled/enabled
    fun verifyUseTelemetryToggle() = useTelemetryToggle()
    fun verifyTelemetrySummary() = telemetrySummary()

    class Transition {
        fun settingsViewPrivacy(interact: SettingsViewPrivacyRobot.() -> Unit): SettingsViewPrivacyRobot.Transition {
            return SettingsViewPrivacyRobot.Transition()
        }
    }
}

private fun privacyUpButton() = Espresso.onView(ViewMatchers.withContentDescription("Navigate up"))
private fun assertPrivacySettingsView() = Espresso.onView(ViewMatchers.withText("Privacy Settings"))
private fun trackingProtectionHeading() = Espresso.onView(ViewMatchers.withText("Tracking Protection"))
private fun tpEnableInNormalBrowsing() = Espresso.onView(ViewMatchers.withText("Enable in Normal Browsing Mode"))
private fun tpEnableInPrivateBrowsing() = Espresso.onView(ViewMatchers.withText("Enable in Private Browsing Mode"))
private fun dataChoicesHeading() = Espresso.onView(ViewMatchers.withText("Data Choices"))
private fun useTelemetryToggle() = Espresso.onView(ViewMatchers.withText("Use Telemetry"))
private fun telemetrySummary() = Espresso.onView(ViewMatchers.withText("Send usage data"))
