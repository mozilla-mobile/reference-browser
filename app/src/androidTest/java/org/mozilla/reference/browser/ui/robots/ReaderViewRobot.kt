package org.mozilla.reference.browser.ui.robots

import androidx.test.uiautomator.UiSelector
import junit.framework.Assert.assertTrue
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime
import org.mozilla.reference.browser.helpers.TestHelper.packageName

class ReaderViewRobot {

    fun verifyAppearanceButton() = assertAppearanceButton()

    class Transition
}
private fun assertAppearanceButton() {
    mDevice.waitForWindowUpdate(packageName, waitingTime)
    assertTrue(mDevice.findObject(
        UiSelector().resourceId("$packageName:id/readerViewAppearanceButton"))
        .waitForExists(waitingTime))
}
