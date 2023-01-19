package org.mozilla.reference.browser.ui.robots

import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime

class NotificationRobot {

    @Suppress("SwallowedException")
    fun verifySystemMediaNotificationExists(notificationMessage: String) {
        assertTrue(systemMediaNotification.waitForExists(waitingTime))
        assertTrue(systemMediaNotificationTitle(notificationMessage).waitForExists(waitingTime))
    }

    fun clickSystemMediaNotificationControlButton(state: String) {
        systemMediaNotificationControlButton(state).waitForExists(waitingTime)
        systemMediaNotificationControlButton(state).click()
    }

    fun verifySystemMediaNotificationControlButtonState(action: String) {
        assertTrue(systemMediaNotificationControlButton(action).waitForExists(waitingTime))
    }

    fun verifyDownloadNotificationExist(notificationMessage: String, fileName: String) {
        val notification = UiSelector().text(notificationMessage)
        var notificationFound = mDevice.findObject(notification).waitForExists(waitingTime)
        val downloadFilename = mDevice.findObject(UiSelector().text(fileName))

        while (!notificationFound) {
            notificationTray.swipeUp(2)
            notificationFound = mDevice.findObject(notification).waitForExists(waitingTime)
        }
        assertTrue(notificationFound)
        assertTrue(downloadFilename.exists())
    }

    fun verifyDownloadNotificationDoesNotExist(notificationMessage: String, fileName: String) {
        val notification = UiSelector().text(notificationMessage)
        val notificationFound = mDevice.findObject(notification).waitForExists(waitingTime)
        val downloadFilename = mDevice.findObject(UiSelector().text(fileName))

        notificationTray.swipeUp(2)

        assertFalse(notificationFound)
        assertFalse(downloadFilename.exists())
    }

    class Transition {

        fun closeNotification(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            mDevice.pressBack()

            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }
    }
}

fun notificationShade(interact: NotificationRobot.() -> Unit): NotificationRobot.Transition {
    mDevice.waitForIdle()
    mDevice.openNotification()

    NotificationRobot().interact()
    return NotificationRobot.Transition()
}

private fun systemMediaNotificationControlButton(state: String) =
    mDevice.findObject(
        UiSelector()
            .resourceId("com.android.systemui:id/action0")
            .className("android.widget.ImageButton")
            .packageName("com.android.systemui")
            .descriptionContains(state),
    )

private fun systemMediaNotificationTitle(title: String) =
    mDevice.findObject(
        UiSelector()
            .textContains(title)
            .resourceId("com.android.systemui:id/header_title")
            .className("android.widget.TextView")
            .packageName("com.android.systemui"),
    )

private val notificationTray = UiScrollable(
    UiSelector().resourceId("com.android.systemui:id/notification_stack_scroller"),
).setAsVerticalList()

private val systemMediaNotification =
    mDevice.findObject(
        UiSelector()
            .resourceId("com.android.systemui:id/qs_media_controls")
            .className("android.view.ViewGroup")
            .packageName("com.android.systemui"),
    )
