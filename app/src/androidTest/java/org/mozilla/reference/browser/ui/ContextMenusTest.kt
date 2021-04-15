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
import org.mozilla.reference.browser.helpers.TestAssetHelper
import org.mozilla.reference.browser.ui.robots.downloadRobot
import org.mozilla.reference.browser.ui.robots.navigationToolbar
import org.mozilla.reference.browser.ui.robots.notificationShade

class ContextMenusTest {

    private lateinit var mockWebServer: MockWebServer

    @get:Rule
    val activityTestRule = BrowserActivityTestRule()

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
    fun verifyLinkContextMenuItemsTest() {
        val pageLinks = TestAssetHelper.getGenericAsset(mockWebServer, 4)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(pageLinks.url) {
            longClickMatchingText("Link 1")
            verifyLinkContextMenuItems()
        }
    }

    @Test
    fun verifyLinkedImageContextMenuItemsTest() {
        val pageLinks = TestAssetHelper.getGenericAsset(mockWebServer, 4)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(pageLinks.url) {
            longClickMatchingImage("test_link_image")
            verifyLinkedImageContextMenuItems()
        }
    }

    @Test
    fun verifyNonLinkedImageContextMenuItemsTest() {
        val pageLinks = TestAssetHelper.getGenericAsset(mockWebServer, 4)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(pageLinks.url) {
            longClickMatchingImage("test_no_link_image")
            verifyNonLinkedImageContextMenuItems()
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
        }.openNavigationToolbar {
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
        }.openNavigationToolbar {
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
        }.openNavigationToolbar {
        }.clickToolbar {
            verifyLinkFromClipboard(genericURL.url.toString())
        }.clickLinkFromClipboard(genericURL.url.toString()) {
            verifyUrl(genericURL.toString())
        }
    }

    @Test
    fun contextShareLinkTest() {
        val pageLinks = TestAssetHelper.getGenericAsset(mockWebServer, 4)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(pageLinks.url) {
            longClickMatchingText("Link 1")
        }.clickContextShareLink {
            verifyContentPanel()
        }
    }

    @Test
    fun contextShareImageTest() {
        val pageLinks = TestAssetHelper.getGenericAsset(mockWebServer, 4)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(pageLinks.url) {
            longClickMatchingImage("test_link_image")
        }.clickContextShareImage {
            verifyContentPanel()
        }
    }

    @Test
    fun contextCopyImageLocationTest() {
        val pageLinks = TestAssetHelper.getGenericAsset(mockWebServer, 4)
        val imageUrl = TestAssetHelper.getImageAsset(mockWebServer)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(pageLinks.url) {
            longClickMatchingImage("test_link_image")
            clickContextCopyImageLocation()
        }.openNavigationToolbar {
        }.clickToolbar {
            verifyLinkFromClipboard(imageUrl.url.toString())
        }.clickLinkFromClipboard(imageUrl.url.toString()) {
            verifyUrl(imageUrl.toString())
        }
    }

    @Test
    fun verifyContextSaveImageTest() {
        val pageLinks = TestAssetHelper.getGenericAsset(mockWebServer, 4)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(pageLinks.url) {
            longClickMatchingImage("test_no_link_image")
            clickContextSaveImage()
        }

        downloadRobot {
            verifyAllowFileAccessPopup()
            clickAllowButton()
        }

        notificationShade {
            verifySystemNotificationExists("Download completed")
        }.closeNotification {
        }
    }

    @Test
    fun verifyContextDownloadLinkTest() {
        val pageLinks = TestAssetHelper.getGenericAsset(mockWebServer, 4)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(pageLinks.url) {
            longClickMatchingImage("test_link_image")
            clickContextDownloadLink()
        }

        downloadRobot {
            verifyAllowFileAccessPopup()
            clickAllowButton()
        }

        notificationShade {
            verifySystemNotificationExists("Download completed")
        }.closeNotification {
        }
    }
}
