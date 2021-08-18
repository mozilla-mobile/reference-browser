package org.mozilla.reference.browser.ui.robots

import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import junit.framework.Assert.assertTrue
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime
import org.mozilla.reference.browser.helpers.TestHelper.getPermissionAllowID
import org.mozilla.reference.browser.helpers.TestHelper.packageName

class DownloadRobot {

    fun clickCancelDownloadButton() = cancelDownload()
    fun clickDownloadButton() = confirmDownload()

    class Transition
}

fun downloadRobot(interact: DownloadRobot.() -> Unit): DownloadRobot.Transition {
    mDevice.waitForIdle()
    DownloadRobot().interact()
    return DownloadRobot.Transition()
}

@Suppress("SwallowedException")
private fun cancelDownload() {
    try {
        // Allow storage permission if displayed
        clickAllowButton()
        assertDownloadPopup()
        mDevice.findObject(UiSelector().resourceId("$packageName:id/close_button")).clickAndWaitForNewWindow()
    } catch (e: NullPointerException) {
        println("The storage permission pop-up wasn't displayed")
        assertDownloadPopup()
        // Close RB's download prompt
        mDevice.findObject(UiSelector().resourceId("$packageName:id/close_button")).clickAndWaitForNewWindow()
    }
}

@Suppress("SwallowedException")
private fun confirmDownload() {
    try {
        // Allow storage permission if displayed
        clickAllowButton()
        assertDownloadPopup()
        mDevice.findObject(UiSelector().resourceId("$packageName:id/download_button")).clickAndWaitForNewWindow()
    } catch (e: NullPointerException) {
        println("The storage permission pop-up wasn't displayed")
        assertDownloadPopup()
        // Proceed with the download, click download from RB's download prompt
        mDevice.findObject(UiSelector().resourceId("$packageName:id/download_button")).clickAndWaitForNewWindow()
    }
}

private fun assertDownloadPopup() {
    mDevice.waitForIdle()
    assertTrue(
        mDevice.findObject(UiSelector().resourceId("$packageName:id/filename"))
            .waitForExists(waitingTime)
    )
}

private fun clickAllowButton() {
    mDevice.waitForIdle()
    mDevice.wait(Until.findObject(
        By.res(getPermissionAllowID() + ":id/permission_message")), waitingTime)
    mDevice.wait(Until.findObject(
        By.res(getPermissionAllowID() + ":id/permission_allow_button")), waitingTime)

    val allowButton = mDevice.findObject(
        By.res(getPermissionAllowID() + ":id/permission_allow_button"))
    allowButton.click()
}
