/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui

import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mozilla.reference.browser.helpers.AndroidAssetDispatcher
import org.mozilla.reference.browser.helpers.BrowserActivityTestRule
import org.mozilla.reference.browser.helpers.RetryTestRule
import org.mozilla.reference.browser.helpers.TestAssetHelper
import org.mozilla.reference.browser.ui.robots.browser
import org.mozilla.reference.browser.ui.robots.navigationToolbar

class ContextMenusTest {

    private lateinit var mockWebServer: MockWebServer

    @get:Rule
    val activityTestRule = BrowserActivityTestRule()

    @Rule
    @JvmField
    val retryTestRule = RetryTestRule(3)

    @Before
    fun setUp() {
        mockWebServer = MockWebServer().apply {
            dispatcher = AndroidAssetDispatcher()
            start()
        }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun verifyLinkContextMenuItems() {
        val pageLinks = TestAssetHelper.getGenericAsset(mockWebServer, 4)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(pageLinks.url) {
            longClickMatchingText("Link 1")
            verifyLinkContextMenuItems()
        }
    }

    @Test
    fun openLinkInNewTabTest() {
        val pageLinks = TestAssetHelper.getGenericAsset(mockWebServer, 4)
        val genericURL = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(pageLinks.url) {
            longClickMatchingText("Link 1")
            clickContextOpenLinkInNewTab()
            clickSnackbarSwitchButton()
        }
        navigationToolbar {
        }.openTabTrayMenu {
            verifyRegularBrowsingTab()
            verifyExistingOpenTabs(pageLinks.title)
            verifyExistingOpenTabs(genericURL.title)
        }
    }

    @Test
    fun openLinkInPrivateTabTest() {
        val pageLinks = TestAssetHelper.getGenericAsset(mockWebServer, 4)
        val genericURL = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(pageLinks.url) {
            longClickMatchingText("Link 1")
            clickContextOpenLinkInPrivateTab()
            clickSnackbarSwitchButton()
        }
        navigationToolbar {
        }.openTabTrayMenu {
            openPrivateBrowsing()
            verifyPrivateBrowsingTab()
            verifyExistingOpenTabs(genericURL.title)
        }
    }

    @Test
    fun contextCopyLinkTest() {
        val pageLinks = TestAssetHelper.getGenericAsset(mockWebServer, 4)
        val genericURL = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(pageLinks.url) {
            longClickMatchingText("Link 1")
            clickContextCopyLink()
            waitUntilCopyLinkSnackbarIsGone()
        }
        navigationToolbar {
        }.clickToolbar {
            pasteAndLoadCopiedLink()
        }

        browser {
            verifyUrl(genericURL.url.toString())
        }
    }

    @Test
    fun contextShareLinkTest() {
        val pageLinks = TestAssetHelper.getGenericAsset(mockWebServer, 4)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(pageLinks.url) {
            longClickMatchingText("Link 1")
        }.clickContextShareLink {
            verifyShareContentPanel()
        }
    }

    @Test
    fun copyTextTest() {
        val genericURL = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(genericURL.url) {
            longClickAndCopyText("content")
        }
        navigationToolbar {
        }.clickToolbar {
            clickClearToolbarButton()
            longClickToolbar()
            clickPasteText()
            verifyPastedToolbarText("content")
        }
    }

    @Test
    fun selectAllAndCopyTextTest() {
        val genericURL = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(genericURL.url) {
            longClickAndCopyText("content", true)
        }
        navigationToolbar {
        }.clickToolbar {
            clickClearToolbarButton()
            longClickToolbar()
            clickPasteText()
            verifyPastedToolbarText("Page content: 1")
        }
    }
}
