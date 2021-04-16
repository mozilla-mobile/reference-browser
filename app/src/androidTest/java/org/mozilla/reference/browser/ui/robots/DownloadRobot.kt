package org.mozilla.reference.browser.ui.robots

import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import junit.framework.Assert.assertTrue
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime
import org.mozilla.reference.browser.helpers.TestHelper.getPermissionAllowID
import org.mozilla.reference.browser.helpers.TestHelper.packageName

class DownloadRobot {

    fun verifyDownloadPopup() = assertDownloadPopup()
    fun clickCancelDownloadButton() = cancelDownloadButton().clickAndWaitForNewWindow()
    fun clickDownloadButton() = downloadButton().clickAndWaitForNewWindow()
    fun clickAllowButton() {
        mDevice.waitForIdle()
        mDevice.wait(Until.findObject(
            By.res(getPermissionAllowID() + ":id/permission_message")), waitingTime)
        mDevice.wait(Until.findObject(
            By.res(getPermissionAllowID() + ":id/permission_allow_button")), waitingTime)

        val allowButton = mDevice.findObject(
            By.res(getPermissionAllowID() + ":id/permission_allow_button"))
        allowButton.click()
    }

    class Transition
}

fun downloadRobot(interact: DownloadRobot.() -> Unit): DownloadRobot.Transition {
    mDevice.waitForIdle()
    DownloadRobot().interact()
    return DownloadRobot.Transition()
}

private fun cancelDownloadButton() = mDevice.findObject(UiSelector().resourceId("$packageName:id/close_button"))
private fun downloadButton() = mDevice.findObject(UiSelector().resourceId("$packageName:id/download_button"))

private fun assertDownloadPopup() {
    mDevice.waitForIdle()
    assertTrue(
        mDevice.findObject(UiSelector().resourceId("$packageName:id/filename"))
            .waitForExists(waitingTime)
    )
}
