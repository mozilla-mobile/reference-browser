/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui

import org.junit.Rule
import org.junit.Test
import org.mozilla.reference.browser.helpers.BrowserActivityTestRule
import org.mozilla.reference.browser.ui.robots.navigationToolbar

/**
 *  Tests for verifying open new tab:
 * - Appears the new tab
 * - The tab counter is updated
 */

class NewTabTest {

    @get:Rule val activityTestRule = BrowserActivityTestRule()

    /* ktlint-disable no-blank-line-before-rbrace */ // This imposes unreadable grouping.
    @Test
    // This test verifies the new tab is open and that its items are all in place
    fun newTabOpenTest() {
        navigationToolbar {
        }.openTabTrayMenu {
        }.openNewTab {
            verifyNewTabView()
            verifyNumberOfTabs()
        }
    }
}
