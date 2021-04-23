package org.mozilla.reference.browser.ui

import androidx.core.net.toUri
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mozilla.reference.browser.helpers.AndroidAssetDispatcher
import org.mozilla.reference.browser.helpers.BrowserActivityTestRule
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
    fun verifyReaderViewPageDetectionTest() {
        val readerViewPage = "https://www.york.ac.uk/teaching/cws/wws/webpage1.html"

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(readerViewPage.toUri()) {
        }.openNavigationToolbar {
            verifyReaderViewButton()
        }
    }
}
