package org.mozilla.reference.browser.ui.robots

import androidx.test.uiautomator.UiSelector
import junit.framework.Assert.assertTrue
import org.mozilla.reference.browser.helpers.TestAssetHelper

/**
 * Implementation of Robot Pattern for download UI handling.
 */

class DownloadRobot {

    fun verifyAllowFileAccessPopup() = assertAllowFileAccessPopup()
    fun clickAllowButton() = allowButton().click()

    class Transition
}

fun downloadRobot(interact: DownloadRobot.() -> Unit): DownloadRobot.Transition {
    mDevice.waitForIdle()
    DownloadRobot().interact()
    return DownloadRobot.Transition()
}

private fun allowButton() = mDevice.findObject(UiSelector().text("ALLOW"))

private fun assertAllowFileAccessPopup() {
    assertTrue(
        mDevice.findObject(UiSelector().text("Allow Reference Browser to access photos, media, and files on your device?"))
            .waitForExists(TestAssetHelper.waitingTime)
    )
}
