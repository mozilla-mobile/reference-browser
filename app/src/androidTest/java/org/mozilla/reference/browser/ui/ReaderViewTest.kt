/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui

import android.view.View
import androidx.test.espresso.IdlingRegistry
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.helpers.AndroidAssetDispatcher
import org.mozilla.reference.browser.helpers.BrowserActivityTestRule
import org.mozilla.reference.browser.helpers.TestAssetHelper
import org.mozilla.reference.browser.helpers.ViewVisibilityIdlingResource
import org.mozilla.reference.browser.ui.robots.navigationToolbar

class ReaderViewTest {
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

        if (readerViewNotification != null) {
            IdlingRegistry.getInstance().unregister(readerViewNotification!!)
        }
    }

    @Test
    fun verifyReaderViewPageMenuDetection() {
        val loremIpsumWebPage = TestAssetHelper.getLoremIpsumAsset(mockWebServer)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(loremIpsumWebPage.url) {
        }.openNavigationToolbar {
            waitForReaderViewIcon()
            readerViewNotification = ViewVisibilityIdlingResource(
                activityTestRule.activity.findViewById(R.id.mozac_browser_toolbar_page_actions),
                View.VISIBLE)
            IdlingRegistry.getInstance().register(readerViewNotification)
            verifyReaderViewIcon()
        }
    }
}
