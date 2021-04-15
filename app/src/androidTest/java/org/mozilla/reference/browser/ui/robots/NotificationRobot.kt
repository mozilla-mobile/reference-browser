package org.mozilla.reference.browser.ui.robots

import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import junit.framework.Assert.assertTrue
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime

class NotificationRobot {

    fun verifySystemNotificationExists(notificationMessage: String) {
        fun notificationTray() =
            UiScrollable(UiSelector().resourceId("com.android.systemui:id/notification_stack_scroller"))

        var notificationFound = false

        do {
            try {
                notificationFound =
                    notificationTray()
                        .getChildByText(UiSelector()
                            .text(notificationMessage), notificationMessage, true)
                        .waitForExists(waitingTime)
                assertTrue(notificationFound)
            } catch (e: UiObjectNotFoundException) {
                notificationTray().scrollForward()
                mDevice.waitForIdle()
            }
        } while (!notificationFound)
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
