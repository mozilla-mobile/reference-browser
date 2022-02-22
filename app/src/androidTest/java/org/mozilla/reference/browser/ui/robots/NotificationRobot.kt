package org.mozilla.reference.browser.ui.robots

import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime

class NotificationRobot {
    @Suppress("SwallowedException")
    fun verifySystemNotificationDoesNotExists(notificationMessage: String) {
        fun notificationTray() =
            UiScrollable(UiSelector().resourceId("com.android.systemui:id/notification_stack_scroller"))

        var notificationFound = true

        do {
            try {
                notificationFound =
                    notificationTray()
                        .getChildByText(
                            UiSelector()
                                .text(notificationMessage),
                            notificationMessage, true
                        )
                        .waitForExists(waitingTime)
                assertFalse(notificationFound)
            } catch (e: UiObjectNotFoundException) {
                notificationTray().scrollForward()
                mDevice.waitForIdle()
            }
        } while (!notificationFound)
    }

    @Suppress("SwallowedException")
    fun verifySystemNotificationExists(notificationMessage: String) {
        fun notificationTray() =
            UiScrollable(UiSelector().resourceId("com.android.systemui:id/notification_stack_scroller"))

        var notificationFound = false

        do {
            try {
                notificationFound =
                    notificationTray()
                        .getChildByText(
                            UiSelector()
                                .text(notificationMessage),
                            notificationMessage, true
                        )
                        .waitForExists(waitingTime)
                assertTrue(notificationFound)
            } catch (e: UiObjectNotFoundException) {
                notificationTray().scrollForward()
                mDevice.waitForIdle()
            }
        } while (!notificationFound)
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
            .resourceId("android:id/action0")
            .descriptionContains(state)
    )

private val notificationTray = UiScrollable(
    UiSelector().resourceId("com.android.systemui:id/notification_stack_scroller")
).setAsVerticalList()
