/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package org.mozilla.reference.browser.ui

import org.junit.Rule
import org.junit.Test
import org.mozilla.reference.browser.helpers.BrowserActivityTestRule
import org.mozilla.reference.browser.ui.robots.navigationToolbar

/**
 *  Tests for verifying tab tray menu:
 * - Appears when counter tabs is clicked
 * - Expected options are displayed as listed below
 * - TBD add missing options to verify
 */

class TabTrayMenuTest {

    @get:Rule val activityTestRule = BrowserActivityTestRule()

    /* ktlint-disable no-blank-line-before-rbrace */ // This imposes unreadable grouping.
    @Test
    // This test verifies the tab tray menu items are all in place
    fun tabTrayUITest() {
        navigationToolbar {
        }.openTabTrayMenu {
            verifyRegularBrowsingButton()
            verifyPrivateBrowsingButton()
            verifyGoBackButton()
            verifyNewTabButton()
            verifyMenuButton()
            verifyDefaultOpenTabTitle()
            verifyCloseButtonInTabPreview()
            verifyDefaultOpenTabThumbnail()
        }
    }

    @Test
    // This test verifies that close all tabs option works as expected
    fun closeAllTabsTest() {
        navigationToolbar {
        }.openTabTrayMenu {
        }.openMoreOptionsMenu {
            verifyCloseAllTabsButton()
        }.closeAllTabs {
            verifyNewTabView()
            checkNumberOfTabsTabCounter("1")
        }
    }
}
