/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import mozilla.components.feature.autofill.AutofillConfiguration
import mozilla.components.support.base.android.NotificationsDelegate
import org.mozilla.reference.browser.autofill.AutofillConfirmActivity
import org.mozilla.reference.browser.autofill.AutofillSearchActivity
import org.mozilla.reference.browser.autofill.AutofillUnlockActivity
import org.mozilla.reference.browser.components.Analytics
import org.mozilla.reference.browser.components.BackgroundServices
import org.mozilla.reference.browser.components.Core
import org.mozilla.reference.browser.components.Push
import org.mozilla.reference.browser.components.Services
import org.mozilla.reference.browser.components.UseCases
import org.mozilla.reference.browser.components.Utilities

/**
 * Provides access to all components.
 */
class Components(private val context: Context) {
    val core by lazy { Core(context) }
    val useCases by lazy {
        UseCases(
            context,
            core.engine,
            core.store,
            core.shortcutManager,
        )
    }

    // Background services are initiated eagerly; they kick off periodic tasks and setup an accounts system.
    val backgroundServices by lazy {
        BackgroundServices(
            context,
            push,
            core.lazyHistoryStorage,
            core.lazyRemoteTabsStorage,
            core.lazyLoginsStorage,
        )
    }
    val analytics by lazy { Analytics(context) }
    val utils by lazy {
        Utilities(
            context,
            core.store,
            useCases.sessionUseCases,
            useCases.searchUseCases,
            useCases.tabsUseCases,
            useCases.customTabsUseCases,
        )
    }
    val services by lazy { Services(context, backgroundServices.accountManager, useCases.tabsUseCases) }
    val push by lazy { Push(context, analytics.crashReporter) }

    @delegate:SuppressLint("NewApi")
    val autofillConfiguration by lazy {
        AutofillConfiguration(
            storage = core.loginsStorage,
            publicSuffixList = utils.publicSuffixList,
            unlockActivity = AutofillUnlockActivity::class.java,
            confirmActivity = AutofillConfirmActivity::class.java,
            searchActivity = AutofillSearchActivity::class.java,
            applicationName = context.getString(R.string.app_name),
            httpClient = core.client,
        )
    }

    private val notificationManagerCompat = NotificationManagerCompat.from(context)

    val notificationsDelegate: NotificationsDelegate by lazy {
        NotificationsDelegate(
            notificationManagerCompat,
        )
    }
}
