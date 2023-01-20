package org.mozilla.reference.browser.ui.robots

import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime
import org.mozilla.reference.browser.helpers.TestHelper.getPermissionAllowID
import org.mozilla.reference.browser.helpers.TestHelper.packageName

class DownloadRobot {
    fun cancelDownload() {
        closeDownloadButton.waitForExists(waitingTime)
        closeDownloadButton.click()
    }

    fun confirmDownload() {
        downloadButton.waitForExists(waitingTime)
        downloadButton.click()
    }

    class Transition
}

fun downloadRobot(interact: DownloadRobot.() -> Unit): DownloadRobot.Transition {
    DownloadRobot().interact()
    return DownloadRobot.Transition()
}

private fun assertDownloadPopup() {
    mDevice.waitForIdle()
    assertTrue(
        mDevice.findObject(UiSelector().resourceId("$packageName:id/filename"))
            .waitForExists(waitingTime),
    )
}

private fun clickAllowButton() {
    mDevice.waitForIdle()
    mDevice.wait(
        Until.findObject(
            By.res(getPermissionAllowID() + ":id/permission_message"),
        ),
        waitingTime,
    )
    mDevice.wait(
        Until.findObject(
            By.res(getPermissionAllowID() + ":id/permission_allow_button"),
        ),
        waitingTime,
    )

    val allowButton = mDevice.findObject(
        By.res(getPermissionAllowID() + ":id/permission_allow_button"),
    )
    allowButton.click()
}

private val closeDownloadButton = mDevice.findObject(UiSelector().resourceId("$packageName:id/close_button"))
private val downloadButton = mDevice.findObject(UiSelector().resourceId("$packageName:id/download_button"))
