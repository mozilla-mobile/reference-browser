/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import android.graphics.Rect
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import mozilla.components.browser.toolbar.R as toolbarR
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert.assertTrue
import org.mozilla.reference.browser.helpers.Constants.LONG_CLICK_DURATION
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime
import org.mozilla.reference.browser.helpers.TestHelper.packageName
import org.mozilla.reference.browser.helpers.TestHelper.waitForObjects
import kotlin.math.abs

/** Implementation of Robot Pattern for browser action. */
class BrowserRobot {
    private var snackbarToolbarGap: Int? = null

    /* Asserts that the text within DOM element with ID="testContent" has the given text, i.e.
     * document.querySelector('#testContent').innerText == expectedText
     */
    fun verifyPageContent(expectedText: String) {
        mDevice.waitForObjects(mDevice.findObject(UiSelector().resourceId("android.webkit.WebView")))
        assertTrue(mDevice.findObject(UiSelector().textContains(expectedText)).waitForExists(waitingTime))
    }

    fun verifyFXAUrl() {
        verifyUrl("https://accounts.firefox.com")
    }

    fun verifyGithubUrl() {
        verifyUrl("https://github.com/login")
    }

    fun verifyUrl(expectedUrl: String) {
        mDevice.findObject(UiSelector().resourceId("$packageName:id/toolbar")).waitForExists(waitingTime)
        mDevice
            .findObject(UiSelector().resourceId("$packageName:id/mozac_browser_toolbar_url_view"))
            .waitForExists(waitingTime)
        mDevice.findObject(UiSelector().textContains(expectedUrl)).waitForExists(waitingTime)
        onView(
                allOf(
                    withSubstring(expectedUrl),
                    withId(toolbarR.id.mozac_browser_toolbar_url_view),
                    isDescendantOfA(withId(toolbarR.id.mozac_browser_toolbar_origin_view)),
                )
            )
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    fun verifyAboutBrowser() {
        assertTrue(
            mDevice.findObject(UiSelector().resourceId("$packageName:id/about_content")).waitForExists(waitingTime)
        )
        assertTrue(
            mDevice.findObject(UiSelector().resourceId("$packageName:id/version_info")).waitForExists(waitingTime)
        )
    }

    fun longClickMatchingText(expectedText: String) {
        mDevice.waitForWindowUpdate(packageName, waitingTime)
        mDevice.findObject(UiSelector().resourceId("$packageName:id/engineView")).waitForExists(waitingTime)
        mDevice.findObject(UiSelector().textContains(expectedText)).waitForExists(waitingTime)
        val link = mDevice.findObject(By.textContains(expectedText))
        link.click(LONG_CLICK_DURATION)
    }

    fun longClickVideoWithNoControls() {
        mDevice.waitForWindowUpdate(packageName, waitingTime)
        mDevice.findObject(UiSelector().resourceId("$packageName:id/engineView")).waitForExists(waitingTime)
        mDevice.waitForIdle()
        val engineView = mDevice.findObject(By.res("$packageName:id/engineView"))
        engineView.click(engineView.visibleCenter, LONG_CLICK_DURATION)
    }

    fun longClickAndCopyText(
        expectedText: String,
        selectAll: Boolean = false,
    ) {
        try {
            // Long click desired text
            mDevice.waitForWindowUpdate(packageName, waitingTime)
            mDevice.findObject(UiSelector().resourceId("$packageName:id/engineView")).waitForExists(waitingTime)
            mDevice.findObject(UiSelector().textContains(expectedText)).waitForExists(waitingTime)
            val link = mDevice.findObject(By.textContains(expectedText))
            link.click(LONG_CLICK_DURATION)

            // Click Select all from the text selection toolbar
            if (selectAll) {
                mDevice.findObject(UiSelector().textContains("Select all")).waitForExists(waitingTime)
                val selectAllText = mDevice.findObject(By.textContains("Select all"))
                selectAllText.click()
            }

            // Click Copy from the text selection toolbar
            mDevice.findObject(UiSelector().textContains("Copy")).waitForExists(waitingTime)
            val copyText = mDevice.findObject(By.textContains("Copy"))
            copyText.click()
        } catch (e: NullPointerException) {
            println("Failed to long click desired text: ${e.localizedMessage}")

            // Refresh the page in case the first long click didn't succeed
            navigationToolbar {}
                .openThreeDotMenu {}
                .refreshPage {
                    mDevice.waitForIdle()
                }

            // Long click again the desired text
            mDevice.waitForWindowUpdate(packageName, waitingTime)
            mDevice.findObject(UiSelector().resourceId("$packageName:id/engineView")).waitForExists(waitingTime)
            mDevice.findObject(UiSelector().textContains(expectedText)).waitForExists(waitingTime)
            val link = mDevice.findObject(By.textContains(expectedText))
            link.click(LONG_CLICK_DURATION)

            // Click again Select all from the text selection toolbar
            if (selectAll) {
                mDevice.findObject(UiSelector().textContains("Select all")).waitForExists(waitingTime)
                val selectAllText = mDevice.findObject(By.textContains("Select all"))
                selectAllText.click()
            }

            // Click again Copy from the text selection toolbar
            mDevice.findObject(UiSelector().textContains("Copy")).waitForExists(waitingTime)
            val copyText = mDevice.findObject(By.textContains("Copy"))
            copyText.click()
        }
    }

    fun verifyLinkContextMenuItems() {
        mDevice.waitForWindowUpdate(packageName, waitingTime)
        mDevice.findObject(UiSelector().resourceId("$packageName:id/parentPanel")).waitForExists(waitingTime)
        assertTrue(mDevice.findObject(UiSelector().resourceId("$packageName:id/titleView")).waitForExists(waitingTime))
        assertTrue(mDevice.findObject(UiSelector().textContains("Open link in new tab")).waitForExists(waitingTime))
        assertTrue(mDevice.findObject(UiSelector().textContains("Open link in private tab")).waitForExists(waitingTime))
        assertTrue(mDevice.findObject(UiSelector().textContains("Copy link")).waitForExists(waitingTime))
        assertTrue(mDevice.findObject(UiSelector().textContains("Share link")).waitForExists(waitingTime))
    }

    fun verifyNoControlsVideoContextMenuItems() {
        mDevice.waitForWindowUpdate(packageName, waitingTime)
        mDevice.findObject(UiSelector().resourceId("$packageName:id/parentPanel")).waitForExists(waitingTime)
        assertTrue(mDevice.findObject(UiSelector().resourceId("$packageName:id/titleView")).waitForExists(waitingTime))
        assertTrue(mDevice.findObject(UiSelector().textContains("Copy link")).waitForExists(waitingTime))
        assertTrue(mDevice.findObject(UiSelector().textContains("Share link")).waitForExists(waitingTime))
        assertTrue(mDevice.findObject(UiSelector().textContains("Save file to device")).waitForExists(waitingTime))
    }

    fun clickContextOpenLinkInNewTab() {
        mDevice.findObject(UiSelector().resourceId("$packageName:id/parentPanel")).waitForExists(waitingTime)
        mDevice.findObject(UiSelector().textContains("Open link in new tab")).waitForExists(waitingTime)

        val contextMenuOpenInNewTab = mDevice.findObject(UiSelector().textContains("Open link in new tab"))
        contextMenuOpenInNewTab.click()
    }

    fun clickContextOpenLinkInPrivateTab() {
        mDevice.findObject(UiSelector().resourceId("$packageName:id/parentPanel")).waitForExists(waitingTime)
        mDevice.findObject(UiSelector().textContains("Open link in private tab")).waitForExists(waitingTime)

        val contextMenuOpenInNewPrivateTab = mDevice.findObject(UiSelector().textContains("Open link in private tab"))
        contextMenuOpenInNewPrivateTab.click()
    }

    fun clickContextCopyLink() {
        mDevice.findObject(UiSelector().resourceId("$packageName:id/parentPanel")).waitForExists(waitingTime)
        mDevice.findObject(UiSelector().textContains("Copy link")).waitForExists(waitingTime)

        val contextCopyLink = mDevice.findObject(UiSelector().textContains("Copy link"))
        contextCopyLink.click()
    }

    fun waitUntilCopyLinkSnackbarIsGone() =
        mDevice
            .findObject(
                UiSelector().resourceId("$packageName:id/snackbar_text").textContains("Link copied to clipboard")
            )
            .waitUntilGone(waitingTime)

    fun verifyMediaPlayerControlButtonState(state: String) {
        mDevice.findObject(UiSelector().textContains("Audio_Test_Page")).waitForExists(waitingTime)
        mDevice.findObject(UiSelector().textContains("audio player")).waitForExists(waitingTime)
        assertTrue(mediaPlayerPlayButton(state).waitForExists(waitingTime))
    }

    fun clickMediaPlayerControlButton(state: String) {
        mediaPlayerPlayButton(state).waitForExists(waitingTime)
        mediaPlayerPlayButton(state).click()
        mDevice.waitForIdle()
    }

    fun clickOpenInAppPromptButton() =
        mDevice.findObject(UiSelector().resourceId("android:id/button1").textContains("OPEN")).also {
            it.waitForExists(waitingTime)
            it.click()
        }

    fun clickSnackbarSwitchButton() =
        mDevice.findObject(UiSelector().resourceId("$packageName:id/snackbar_action").textContains("SWITCH")).also {
            it.waitForExists(waitingTime)
            it.click()
        }

    fun verifySnackbarIsAnchoredAboveToolbar() {
        mDevice.findObject(UiSelector().resourceId("$packageName:id/snackbar_text")).waitForExists(waitingTime)
        onView(withId(com.google.android.material.R.id.snackbar_text)).check { snackbarText, exception ->
            if (exception != null) {
                throw exception
            }

            val snackbarContent = snackbarText.parent as View
            val snackbar = snackbarContent.parent as View
            val toolbar = snackbar.rootView.findViewById<View>(org.mozilla.reference.browser.R.id.toolbar)
            val snackbarBounds = Rect().also(snackbar::getGlobalVisibleRect)
            val toolbarBounds = Rect().also(toolbar::getGlobalVisibleRect)
            val visibleScreenBounds = Rect().also(snackbar.rootView::getWindowVisibleDisplayFrame)
            val gap = toolbarBounds.top - snackbarBounds.bottom
            val maximumGap = (snackbar.resources.displayMetrics.density * MAXIMUM_SNACKBAR_GAP_DP).toInt()
            val positionTolerance = (snackbar.resources.displayMetrics.density * POSITION_TOLERANCE_DP).toInt()

            assertTrue("Snackbar overlaps toolbar: snackbar=$snackbarBounds toolbar=$toolbarBounds", gap >= 0)
            assertTrue("Snackbar is too far above toolbar: gap=$gap", gap <= maximumGap)
            assertTrue(
                "Toolbar is not aligned with the visible screen: toolbar=$toolbarBounds screen=$visibleScreenBounds",
                abs(toolbarBounds.bottom - visibleScreenBounds.bottom) <= positionTolerance,
            )

            snackbarToolbarGap?.let { initialGap ->
                assertTrue(
                    "Snackbar gap changed from $initialGap to $gap",
                    abs(initialGap - gap) <= positionTolerance,
                )
            } ?: run {
                snackbarToolbarGap = gap
            }
        }
    }

    fun focusToolbarWithSnackbarShown() {
        mDevice
            .findObject(UiSelector().resourceId("$packageName:id/mozac_browser_toolbar_url_view"))
            .click()
        mDevice.findObject(UiSelector().textContains("Search or enter address")).waitForExists(waitingTime)
        mDevice.waitForIdle()
    }

    fun dismissKeyboardWithSnackbarShown() {
        mDevice.pressBack()
        mDevice.waitForIdle()
    }

    class Transition {
        fun checkExternalApps(interact: ExternalAppsRobot.() -> Unit): ExternalAppsRobot.Transition {
            mDevice.waitForWindowUpdate(packageName, waitingTime)
            ExternalAppsRobot().interact()
            return ExternalAppsRobot.Transition()
        }

        fun clickContextShareLink(interact: ContentPanelRobot.() -> Unit): ContentPanelRobot.Transition {
            mDevice.findObject(UiSelector().resourceId("$packageName:id/parentPanel")).waitForExists(waitingTime)
            mDevice.findObject(UiSelector().textContains("Share link")).waitForExists(waitingTime)

            val contextCopyLink = mDevice.findObject(UiSelector().textContains("Share link"))
            contextCopyLink.click()

            ContentPanelRobot().interact()
            return ContentPanelRobot.Transition()
        }

        fun goBack(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            mDevice.pressBack()
            mDevice
                .findObject(UiSelector().resourceId("$packageName:id/mozac_browser_toolbar_progress"))
                .waitUntilGone(waitingTime)
            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }
    }
}

private const val MAXIMUM_SNACKBAR_GAP_DP = 32
private const val POSITION_TOLERANCE_DP = 1

fun browser(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
    BrowserRobot().interact()
    return BrowserRobot.Transition()
}

private fun mediaPlayerPlayButton(state: String) =
    mDevice.findObject(UiSelector().className("android.widget.Button").text(state))
