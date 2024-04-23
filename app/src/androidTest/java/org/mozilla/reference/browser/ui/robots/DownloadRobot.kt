/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.uiautomator.UiSelector
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime
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

private val closeDownloadButton = mDevice.findObject(UiSelector().resourceId("$packageName:id/close_button"))
private val downloadButton = mDevice.findObject(UiSelector().resourceId("$packageName:id/download_button"))
