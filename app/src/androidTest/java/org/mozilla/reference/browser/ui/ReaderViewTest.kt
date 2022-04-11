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
import org.mozilla.reference.browser.ui.robots.navigationToolbar

class ReaderViewTest {

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
    fun verifyReaderViewDetectionTest() {
        val readerViewPage = TestAssetHelper.getLoremIpsumAsset(mockWebServer)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(readerViewPage.url) {
        }
        navigationToolbar {
            verifyReaderViewButton()
        }.clickReaderViewButton {
            verifyAppearanceButtonExists()
            clickAppearanceButton()
            verifyAppearanceMenuExists()
        }.dismissAppearanceMenu {
        }
        navigationToolbar {
        }.clickReaderViewButton {
            verifyAppearanceButtonDoesntExists()
        }
    }

    @Test
    fun readerViewFontChangeTest() {
        val readerViewPage = TestAssetHelper.getLoremIpsumAsset(mockWebServer)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(readerViewPage.url) {
        }
        navigationToolbar {
            verifyReaderViewButton()
        }.clickReaderViewButton {
            verifyAppearanceButtonExists()
            clickAppearanceButton()
            verifyAppearanceMenuExists()
            verifyFontGroupButtons()
            clickSansSerifButton()
            verifyActiveAppearanceFont("SANSSERIF")
            clickSerifButton()
            verifyActiveAppearanceFont("SERIF")
        }
    }

    @Test
    fun readerViewFontSizeChangeTest() {
        val readerViewPage = TestAssetHelper.getLoremIpsumAsset(mockWebServer)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(readerViewPage.url) {
        }
        navigationToolbar {
            verifyReaderViewButton()
        }.clickReaderViewButton {
            verifyAppearanceButtonExists()
            clickAppearanceButton()
            verifyAppearanceMenuExists()
            verifyIncreaseFontSizeButton()
            verifyDecreaseFontSizeButton()
            verifyAppearanceFontSize(3)
            clickIncreaseFontSizeButton()
            verifyAppearanceFontSize(4)
            clickDecreaseFontSizeButton()
            verifyAppearanceFontSize(3)
        }
    }

    @Test
    fun readerViewColorSchemeChangeTest() {
        val readerViewPage = TestAssetHelper.getLoremIpsumAsset(mockWebServer)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(readerViewPage.url) {
        }
        navigationToolbar {
            verifyReaderViewButton()
        }.clickReaderViewButton {
            verifyAppearanceButtonExists()
            clickAppearanceButton()
            verifyAppearanceMenuExists()
            verifyColorSchemeGroupButtons()
            clickSepiaColorButton()
            verifyAppearanceColorScheme("SEPIA")
            clickDarkColorButton()
            verifyAppearanceColorScheme("DARK")
            clickLightColorButton()
            verifyAppearanceColorScheme("LIGHT")
        }
    }
}
