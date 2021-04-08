/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mozilla.reference.browser.helpers.AndroidAssetDispatcher
import org.mozilla.reference.browser.helpers.BrowserActivityTestRule
import org.mozilla.reference.browser.helpers.TestAssetHelper
import org.mozilla.reference.browser.ui.robots.navigationToolbar

/**
 *  Tests for verifying the main three dot menu options
 *
 *  Including:
 * - Verify all menu items present
 * - Forward button navigates forward to a page
 * - Refresh button refreshes page content
 * - Share button opens app overlay menu
 * - Request desktop site toggle forwards to desktop view of web page (TBD)
 * - Find in page button can locate web page text
 * - Report issue button forwards to gitubh issues (TBD)
 * - Open settings button opens Settings sub-menu
 *
 * Not included:
 * - TODO: Request desktop site (user mockWebServer to parse request headers)
 * - Stop button stops page loading (covered by smoke tests)
 */

class ThreeDotMenuTest {

    private val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private lateinit var mockWebServer: MockWebServer

    @get:Rule
    val activityTestRule = BrowserActivityTestRule()

    @Before
    fun setUp() {
        mockWebServer = MockWebServer().apply {
            setDispatcher(AndroidAssetDispatcher())
            start()
        }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    /* ktlint-disable no-blank-line-before-rbrace */ // This imposes unreadable grouping.
    @Test
    fun homeScreenMenuTest() {
        navigationToolbar {
        }.openThreeDotMenu {
            verifyThreeDotMenuExists()
            // These items should not exist in the home screen menu
            verifyForwardButtonDoesntExist()
            verifyReloadButtonDoesntExist()
            verifyStopButtonDoesntExist()
            verifyShareButtonDoesntExist()
            verifyRequestDesktopSiteToggleDoesntExist()
            verifyAddToHomescreenButtonDoesntExist()
            verifyFindInPageButtonDoesntExist()
            // Only these items should exist in the home screen menu
            verifyAddOnsButtonExists()
            verifySyncedTabsButtonExists()
            verifyReportIssueExists()
            verifyOpenSettingsExists()
        }
    }

    @Test
    fun threeDotMenuItemsTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)
        navigationToolbar {
        // pull up URL to ensure this is not a first-user 3 dot menu
        }.enterUrlAndEnterToBrowser(defaultWebPage.url) {
            mDevice.waitForIdle()
        }.openNavigationToolbar {
        }.openThreeDotMenu {
            verifyThreeDotMenuExists()
            verifyForwardButtonExists()
            verifyReloadButtonExists()
            verifyStopButtonExists()
            verifyShareButtonExists()
            verifyRequestDesktopSiteToggleExists()
            verifyAddToHomescreenButtonExists()
            verifyFindInPageButtonExists()
            verifyAddOnsButtonExists()
            verifySyncedTabsButtonExists()
            verifyReportIssueExists()
            verifyOpenSettingsExists()
        }
    }

    @Test
    fun goForwardTest() {

        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)
        val nextWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 2)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(defaultWebPage.url) {
            verifyUrl(defaultWebPage.toString())
        }.openNavigationToolbar {
        }.enterUrlAndEnterToBrowser(nextWebPage.url) {
            verifyUrl(nextWebPage.toString())
            mDevice.pressBack()
            verifyUrl(defaultWebPage.toString())
        }

        navigationToolbar {
        }.openThreeDotMenu {
        }.goForward {
            verifyUrl(nextWebPage.toString())
        }
    }

    @Test
    @Ignore("https://github.com/mozilla-mobile/reference-browser/issues/1314")
    // need to add clear cache setup to ensure correct starting page
    // also, investigate why this periodically causes mockWebServer to crash
    fun refreshPageTest() {

        val refreshWebPage = TestAssetHelper.getRefreshAsset(mockWebServer)

        navigationToolbar {

        // load the default page, to be refreshed
        // (test assumes no cookies cached at test start)

        }.enterUrlAndEnterToBrowser(refreshWebPage.url) {
            verifyPageContent("DEFAULT")
        }.openNavigationToolbar {
        }.openThreeDotMenu {

        // refresh page and verify
        }.refreshPage {
            verifyPageContent("REFRESHED")
        }.openNavigationToolbar {
        }
    }

    @Test
    fun doShareTest() {
        val loremIpsumWebPage = TestAssetHelper.getLoremIpsumAsset(mockWebServer)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(loremIpsumWebPage.url) {
        }.openNavigationToolbar {
        }.openThreeDotMenu {
        }.openShare {
            verifyContentPanel()
        }
    }

    @Ignore("Temp disable broken test - see:  https://github.com/mozilla-mobile/fenix/issues/5534")
    @Test
    // finds specific text snippets in a lorem ipsum sample page
    fun findInPageTest() {
        val loremIpsumWebPage = TestAssetHelper.getLoremIpsumAsset(mockWebServer)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(loremIpsumWebPage.url) {
        }.openNavigationToolbar {
        }.openThreeDotMenu {
        }.findInPage {
            verifyFindInPageNextButton()
            verifyFindInPagePrevButton()
            verifyFindInPageCloseButton()
            mDevice.waitForIdle()
            enterFindInPageQuery("lab")
            mDevice.waitForIdle()
            verifyFindNextInPageResult("1/3")
            verifyFindNextInPageResult("2/3")
            verifyFindNextInPageResult("3/3")
            verifyFindPrevInPageResult("1/3")
            verifyFindPrevInPageResult("3/3")
            verifyFindPrevInPageResult("2/3")
            enterFindInPageQuery("in")
            verifyFindNextInPageResult("3/7")
            verifyFindNextInPageResult("4/7")
            verifyFindNextInPageResult("5/7")
            verifyFindNextInPageResult("6/7")
            verifyFindNextInPageResult("7/7")
        }
    }

    @Test
    // so less flaky, we only test redirect to github login
    // (redirect happens with / without WIFI enabled)
    fun reportIssueTest() {
        val loremIpsumWebPage = TestAssetHelper.getLoremIpsumAsset(mockWebServer)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(loremIpsumWebPage.url) {
            mDevice.waitForIdle()
        }.openNavigationToolbar {
        }.openThreeDotMenu {
        }.reportIssue {
            mDevice.waitForIdle()
            verifyGithubUrl()
        }
    }

    @Test
    fun openSettingsTest() {
        navigationToolbar {
        }.openThreeDotMenu {
        }.openSettings {
            verifySettingsViewExists()
        }
    }

    @Test
    // Verifies the Synced tabs menu opens from a tab's 3 dot menu and displays the correct view if the user isn't signed in
    fun openSyncedTabsTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(defaultWebPage.url) {
        }.openNavigationToolbar {
        }.openThreeDotMenu {
        }.openSyncedTabs {
            verifyNotSignedInSyncTabsView()
        }
    }

    @Test
    fun requestDesktopSiteTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(defaultWebPage.url) {
        }.openNavigationToolbar {
        }.openThreeDotMenu {
        }.requestDesktopSite {
        }.openThreeDotMenu {
            verifyRequestDesktopSiteIsTurnedOn()
        }.goBack {
        }.openThreeDotMenu {
        }.requestDesktopSite {
        }.openThreeDotMenu {
            verifyRequestDesktopSiteIsTurnedOff()
        }
    }

    @Test
    fun addToHomeScreenTest() {
        val defaultWebPage = TestAssetHelper.getGenericAsset(mockWebServer, 1)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(defaultWebPage.url) {
        }.openNavigationToolbar {
        }.openThreeDotMenu {
        }.openAddToHomeScreen {
            verifyAddToHomeScreenPopup()
            clickCancelAddToHomeScreenButton()
        }

        navigationToolbar {
        }.openThreeDotMenu {
        }.openAddToHomeScreen {
            verifyAddToHomeScreenPopup()
            clickAddAutomaticallyToHomeScreenButton()
        }.openHomeScreenShortcut(defaultWebPage.title) {
            verifyUrl(defaultWebPage.toString())
        }
    }
}
