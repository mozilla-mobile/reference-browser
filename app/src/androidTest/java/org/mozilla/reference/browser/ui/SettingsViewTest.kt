/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui

import org.junit.Rule
import org.junit.Test
import org.mozilla.reference.browser.helpers.BrowserActivityTestRule
import org.mozilla.reference.browser.ui.robots.navigationToolbar

/**
 *  Tests for verifying the settings view options:
 * - Appears when the settings submenu is tapped
 * - Expected options are displayed as listed below
 */

class SettingsViewTest {

    @get:Rule val browserActivityTestRule = BrowserActivityTestRule()

    /* ktlint-disable no-blank-line-before-rbrace */ // This imposes unreadable grouping.
    @Test
    // This test verifies settings view items are all in place
    fun basicSettingsTest() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            assertSettingsView()
            assertNavigateUp()
            assertSyncSigninButton()
            assertSyncHistorySummary()
            assertUseTelemetryToggle()
            assertTelemetrySummary()
            assertMakeDefaultBrowserButton()
            assertDeveloperToolsHeading()
            assertRemoteDebuggingToggle()
            assertMozillaHeading()
            assertAboutReferenceBrowserButton()
        }
    }
}
