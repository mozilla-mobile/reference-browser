/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.components

import android.content.Context
import android.os.Build
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.browser.storage.sync.PlacesHistoryStorage
import mozilla.components.concept.sync.AccountObserver
import mozilla.components.concept.sync.DeviceCapability
import mozilla.components.concept.sync.DeviceType
import mozilla.components.concept.sync.OAuthAccount
import mozilla.components.feature.push.AutoPushFeature
import mozilla.components.feature.push.PushConfig
import mozilla.components.feature.sendtab.SendTabFeature
import mozilla.components.service.fxa.DeviceConfig
import mozilla.components.service.fxa.ServerConfig
import mozilla.components.service.fxa.SyncConfig
import mozilla.components.service.fxa.SyncEngine
import mozilla.components.service.fxa.manager.FxaAccountManager
import mozilla.components.service.fxa.sync.GlobalSyncableStoreProvider
import org.mozilla.reference.browser.push.FirebasePush
import org.mozilla.reference.browser.NotificationManager

/**
 * Component group for background services. These are components that need to be accessed from
 * within a background worker.
 */
class BackgroundServices(
    context: Context,
    placesHistoryStorage: PlacesHistoryStorage
) {
    companion object {
        const val CLIENT_ID = "3c49430b43dfba77"
        const val REDIRECT_URL = "https://accounts.firefox.com/oauth/success/$CLIENT_ID"
    }

    init {
        // Make the "history" store accessible to workers spawned by the sync manager.
        GlobalSyncableStoreProvider.configureStore(SyncEngine.History to placesHistoryStorage)
    }

    private val serverConfig = ServerConfig.release(CLIENT_ID, REDIRECT_URL)
    private val deviceConfig = DeviceConfig(
        name = "Reference Browser on " + Build.MANUFACTURER + " " + Build.MODEL,
        type = DeviceType.MOBILE,
        capabilities = setOf(DeviceCapability.SEND_TAB)
    )
    private val syncConfig = SyncConfig(
        supportedEngines = setOf(SyncEngine.History),
        syncPeriodInMinutes = 240L
    ) // four hours

    val accountManager by lazy {
        FxaAccountManager(
            context,
            serverConfig,
            deviceConfig,
            syncConfig,
            // We don't need to specify this explicitly, but `syncConfig` may be disabled due to an 'experiments'
            // flag. In that case, sync scope necessary for syncing won't be acquired during authentication
            // unless we explicitly specify it below.
            // This is a good example of an information leak at the API level.
            // See https://github.com/mozilla-mobile/android-components/issues/3732
            setOf("https://identity.mozilla.com/apps/oldsync")
        ).also {
            // We don't need the push service unless we're signed in.
            it.register(pushServiceObserver, ProcessLifecycleOwner.get(), false)

            // Initializing the feature allows it to start observing events as needed.
            SendTabFeature(it, pushFeature) { device, tabs ->
                notificationManager.showReceivedTabs(device, tabs)
            }

            CoroutineScope(Dispatchers.Main).launch { it.initAsync().await() }
        }
    }

    val pushFeature by lazy {
        pushConfig?.let { config ->
            AutoPushFeature(context, pushService, config)
        }
    }

    /**
     * The push configuration data class used to initialize the AutoPushFeature.
     *
     * If we have the `project_id` resource, then we know that the Firebase configuration and API
     * keys are available for the FCM service to be used.
     */
    private val pushConfig by lazy {
        val resId = context.resources.getIdentifier("project_id", "string", context.packageName)
        if (resId == 0) {
            return@lazy null
        }
        val projectId = context.resources.getString(resId)
        PushConfig(projectId)
    }

    private val pushService by lazy { FirebasePush() }

    private val notificationManager by lazy {
        NotificationManager(context)
    }

    private val pushServiceObserver by lazy {
        object : AccountObserver {
            override fun onAuthenticated(account: OAuthAccount, newAccount: Boolean) {
                pushService.start(context)
            }

            override fun onLoggedOut() {
                pushService.stop()
            }
        }
    }
}
