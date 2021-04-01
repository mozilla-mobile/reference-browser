package org.mozilla.reference.browser.ui

import android.view.View
import androidx.test.espresso.IdlingRegistry
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.helpers.AndroidAssetDispatcher
import org.mozilla.reference.browser.helpers.BrowserActivityTestRule
import org.mozilla.reference.browser.helpers.TestAssetHelper
import org.mozilla.reference.browser.helpers.matchers.ViewVisibilityIdlingResource
import org.mozilla.reference.browser.ui.robots.navigationToolbar

class ReaderViewTest {

    private val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private lateinit var mockWebServer: MockWebServer
    private var readerViewNotification: ViewVisibilityIdlingResource? = null

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
        IdlingRegistry.getInstance().unregister(readerViewNotification)
    }

    @Test
    fun verifyReaderViewPageMenuDetection() {
        val readerViewPage = TestAssetHelper.getLoremIpsumAsset(mockWebServer)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(readerViewPage.url) {
            readerViewNotification = ViewVisibilityIdlingResource(
                activityTestRule.activity.findViewById(R.id.mozac_browser_toolbar_page_actions),
                View.VISIBLE
            )
            IdlingRegistry.getInstance().register(readerViewNotification)
        }
            navigationToolbar{
            verifyReaderViewDetected(visible = true)
        }
    }
}
