/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots
import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.uiautomator.UiDevice
import org.mozilla.reference.browser.helpers.click


class MainMenuRobot {
    // open app layover
    fun sharePage() = shareButton().click()
    fun toggleRequestDesktopSite() = requestDesktopSiteToggle().click()
    // sends you to github
    fun reportIssue() = reportIssueButton().click()
    fun openSettings() = settingsButton().click()
}

private fun shareButton() = onView(ViewMatchers.withText("Share"))
private fun requestDesktopSiteToggle() = onView(ViewMatchers.withText("Request Desktop Site"))
private fun reportIssueButton() = onView(ViewMatchers.withText("Report Issue"))
private fun settingsButton() = onView(ViewMatchers.withText("Settings"))