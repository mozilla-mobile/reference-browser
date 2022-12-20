/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import mozilla.components.concept.sync.Device
import mozilla.components.concept.sync.TabData
import mozilla.components.support.base.log.logger.Logger
import org.mozilla.reference.browser.IntentRequestCodes.REQUEST_CODE_DATA_REPORTING
import org.mozilla.reference.browser.IntentRequestCodes.REQUEST_CODE_SEND_TAB
import android.app.NotificationManager as AndroidNotificationManager

/**
 * Manages notification channels and allows displaying different supported types of notifications.
 */
object NotificationManager {

    // Send Tab
    private const val RECEIVE_TABS_TAG = "org.mozilla.reference.browser.receivedTabs"
    private const val RECEIVE_TABS_CHANNEL_ID = "org.mozilla.reference.browser.ReceivedTabsChannel"

    // Data Reporting
    private const val PRIVACY_NOTICE_URL = "https://www.mozilla.org/en-US/privacy/firefox/"
    private const val DATA_REPORTING_VERSION = 1
    private const val DATA_REPORTING_TAG = "org.mozilla.reference.browser.DataReporting"
    private const val DATA_REPORTING_NOTIFICATION_ID = 1
    private const val PREFS_POLICY_VERSION = "datareporting.policy.dataSubmissionPolicyVersion"
    private const val PREFS_POLICY_NOTIFIED_TIME =
        "datareporting.policy.dataSubmissionPolicyNotifiedTime"

    // Default
    private const val NOTIFICATION_CHANNEL_ID = "default-notification-channel"

    // Use an incrementing notification ID since they have the same tag.
    private var notificationIdCount = 0
    private val logger = Logger("NotificationManager")

    fun showReceivedTabs(context: Context, device: Device?, tabs: List<TabData>) {
        // In the future, experiment with displaying multiple tabs from the same device as as Notification Groups.
        // For now, a single notification per tab received will suffice.
        logger.debug("Showing ${tabs.size} tab(s) received from deviceID=${device?.id}")

        tabs.forEach { tab ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tab.url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val flags = if (SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_ONE_SHOT
            } else {
                PendingIntent.FLAG_ONE_SHOT
            }

            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_SEND_TAB,
                intent,
                flags,
            )
            val importance = if (SDK_INT >= Build.VERSION_CODES.N) {
                // We pick 'IMPORTANCE_HIGH' priority because this is a user-triggered action that is
                // expected to be part of a continuity flow. That is, user is expected to be waiting for
                // this notification on their device; make it obvious.
                AndroidNotificationManager.IMPORTANCE_HIGH
            } else {
                null
            }
            val channelId = getNotificationChannelId(
                context,
                RECEIVE_TABS_CHANNEL_ID,
                context.getString(R.string.fxa_received_tab_channel_name),
                context.getString(R.string.fxa_received_tab_channel_description),
                importance,
            )

            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setSendTabTitle(context, device, tab)
                .setWhen(System.currentTimeMillis())
                .setContentText(tab.url)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_VIBRATE or Notification.DEFAULT_SOUND)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)

            NotificationManagerCompat.from(context).notify(
                RECEIVE_TABS_TAG,
                notificationIdCount++,
                builder.build(),
            )
        }
    }

    fun checkAndNotifyPolicy(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val currentVersion = preferences.getInt(PREFS_POLICY_VERSION, -1)

        if (currentVersion < 1) {
            // This is a first run, so notify user about data policy.
            notifyDataPolicy(context, preferences)
        }
    }

    /**
     * Launch a notification of the data policy, and record notification time and version.
     */
    private fun notifyDataPolicy(context: Context, preferences: SharedPreferences) {
        val resources = context.resources

        val notificationTitle = resources.getString(R.string.datareporting_notification_title)
        val notificationSummary = resources.getString(R.string.datareporting_notification_summary)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(PRIVACY_NOTICE_URL)
            setPackage(context.packageName)
        }

        val flags = if (SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }

        val pendingIntent =
            PendingIntent.getActivity(context, REQUEST_CODE_DATA_REPORTING, intent, flags)

        val notificationBuilder = NotificationCompat.Builder(
            context,
            getNotificationChannelId(context),
        ).apply {
            setContentTitle(notificationTitle)
            setContentText(notificationSummary)
            setSmallIcon(R.drawable.ic_notification)
            setAutoCancel(true)
            setContentIntent(pendingIntent)
            setStyle(NotificationCompat.BigTextStyle().bigText(notificationSummary))
        }

        NotificationManagerCompat.from(context)
            .notify(DATA_REPORTING_TAG, DATA_REPORTING_NOTIFICATION_ID, notificationBuilder.build())

        preferences.edit()
            .putLong(PREFS_POLICY_NOTIFIED_TIME, System.currentTimeMillis())
            .putInt(PREFS_POLICY_VERSION, DATA_REPORTING_VERSION)
            .apply()
    }

    private fun getNotificationChannelId(
        context: Context,
        channelId: String = NOTIFICATION_CHANNEL_ID,
        channelName: String = context.resources.getString(R.string.default_notification_channel),
        description: String? = null,
        channelImportance: Int? = null,
    ): String {
        if (SDK_INT >= Build.VERSION_CODES.O) {
            val importance = channelImportance ?: AndroidNotificationManager.IMPORTANCE_DEFAULT
            createNotificationChannelIfNeeded(
                context,
                channelId,
                channelName,
                description,
                importance,
            )
        }

        return channelId
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannelIfNeeded(
        context: Context,
        channelId: String,
        channelName: String,
        channelDescription: String?,
        importance: Int = AndroidNotificationManager.IMPORTANCE_DEFAULT,
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager

        if (null != notificationManager.getNotificationChannel(channelId)) {
            return
        }

        val channel = NotificationChannel(
            channelId,
            channelName,
            importance,
        ).apply {
            description = channelDescription
        }

        notificationManager.createNotificationChannel(channel)
    }

    private fun NotificationCompat.Builder.setSendTabTitle(
        context: Context,
        device: Device?,
        tab: TabData,
    ): NotificationCompat.Builder {
        device?.let {
            setContentTitle(
                context.getString(
                    R.string.fxa_tab_received_from_notification_name,
                    it.displayName,
                ),
            )
            return this
        }

        if (tab.title.isEmpty()) {
            setContentTitle(context.getString(R.string.fxa_tab_received_notification_name))
        } else {
            setContentTitle(tab.title)
        }
        return this
    }
}
