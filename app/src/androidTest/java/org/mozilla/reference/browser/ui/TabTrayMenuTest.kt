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
 * - Options/Buttons in this menu work as expected
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
        }.openNewTab {
        }.openTabTrayMenu {
            // This check will be enabled once there is a set up/tear down defined
            // verifyThereIsOneTabsOpen()
        }.openMoreOptionsMenu {
            verifyCloseAllTabsButton()
        }.closeAllTabs {
            verifyNewTabAddressView()
            checkNumberOfTabsTabCounter("0")
        }
    }

    @Test
    // This test verifies that close all tabs option works as expected
    fun closeAllPrivateTabsTest() {
        navigationToolbar {
        }.openTabTrayMenu {
            openPrivateBrowsing()
        }.openNewTab {
        }.openTabTrayMenu {
            openPrivateBrowsing()
            verifyThereIsOnePrivateTabOpen()
        }.openMoreOptionsMenu {
            verifyCloseAllPrivateTabsButton()
        }.closeAllPrivateTabs {
        }.openTabTrayMenu {
            openPrivateBrowsing()
            verifyThereAreNotPrivateTabsOpen()
        }
    }

    @Test
    // This test verifies the change between regular-private browsing works
    fun privateRegularModeChangeTest() {
        navigationToolbar {
        }.openTabTrayMenu {
            openPrivateBrowsing()
            verifyPrivateBrowsingButton(true)
            verifyRegularBrowsingButton(false)
            openRegularBrowsing()
            verifyPrivateBrowsingButton(false)
            verifyRegularBrowsingButton(true)
        }
    }

    @Test
    // This test verifies the new tab is open and that its items are all in place
    fun openNewTabTest() {
        navigationToolbar {
        }.openTabTrayMenu {
        }.openNewTab {
            verifyNewTabAddressView()
            checkNumberOfTabsTabCounter("1")
        }
    }

    @Test
    // This test verifies the back button functionality
    fun goBackFromTabTrayTest() {
        navigationToolbar {
        }.openTabTrayMenu {
        }.goBack {
            // For now checking new tab is valid, this will change when browsing to/from different places
            verifyNewTabAddressView()
        }
    }
}
