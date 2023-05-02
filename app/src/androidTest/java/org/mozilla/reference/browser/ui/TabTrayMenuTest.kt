/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.helpers.AndroidAssetDispatcher
import org.mozilla.reference.browser.helpers.BrowserActivityTestRule
import org.mozilla.reference.browser.helpers.RetryTestRule
import org.mozilla.reference.browser.helpers.TestAssetHelper
import org.mozilla.reference.browser.helpers.click
import org.mozilla.reference.browser.ui.robots.mDevice
import org.mozilla.reference.browser.ui.robots.navigationToolbar

/**
 *  Tests for verifying tab tray menu:
 * - Appears when counter tabs is clicked
 * - Expected options are displayed as listed below
 * - Options/Buttons in this menu work as expected
 */

class TabTrayMenuTest {

    private lateinit var mockWebServer: MockWebServer

    @get:Rule val activityTestRule = BrowserActivityTestRule()

    @Rule
    @JvmField
    val retryTestRule = RetryTestRule(3)

    // SetUp to close all tabs before starting each test
    @Before
    fun setUp() {
        val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        mockWebServer = MockWebServer().apply {
            dispatcher = AndroidAssetDispatcher()
            start()
        }

        fun optionsButton() = onView(ViewMatchers.withContentDescription("More options"))
        fun closeAllTabsButton() = onView(ViewMatchers.withText("Close All Tabs"))
        fun goBackButton() = onView(ViewMatchers.withContentDescription("back"))
        val tabCounterButton = onView(withId(R.id.counter_text))

        mDevice.waitForIdle()
        tabCounterButton.click()

        val thereAreTabsOpenInTabTray = mDevice.findObject(UiSelector().text("about:blank")).exists()

        if (thereAreTabsOpenInTabTray) {
            optionsButton().click()
            closeAllTabsButton().click()
        } else {
            goBackButton().click()
        }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    /* ktlint-disable no-blank-line-before-rbrace */ // This imposes unreadable grouping.
    // This test verifies the tab tray menu items are all in place
    @Test
    fun tabTrayUITest() {
        navigationToolbar {
        }.openTabTrayMenu {
            verifyRegularBrowsingTab()
            verifyPrivateBrowsingTab()
            verifyGoBackButton()
            verifyNewTabButton()
        }.openMoreOptionsMenu(activityTestRule.activity) {
            verifyCloseAllTabsButton()
        }
    }

    // This test verifies that close all tabs option works as expected
    @Test
    fun closeAllTabsTest() {
        navigationToolbar {
        }.openTabTrayMenu {
        }.openNewTab {
        }.openTabTrayMenu {
            verifyThereIsOneTabOpen()
        }.openMoreOptionsMenu(activityTestRule.activity) {
            mDevice.waitForIdle()
            verifyCloseAllTabsButton()
        }.closeAllTabs {
            verifyNoTabAddressView()
            checkNumberOfTabsTabCounter("0")
        }
    }

    // This test verifies that close all tabs option works as expected
    @Test
    fun closeAllPrivateTabsTest() {
        navigationToolbar {
        }.openTabTrayMenu {
            openPrivateBrowsing()
        }.openNewTab {
        }.openTabTrayMenu {
            openPrivateBrowsing()
            verifyThereIsOnePrivateTabOpen()
        }.openMoreOptionsMenu(activityTestRule.activity) {
            mDevice.waitForIdle()
            verifyCloseAllPrivateTabsButton()
        }.closeAllPrivateTabs {
        }.openTabTrayMenu {
            openPrivateBrowsing()
            verifyThereAreNotPrivateTabsOpen()
            goBackFromTabTrayTest()
        }
    }

    @Test
    fun closeOneTabXButtonTest() {
        navigationToolbar {
        }.openTabTrayMenu {
        }.openNewTab {
            checkNumberOfTabsTabCounter("1")
        }.openTabTrayMenu {
        }.closeTabXButton {
            checkNumberOfTabsTabCounter("0")
        }
    }

    // This test verifies the change between regular-private browsing works
    @Test
    fun privateRegularModeChangeTest() {
        navigationToolbar {
        }.openTabTrayMenu {
            openPrivateBrowsing()
            verifyPrivateBrowsingTab(true)
            verifyRegularBrowsingTab(false)
            openRegularBrowsing()
            verifyPrivateBrowsingTab(false)
            verifyRegularBrowsingTab(true)
            goBackFromTabTrayTest()
        }
    }

    // This test verifies the new tab is open and that its items are all in place
    @Test
    fun openNewTabTest() {
        val genericURL = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        navigationToolbar {
        }.openTabTrayMenu {
        }.openNewTab {
            verifyNewTabAddressView("about:blank")
            checkNumberOfTabsTabCounter("1")
        }.openTabTrayMenu {
        }.openNewTab {
        }.enterUrlAndEnterToBrowser(genericURL.url) {
            verifyUrl(genericURL.url.toString())
        }
        navigationToolbar {
            checkNumberOfTabsTabCounter("2")
        }.openTabTrayMenu {
            verifyExistingOpenTabs("about:blank")
            verifyExistingOpenTabs(genericURL.title)
        }.clickOpenTab("about:blank") {
            verifyUrl("about:blank")
        }
    }

    // This test verifies the new tab is open and that its items are all in place
    @Ignore("Failing, see: https://github.com/mozilla-mobile/reference-browser/issues/1923")
    @Test
    fun openNewPrivateTabTest() {
        val firstGenericURL = TestAssetHelper.getGenericAsset(mockWebServer, 1)
        val secondGenericURL = TestAssetHelper.getGenericAsset(mockWebServer, 2)

        navigationToolbar {
        }.openTabTrayMenu {
            openPrivateBrowsing()
        }.openNewTab {
            verifyNewTabAddressView("data:text/html")
            checkNumberOfTabsTabCounter("1")
        }.openTabTrayMenu {
            openPrivateBrowsing()
        }.openNewTab {
        }.enterUrlAndEnterToBrowser(firstGenericURL.url) {
            verifyPageContent("Page content: 1")
        }
        navigationToolbar {
            checkNumberOfTabsTabCounter("2")
        }.openTabTrayMenu {
            openPrivateBrowsing()
            verifyExistingOpenTabs("Private Browsing")
            verifyExistingOpenTabs(firstGenericURL.title)
        }.openNewTab {
        }.enterUrlAndEnterToBrowser(secondGenericURL.url) {
            verifyPageContent("Page content: 2")
        }
        navigationToolbar {
            checkNumberOfTabsTabCounter("3")
        }
    }

    // This test verifies the back button functionality
    @Test
    fun goBackFromTabTrayTest() {
        navigationToolbar {
        }.openTabTrayMenu {
        }.goBackFromTabTray {
            // For now checking new tab is valid, this will change when browsing to/from different places
            verifyNoTabAddressView()
        }
    }
}
