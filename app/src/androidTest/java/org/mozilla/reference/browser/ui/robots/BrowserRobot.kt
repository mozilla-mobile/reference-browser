/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import junit.framework.Assert.assertTrue
import org.mozilla.reference.browser.ext.waitAndInteract
import org.mozilla.reference.browser.helpers.Constants.LONG_CLICK_DURATION
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime
import org.mozilla.reference.browser.helpers.TestHelper.packageName

/**
 * Implementation of Robot Pattern for browser action.
 */
class BrowserRobot {
    /* Asserts that the text within DOM element with ID="testContent" has the given text, i.e.
    * document.querySelector('#testContent').innerText == expectedText
    */
    fun verifyPageContent(expectedText: String) {
        val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        mDevice.waitAndInteract(Until.findObject(By.textContains(expectedText))) {}
    }

    fun verifyFXAUrl() {
        verifyUrl("https://accounts.firefox.com")
    }

    fun verifyGithubUrl() {
        verifyUrl("https://github.com/login")
    }

    fun verifyUrl(expectedUrl: String) {
        mDevice.findObject(UiSelector()
            .resourceId("$packageName:id/toolbar"))
            .waitForExists(waitingTime)
        mDevice.findObject(UiSelector()
            .resourceId("$packageName:id/mozac_browser_toolbar_url_view"))
            .waitForExists(waitingTime)
        mDevice.findObject(UiSelector().textContains(expectedUrl))
            .waitForExists(waitingTime)
    }

    fun verifyAboutBrowser() {
        assertTrue(mDevice.findObject(UiSelector().resourceId("$packageName:id/about_content"))
            .waitForExists(waitingTime))
        assertTrue(mDevice.findObject(UiSelector().resourceId("$packageName:id/version_info"))
            .waitForExists(waitingTime))
    }

    fun longClickMatchingText(expectedText: String) {
        mDevice.waitForWindowUpdate(packageName, waitingTime)
        mDevice.findObject(UiSelector().resourceId("$packageName:id/engineView"))
            .waitForExists(waitingTime)
        mDevice.findObject(UiSelector().textContains(expectedText)).waitForExists(waitingTime)
        val link = mDevice.findObject(By.textContains(expectedText))
            link.click(LONG_CLICK_DURATION)
    }

    fun verifyLinkContextMenuItems() {
        mDevice.waitForWindowUpdate(packageName, waitingTime)
        mDevice.findObject(UiSelector().resourceId("$packageName:id/parentPanel"))
            .waitForExists(waitingTime)
        assertTrue(mDevice.findObject(UiSelector().resourceId("$packageName:id/titleView"))
            .waitForExists(waitingTime))
        assertTrue(mDevice.findObject(UiSelector().textContains("Open link in new tab"))
            .waitForExists(waitingTime))
        assertTrue(mDevice.findObject(UiSelector().textContains("Open link in private tab"))
            .waitForExists(waitingTime))
        assertTrue(mDevice.findObject(UiSelector().textContains("Copy link"))
            .waitForExists(waitingTime))
        assertTrue(mDevice.findObject(UiSelector().textContains("Share link"))
            .waitForExists(waitingTime))
    }

    fun verifyNoControlsVideoContextMenuItems() {
        mDevice.waitForWindowUpdate(packageName, waitingTime)
        mDevice.findObject(UiSelector().resourceId("$packageName:id/parentPanel"))
            .waitForExists(waitingTime)
        assertTrue(mDevice.findObject(UiSelector().resourceId("$packageName:id/titleView"))
            .waitForExists(waitingTime))
        assertTrue(mDevice.findObject(UiSelector().textContains("Copy link"))
            .waitForExists(waitingTime))
        assertTrue(mDevice.findObject(UiSelector().textContains("Share link"))
            .waitForExists(waitingTime))
        assertTrue(mDevice.findObject(UiSelector().textContains("Save file to device"))
            .waitForExists(waitingTime))
    }

    fun clickContextOpenLinkInNewTab() {
        mDevice.findObject(UiSelector().resourceId("$packageName:id/parentPanel"))
            .waitForExists(waitingTime)
        mDevice.findObject(UiSelector().textContains("Open link in new tab"))
            .waitForExists(waitingTime)

        val contextMenuOpenInNewTab = mDevice.findObject(UiSelector().textContains("Open link in new tab"))
        contextMenuOpenInNewTab.click()
    }

    fun clickContextOpenLinkInPrivateTab() {
        mDevice.findObject(UiSelector().resourceId("$packageName:id/parentPanel"))
            .waitForExists(waitingTime)
        mDevice.findObject(UiSelector().textContains("Open link in private tab"))
            .waitForExists(waitingTime)

        val contextMenuOpenInNewPrivateTab = mDevice.findObject(UiSelector().textContains("Open link in private tab"))
        contextMenuOpenInNewPrivateTab.click()
    }

    fun clickContextCopyLink() {
        mDevice.findObject(UiSelector().resourceId("$packageName:id/parentPanel"))
            .waitForExists(waitingTime)
        mDevice.findObject(UiSelector().textContains("Copy link"))
            .waitForExists(waitingTime)

        val contextCopyLink = mDevice.findObject(UiSelector().textContains("Copy link"))
        contextCopyLink.click()
    }

    fun verifyMediaPlayerControlButtonState(state: String) {
        assertTrue(mediaPlayerPlayButton(state).waitForExists(waitingTime))
    }

    fun clickMediaPlayerControlButton(state: String) {
        mediaPlayerPlayButton(state).waitForExists(waitingTime)
        mediaPlayerPlayButton(state).click()
        mDevice.waitForIdle()
    }

    class Transition {
        private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        fun openNavigationToolbar(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
            device.pressMenu()

            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }

        fun checkExternalApps(interact: ExternalAppsRobot.() -> Unit): ExternalAppsRobot.Transition {
            mDevice.waitForWindowUpdate(packageName, waitingTime)
            ExternalAppsRobot().interact()
            return ExternalAppsRobot.Transition()
        }

        fun clickContextShareLink(interact: ContentPanelRobot.() -> Unit): ContentPanelRobot.Transition {
            mDevice.findObject(UiSelector().resourceId("$packageName:id/parentPanel"))
                .waitForExists(waitingTime)
            mDevice.findObject(UiSelector().textContains("Share link"))
                .waitForExists(waitingTime)

            val contextCopyLink = mDevice.findObject(UiSelector().textContains("Share link"))
            contextCopyLink.click()

            ContentPanelRobot().interact()
            return ContentPanelRobot.Transition()
        }
    }
}

fun browser(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
    BrowserRobot().interact()
    return BrowserRobot.Transition()
}

private fun mediaPlayerPlayButton(state: String) =
    mDevice.findObject(
        UiSelector()
            .className("android.widget.Button")
            .text(state)
    )
