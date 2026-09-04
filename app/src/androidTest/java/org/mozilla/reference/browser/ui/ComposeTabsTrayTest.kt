/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui

import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.helpers.AndroidAssetDispatcher
import org.mozilla.reference.browser.helpers.BooleanPreferenceRule
import org.mozilla.reference.browser.helpers.BrowserActivityTestRule
import org.mozilla.reference.browser.helpers.RetryTestRule
import org.mozilla.reference.browser.ui.robots.navigationToolbar

/**
 * Tests for the experimental Jetpack Compose tabs tray.
 *
 * The tray is behind the "Use experimental Jetpack Compose tabs tray" setting, which [BooleanPreferenceRule] enables
 * before the activity is created.
 */
class ComposeTabsTrayTest {
    private lateinit var mockWebServer: MockWebServer

    @get:Rule
    val rules: RuleChain =
        RuleChain.outerRule(BooleanPreferenceRule(R.string.pref_key_compose_tabs_tray, true))
            .around(BrowserActivityTestRule())

    @Rule @JvmField val retryTestRule = RetryTestRule(3)

    @Before
    fun setUp() {
        mockWebServer =
            MockWebServer().apply {
                dispatcher = AndroidAssetDispatcher()
                start()
            }
    }

    @After
    fun tearDown() {
        runCatching { mockWebServer.close() }
    }

    // Verifies the tray opens from the tab counter and the back button returns to the browser.
    @Test
    fun openAndCloseComposeTabsTrayTest() {
        navigationToolbar {}
            .openComposeTabsTray {
                verifyTabsTray()
            }
            .goBackToBrowser {
                verifyNoTabAddressView()
            }
    }
}
