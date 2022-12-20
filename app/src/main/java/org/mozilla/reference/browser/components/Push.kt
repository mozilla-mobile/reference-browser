/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.components

import android.annotation.SuppressLint
import android.content.Context
import mozilla.components.feature.push.AutoPushFeature
import mozilla.components.feature.push.PushConfig
import mozilla.components.lib.crash.CrashReporter
import mozilla.components.support.base.log.logger.Logger
import org.mozilla.reference.browser.push.FirebasePush

/**
 * Component group for push services. These components use services that strongly depend on
 * push messaging (e.g. WebPush, SendTab).
 */
@SuppressLint("DiscouragedApi")
class Push(
    context: Context,
    crashReporter: CrashReporter,
) {
    val feature by lazy {
        pushConfig?.let { config ->
            AutoPushFeature(
                context = context,
                service = pushService,
                config = config,
                crashReporter = crashReporter,
            )
        }
    }

    /**
     * The push configuration data class used to initialize the AutoPushFeature.
     *
     * If we have the `project_id` resource, then we know that the Firebase configuration and API
     * keys are available for the FCM service to be used.
     */
    private val pushConfig by lazy {
        val logger = Logger("AutoPush")

        val resId = context.resources.getIdentifier("project_id", "string", context.packageName)
        if (resId == 0) {
            logger.info("No push keys found. Exiting..")
            return@lazy null
        }
        logger.info("Push keys detected, instantiation beginning..")
        val projectId = context.resources.getString(resId)
        PushConfig(projectId)
    }

    private val pushService by lazy { FirebasePush() }
}
