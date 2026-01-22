/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.macrobenchmark

import androidx.test.platform.app.InstrumentationRegistry
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.Headers
import org.junit.rules.ExternalResource
import java.io.IOException
import java.io.InputStream

/**
 * A JUnit [ExternalResource] that manages the lifecycle of a [MockWebServer] instance.
 *
 * A new server will be started for each test run.
 *
 * The server is configured with a [Dispatcher] that will return Android assets in the body of
 * requests. If the dispatcher is unable to read a requested asset, it will return a 404 response.
 *
 * @param port The port to start the server on. If 0 (default) a random available port will be used.
 */
class MockWebServerRule(
    private val port: Int = 0,
) : ExternalResource() {

    lateinit var server: MockWebServer
        private set

    override fun before() {
        server = MockWebServer().apply {
            start(this@MockWebServerRule.port)
            dispatcher = AndroidAssetDispatcher()
        }
    }

    /**
     * A [MockWebServer] [Dispatcher] that will return Android assets in the body of requests.
     *
     * If the dispatcher is unable to read a requested asset, it will return a 404 response.
     */
    private class AndroidAssetDispatcher : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            val assetManager = InstrumentationRegistry.getInstrumentation().context.assets
            try {
                return assetManager.open(HtmlAsset.LONG.fileName).use { it.toResponse() }
            } catch (e: IOException) {
                println(e)
            }
            return MockResponse(HTTP_NOT_FOUND)
        }

        @Throws(IOException::class)
        private fun InputStream.toResponse() = MockResponse(
            code = HTTP_OK,
            body = bufferedReader().use { it.readText() },
            headers = Headers.Builder().set(HTML_CHARSET_HEADER_NAME, HTML_CHARSET).build()
        )

        companion object {
            const val HTTP_OK = 200
            const val HTTP_NOT_FOUND = 404
            const val HTML_CHARSET_HEADER_NAME = "content-type"
            const val HTML_CHARSET = "text/html; charset=utf-8"
        }
    }

}
