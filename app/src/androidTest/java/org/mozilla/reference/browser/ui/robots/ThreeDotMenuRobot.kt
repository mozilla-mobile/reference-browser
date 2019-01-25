/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.mozilla.reference.browser.helpers.click
import org.mozilla.reference.browser.R

class ThreeDotMenuRobot {
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    fun assertThreeDotRecyclerView() = recyclerView()
    fun assertForward() = forwardButton()
    fun assertReload() = refreshButton()
    fun assertStop() = stopButton()
    fun doShareOverlay() {
        shareButton().click()
        assertShareOverlay()
        device.pressBack()
    }

    fun assertToggleRequestDesktopSite() = requestDesktopSiteToggle()
    fun assertReportIssue() = reportIssueButton()
    fun assertOpenSettings() = settingsButton()

    class Transition {
        fun openSettings(interact: SettingsViewRobot.() -> Unit): SettingsViewRobot.Transition {
            settingsButton().click()
            SettingsViewRobot().interact()
            return SettingsViewRobot.Transition()
        }
    }
}

private fun recyclerView() {
    onView(withId(R.id.mozac_browser_menu_recyclerView))
    reportIssueButton()
}
private fun forwardButton() = onView(ViewMatchers.withContentDescription("Forward"))
private fun refreshButton() = onView(ViewMatchers.withContentDescription("Refresh"))
private fun stopButton() = onView(ViewMatchers.withContentDescription("Stop"))
private fun shareButton() = onView(ViewMatchers.withText("Share"))
private fun assertShareOverlay() = onView(ViewMatchers.withText("Share with…"))
private fun requestDesktopSiteToggle() = onView(ViewMatchers.withText("Request Desktop Site"))
private fun reportIssueButton() = onView(ViewMatchers.withText("Report Issue"))
private fun settingsButton() = onView(ViewMatchers.withText("Settings"))
