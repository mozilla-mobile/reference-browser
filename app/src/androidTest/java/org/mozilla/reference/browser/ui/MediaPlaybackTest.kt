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
import org.mozilla.reference.browser.ui.robots.notificationShade

class MediaPlaybackTest {

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
    fun audioPlaybackTest() {
        val audioTestPage = TestAssetHelper.getAudioPageAsset(mockWebServer)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(audioTestPage.url) {
            clickMediaPlayerControlButton("Play")
        }

        notificationShade {
            verifySystemNotificationExists(audioTestPage.title)
            clickSystemMediaNotificationControlButton("Pause")
            verifySystemMediaNotificationControlButtonState("Play")
            clickSystemMediaNotificationControlButton("Play")
        }.closeNotification {
            verifyMediaPlayerControlButtonState("Pause")
        }
    }

    @Test
    fun videoPlaybackTest() {
        val videoTestPage = TestAssetHelper.getVideoPageAsset(mockWebServer)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(videoTestPage.url) {
            clickMediaPlayerControlButton("Play")
        }

        notificationShade {
            verifySystemNotificationExists(videoTestPage.title)
            verifySystemMediaNotificationControlButtonState("Pause")
            clickSystemMediaNotificationControlButton("Pause")
            verifySystemMediaNotificationControlButtonState("Play")
        }.closeNotification {}
    }

    @Test
    fun hiddenVideoControlsContextMenuTest() {
        val noControlsVideoTestPage = TestAssetHelper.getNoControlsVideoPageAsset(mockWebServer)

        navigationToolbar {
        }.enterUrlAndEnterToBrowser(noControlsVideoTestPage.url) {
            longClickMatchingText("test_link_video")
            verifyNoControlsVideoContextMenuItems()
        }
    }
}
