/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.helpers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.browser.customtabs.CustomTabsIntent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import mozilla.components.support.ktx.android.content.appName
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTimeShort
import org.mozilla.reference.browser.ui.robots.mDevice

object TestHelper {

    val packageName = InstrumentationRegistry.getInstrumentation().targetContext.packageName
    val appContext: Context = InstrumentationRegistry.getInstrumentation().targetContext
    val appName = appContext.appName

    fun scrollToElementByText(text: String): UiScrollable {
        val appView = UiScrollable(UiSelector().scrollable(true))
        appView.scrollTextIntoView(text)
        return appView
    }

    fun getPermissionAllowID(): String {
        return when (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            true -> "com.android.permissioncontroller"
            false -> "com.android.packageinstaller"
        }
    }

    fun createCustomTabIntent(
        pageUrl: String,
        customActionButtonDescription: String = "",
    ): Intent {
        val appContext = InstrumentationRegistry.getInstrumentation()
            .targetContext
            .applicationContext
        val pendingIntent = PendingIntent.getActivity(appContext, 0, Intent(), 0)
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShareState(CustomTabsIntent.SHARE_STATE_ON)
            .setActionButton(
                createTestBitmap(),
                customActionButtonDescription,
                pendingIntent,
                true,
            )
            .build()
        customTabsIntent.intent.data = Uri.parse(pageUrl)
        return customTabsIntent.intent
    }

    private fun createTestBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.GREEN)
        return bitmap
    }

    fun UiDevice.waitForObjects(obj: UiObject, waitingTime: Long = TestAssetHelper.waitingTime) {
        this.waitForIdle()
        Assert.assertNotNull(obj.waitForExists(waitingTime))
    }

    fun itemWithResId(resourceId: String) =
        mDevice.findObject(UiSelector().resourceId(resourceId))

    fun itemWithText(itemText: String) =
        mDevice.findObject(UiSelector().text(itemText))

    fun itemWithResIdContainingText(resourceId: String, text: String) =
        mDevice.findObject(UiSelector().resourceId(resourceId).textContains(text))

    fun assertUIObjectExists(vararg appItems: UiObject, exists: Boolean = true) {
        if (exists) {
            for (appItem in appItems) {
                assertTrue(appItem.waitForExists(waitingTime))
            }
        } else {
            for (appItem in appItems) {
                assertFalse(appItem.waitForExists(waitingTimeShort))
            }
        }
    }

    fun getStringResource(id: Int) = appContext.resources.getString(id, appName)
}
