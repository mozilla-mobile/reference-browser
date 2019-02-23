
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.web.model.Atoms
import androidx.test.espresso.web.sugar.Web.onWebView
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import org.junit.Assert.assertTrue
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime

class BrowserRobot {

    /**
     * Executes the given JS string.
     * @return if the final expression is a return statement, returns the provided String, else null.
     */
    fun executeJS(js: String): String? {
        return webView()
                .perform(Atoms.script(js))
                .get().value as? String
    }

    /**
     * Asserts that the text within DOM element with ID="testContent" has the given text, i.e.
     *   document.querySelector('#testContent').innerText == expectedText
     */
    fun verifyPageContent(expectedText: String) {
        val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        mDevice.waitForIdle()
        val testContent = mDevice.findObject((UiSelector().textContains(expectedText)))
        testContent.waitForExists(waitingTime)
        assertTrue(testContent.exists())
    }

    fun verifyUrl() {
        val redirectUrl = "https://github.com/login?return_to=https%3A%2F%2Fgithub.com%2Fmozilla-mobile%2Freference-browser%2Fissues%2Fnew"
        onView(withText(redirectUrl))
    }

    class Transition {
        private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        fun openNavigationToolbar(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
            device.pressMenu()

            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }
    }
}

fun browser(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
    BrowserRobot().interact()
    return BrowserRobot.Transition()
}

private fun webView() = onWebView()
