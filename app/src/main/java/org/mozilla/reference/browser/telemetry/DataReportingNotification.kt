/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.telemetry

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.mozilla.reference.browser.R

/**
 * Data Reporting notification to be shown on the first app start and whenever the data policy version changes.
 */
object DataReportingNotification {
    private const val PREFS_POLICY_NOTIFIED_TIME = "datareporting.policy.dataSubmissionPolicyNotifiedTime"
    private const val PREFS_POLICY_VERSION = "datareporting.policy.dataSubmissionPolicyVersion"

    private const val PRIVACY_NOTICE_URL = "https://www.mozilla.org/en-US/privacy/firefox/"

    private const val DATA_REPORTING_VERSION = 1

    private const val NOTIFICATION_ID = 1

    private const val NOTIFICATION_CHANNEL_ID = "default-notification-channel"

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

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val notificationBuilder = NotificationCompat.Builder(context, getNotificationChannelId(context))
            .setContentTitle(notificationTitle)
            .setContentText(notificationSummary)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationSummary))

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID, notificationBuilder.build())

        preferences.edit()
            .putLong(PREFS_POLICY_NOTIFIED_TIME, System.currentTimeMillis())
            .putInt(PREFS_POLICY_VERSION, DATA_REPORTING_VERSION)
            .apply()
    }

    private fun getNotificationChannelId(context: Context): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannelIfNeeded(context)
        }

        return NOTIFICATION_CHANNEL_ID
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannelIfNeeded(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (null != notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)) {
            return
        }

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            context.resources.getString(R.string.default_notification_channel),
            NotificationManager.IMPORTANCE_DEFAULT)

        notificationManager.createNotificationChannel(channel)
    }
}
