package org.mozilla.reference.browser.ui

import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mozilla.reference.browser.helpers.AndroidAssetDispatcher
import org.mozilla.reference.browser.helpers.BrowserActivityTestRule
import org.mozilla.reference.browser.helpers.TestAssetHelper
import org.mozilla.reference.browser.ui.robots.navigationToolbar

class ReaderViewTest {

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
    fun verifyReaderViewDetectedTest() {
        val readerViewPage = TestAssetHelper.getLoremIpsumAsset(mockWebServer)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(readerViewPage.url) {
        }.openNavigationToolbar {
            verifyReaderViewButton()
        }.clickReaderViewButton {
            verifyAppearanceButton()
            openReaderViewAppearance()
            verifyAppearanceControlsMenu()
            verifyFontGroupButtons()
            verifyIncreaseFontSizeButton()
            verifyDecreaseFontSizeButton()
            verifyColorSchemeGroupButtons()
        }.dismissAppearanceControlsMenu {
            verifyAppearanceMenuDoesntExist()
        }

        navigationToolbar {
        }.clickReaderViewButton {
            verifyAppearanceButtonDoesntExist()
        }
    }

    @Test
    fun readerViewFontChangeTest() {
        val readerViewPage = TestAssetHelper.getLoremIpsumAsset(mockWebServer)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(readerViewPage.url) {
        }.openNavigationToolbar {
            verifyReaderViewButton()
        }.clickReaderViewButton {
            verifyAppearanceButton()
            openReaderViewAppearance()
            verifyAppearanceControlsMenu()
            verifyFontGroupButtons()
            clickSansSerifButton()
            verifyAppearanceFont("SANSSERIF")
            clickSerifButton()
            verifyAppearanceFont("SERIF")
        }
    }

    @Test
    fun readerViewFontSizeChangeTest() {
        val readerViewPage = TestAssetHelper.getLoremIpsumAsset(mockWebServer)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(readerViewPage.url) {
        }.openNavigationToolbar {
            verifyReaderViewButton()
        }.clickReaderViewButton {
            verifyAppearanceButton()
            openReaderViewAppearance()
            verifyAppearanceControlsMenu()
            verifyIncreaseFontSizeButton()
            verifyDecreaseFontSizeButton()
            verifyAppearanceFontSize(3)
            clickIncreaseFontSizeButton()
            verifyAppearanceFontSize(4)
            clickIncreaseFontSizeButton()
            verifyAppearanceFontSize(5)
            clickDecreaseFontSizeButton()
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
        }.openNavigationToolbar {
            verifyReaderViewButton()
        }.clickReaderViewButton {
            verifyAppearanceButton()
            openReaderViewAppearance()
            verifyAppearanceControlsMenu()
            verifyColorSchemeGroupButtons()
            clickDarkColorSchemeButton()
            verifyAppearanceColorScheme("DARK")
            clickSepiaColorSchemeButton()
            verifyAppearanceColorScheme("SEPIA")
            clickLightColorSchemeButton()
            verifyAppearanceColorScheme("LIGHT")
        }
    }
}
