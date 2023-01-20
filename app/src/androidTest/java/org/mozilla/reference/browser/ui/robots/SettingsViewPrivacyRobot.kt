/* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.mozilla.reference.browser.helpers.TestAssetHelper

/**
 * Implementation of Robot Pattern for the settings privacy menu.
 */
class SettingsViewPrivacyRobot {

    fun verifyPrivacyUpButton() = assertPrivacyUpButton()
    fun verifyPrivacySettings() = assertPrivacySettingsView()
    fun verifyTrackingProtectionHeading() = assertTrackingProtectionHeading()
    fun verifyTPEnableInNormalBrowsing() = assertTpEnableInNormalBrowsing()
    fun verifyTPEnableinPrivateBrowsing() = assertTpEnableInPrivateBrowsing()
    fun verifyDataChoicesHeading() = assertDataChoicesHeading()

    // verifyUseTelemetryToggle does not yet check that the client telemetry is disabled/enabled
    fun verifyUseTelemetryToggle() = assertUseTelemetryToggle()
    fun verifyTelemetrySummary() = assertTelemetrySummary()

    class Transition {
        fun settingsViewPrivacy(): SettingsViewPrivacyRobot.Transition {
            return SettingsViewPrivacyRobot.Transition()
        }
    }
}

private fun privacyUpButton() = Espresso.onView(ViewMatchers.withContentDescription("Navigate up"))
private fun privacySettingsView() = Espresso.onView(ViewMatchers.withText("Privacy Settings"))
private fun trackingProtectionHeading() = Espresso.onView(ViewMatchers.withText("Tracking Protection"))
private fun tpEnableInNormalBrowsing() = Espresso.onView(ViewMatchers.withText("Enable in Normal Browsing Mode"))
private fun tpEnableInPrivateBrowsing() = Espresso.onView(ViewMatchers.withText("Enable in Private Browsing Mode"))
private fun dataChoicesHeading() = Espresso.onView(ViewMatchers.withText("Data Choices"))
private fun useTelemetryToggle() = Espresso.onView(ViewMatchers.withText("Use Telemetry"))
private fun telemetrySummary() = Espresso.onView(ViewMatchers.withText("Send usage data"))
private fun assertPrivacyUpButton() {
    mDevice.wait(Until.findObject(By.text("Navigate up")), TestAssetHelper.waitingTimeShort)
}
private fun assertPrivacySettingsView() = privacySettingsView()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertTrackingProtectionHeading() = trackingProtectionHeading()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertTpEnableInNormalBrowsing() = tpEnableInNormalBrowsing()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertTpEnableInPrivateBrowsing() = tpEnableInPrivateBrowsing()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertDataChoicesHeading() = dataChoicesHeading()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertUseTelemetryToggle() = useTelemetryToggle()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertTelemetrySummary() = telemetrySummary()
    .check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
