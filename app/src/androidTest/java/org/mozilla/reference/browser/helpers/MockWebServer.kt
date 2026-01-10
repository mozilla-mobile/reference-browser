/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.helpers

import android.net.Uri
import android.os.Handler
import android.os.Looper
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okio.Buffer
import okio.source
import org.mozilla.reference.browser.helpers.ext.toUri
import java.io.IOException
import java.io.InputStream

object MockWebServerHelper {
    fun initMockWebServerAndReturnEndpoints(vararg messages: String): List<Uri> {
        val mockServer = MockWebServer()
        var uniquePath = 0
        val uris = mutableListOf<Uri>()

        messages.forEach { message ->
            val response = MockResponse(body = "<html><body>$message</body></html>")
            mockServer.enqueue(response)

            val endpoint = mockServer.url(uniquePath++.toString()).toString().toUri()!!
            uris += endpoint
        }

        return uris
    }
}

/**
 * A [MockWebServer] [Dispatcher] that will return Android assets in the body of requests.
 */
const val HTTP_OK = 200
const val HTTP_NOT_FOUND = 404

class AndroidAssetDispatcher : Dispatcher() {
    private val mainThreadHandler = Handler(Looper.getMainLooper())

    override fun dispatch(request: RecordedRequest): MockResponse {
        val assetManager = androidx.test.platform.app.InstrumentationRegistry
            .getInstrumentation()
            .context.assets

        return try {
            val assetPath = request.url.encodedPath.removePrefix("/")
            val normalizedPath = Uri.parse(assetPath).path ?: assetPath

            assetManager.open(normalizedPath).use { inputStream ->
                fileToResponse(normalizedPath, inputStream)
            }
        } catch (e: IOException) {
            mainThreadHandler.postAtFrontOfQueue { throw e }
            MockResponse(code = HTTP_NOT_FOUND)
        }
    }
}

@Throws(IOException::class)
private fun fileToResponse(
    path: String,
    file: InputStream,
): MockResponse =
    MockResponse
        .Builder()
        .code(HTTP_OK)
        .body(fileToBytes(file)!!)
        .addHeader("content-type: " + contentType(path))
        .build()

@Throws(IOException::class)
private fun fileToBytes(file: InputStream): Buffer? {
    val result = Buffer()
    result.writeAll(file.source())
    return result
}

private fun contentType(path: String): String? =
    when {
        path.endsWith(".png") -> "image/png"
        path.endsWith(".jpg") -> "image/jpeg"
        path.endsWith(".jpeg") -> "image/jpeg"
        path.endsWith(".gif") -> "image/gif"
        path.endsWith(".svg") -> "image/svg+xml"
        path.endsWith(".html") -> "text/html; charset=utf-8"
        path.endsWith(".txt") -> "text/plain; charset=utf-8"
        else -> "application/octet-stream"
    }
