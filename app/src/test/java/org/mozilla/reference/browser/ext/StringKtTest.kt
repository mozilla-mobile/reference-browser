/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ext

import org.junit.Assert.assertEquals
import org.junit.Test

class StringKtTest {
    @Test
    fun `replace should replace all keys with their corresponding values`() {
        val input = "The quick brown fox jumps over the lazy dog"
        val pairs = mapOf(
            "quick" to "slow",
            "brown" to "red",
            "fox" to "cat",
            "dog" to "mouse",
        )

        val result = input.replace(pairs)

        assertEquals("The slow red cat jumps over the lazy mouse", result)
    }

    @Test
    fun `replace should return the same string if the map is empty`() {
        val input = "Hello world"
        val pairs = emptyMap<String, String>()

        val result = input.replace(pairs)

        assertEquals(input, result)
    }

    @Test
    fun `replace should handle keys that are not present in the string`() {
        val input = "Hello world"
        val pairs = mapOf("foo" to "bar")

        val result = input.replace(pairs)

        assertEquals("Hello world", result)
    }
}
