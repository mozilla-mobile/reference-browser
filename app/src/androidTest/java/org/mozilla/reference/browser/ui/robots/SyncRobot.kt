package org.mozilla.reference.browser.ui.robots

import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import junit.framework.Assert.assertTrue
import org.mozilla.reference.browser.helpers.TestAssetHelper
import org.mozilla.reference.browser.helpers.TestHelper.packageName

class SyncRobot {

    fun verifyConnectedMessage() = assertConnectedMessage()
    fun verifySettingsSyncAccount(accountName: String) = assertSettingsSyncAccount(accountName)

    fun typeEmail(emailAddress: String) {
        val emailInput = mDevice.findObject(UiSelector()
            .index(0)
            .className("android.widget.EditText"))
        emailInput.waitForExists(TestAssetHelper.waitingTime)

        emailInput.setText(emailAddress)
    }

    fun typePassword(passwordValue: String) {
        val passwordInput = mDevice.findObject(UiSelector()
            .index(0)
            .className("android.widget.EditText")
            .resourceId("password"))

        passwordInput.setText(passwordValue)
    }

    fun clickContinueButton() {
        val continueButton = mDevice.findObject(By.res("submit-btn"))
        continueButton.clickAndWait(Until.newWindow(), TestAssetHelper.waitingTime)
    }

    fun clickSignInButton() {
        val signInButton = mDevice.findObject(By.res("submit-btn"))
        signInButton.clickAndWait(Until.newWindow(), TestAssetHelper.waitingTime)
    }

    fun waitForSignInToFinish() =
        mDevice.wait(Until.gone(By.res("submit-btn")), TestAssetHelper.waitingTime)


    fun verifyFXAUrl() {
        verifyUrl("https://accounts.firefox.com")
    }

    class Transition {}
}

private fun assertConnectedMessage() = assertTrue(mDevice.findObject(UiSelector().text("Connected"))
    .waitForExists(TestAssetHelper.waitingTime))

private fun assertSettingsSyncAccount(accountName: String) = assertTrue(mDevice.findObject(UiSelector()
    .text(accountName))
    .waitForExists(TestAssetHelper.waitingTime))

private fun verifyUrl(expectedUrl: String) {
    mDevice.findObject(UiSelector()
        .resourceId("$packageName:id/toolbar"))
        .waitForExists(TestAssetHelper.waitingTime)
    mDevice.findObject(UiSelector()
        .resourceId("$packageName:id/mozac_browser_toolbar_url_view"))
        .waitForExists(TestAssetHelper.waitingTime)
    mDevice.findObject(UiSelector().textContains(expectedUrl))
        .waitForExists(TestAssetHelper.waitingTime)
}