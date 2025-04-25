/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.helpers

import android.net.Uri
import okhttp3.mockwebserver.MockWebServer
import org.mozilla.reference.browser.helpers.ext.toUri
import java.util.concurrent.TimeUnit

/**
 * Helper for hosting web pages locally for testing purposes.
 */
object TestAssetHelper {
    val waitingTime: Long = TimeUnit.SECONDS.toMillis(15)
    val waitingTimeShort: Long = TimeUnit.SECONDS.toMillis(1)

    data class TestAsset(
        val url: Uri,
        val content: String,
        val title: String,
    )

    /**
     * Hosts 3 simple websites, found at androidTest/assets/pages/generic[1|2|3].html
     * Returns a list of TestAsset, which can be used to navigate to each and
     * assert that the correct information is being displayed.
     *
     * Content for these pages all follow the same pattern. See [generic1.html] for
     * content implementation details.
     */
    fun getGenericAssets(server: MockWebServer): List<TestAsset> {
        return (1..4).map {
            TestAsset(
                server.url("pages/generic$it.html").toString().toUri()!!,
                "Page content: $it",
                "",
            )
        }
    }

    fun getGenericAsset(
        server: MockWebServer,
        pageNum: Int,
    ): TestAsset {
        val url = server.url("pages/generic$pageNum.html").toString().toUri()!!
        val content = "Page content: $pageNum"
        val title = "Test_Page_$pageNum"

        return TestAsset(url, content, title)
    }

    fun getLoremIpsumAsset(server: MockWebServer): TestAsset {
        val url = server.url("pages/lorem-ipsum.html").toString().toUri()!!
        val content = "Page content: lorem ipsum"
        return TestAsset(url, content, "")
    }

    fun getRefreshAsset(server: MockWebServer): TestAsset {
        val url = server.url("pages/refresh.html").toString().toUri()!!
        val content = "Page content: refresh"

        return TestAsset(url, content, "")
    }

    fun getUUIDPage(server: MockWebServer): TestAsset {
        val url = server.url("pages/basic_nav_uuid.html").toString().toUri()!!
        val content = "Page content: basic_nav_uuid"

        return TestAsset(url, content, "")
    }

    fun getImageAsset(server: MockWebServer): TestAsset {
        val url = server.url("resources/rabbit.jpg").toString().toUri()!!

        return TestAsset(url, "", "")
    }

    fun getDownloadAsset(server: MockWebServer): TestAsset {
        val url = server.url("pages/download.html").toString().toUri()!!
        val content = "Page content: web_icon.png"

        return TestAsset(url, content, "")
    }

    fun getAudioPageAsset(server: MockWebServer): TestAsset {
        val url = server.url("pages/audioMediaPage.html").toString().toUri()!!
        val title = "Audio_Test_Page"
        val content = "Page content: audio player"

        return TestAsset(url, content, title)
    }

    fun getVideoPageAsset(server: MockWebServer): TestAsset {
        val url = server.url("pages/videoMediaPage.html").toString().toUri()!!
        val title = "Video_Test_Page"
        val content = "Page content: video player"

        return TestAsset(url, content, title)
    }

    fun getNoControlsVideoPageAsset(server: MockWebServer): TestAsset {
        val url = server.url("pages/noControlsVideoMediaPage.html").toString().toUri()!!
        val title = "No_Controls_Video_Test_Page"
        val content = "Page content: video player"

        return TestAsset(url, content, title)
    }
}
