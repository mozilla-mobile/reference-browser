/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.components

import android.content.Context
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.appservices.fxaclient.Config.Server
import mozilla.components.browser.storage.sync.PlacesHistoryStorage
import mozilla.components.browser.storage.sync.RemoteTabsStorage
import mozilla.components.concept.sync.DeviceCapability
import mozilla.components.concept.sync.DeviceType
import mozilla.components.feature.accounts.push.FxaPushSupportFeature
import mozilla.components.feature.accounts.push.SendTabFeature
import mozilla.components.feature.syncedtabs.SyncedTabsFeature
import mozilla.components.service.fxa.DeviceConfig
import mozilla.components.service.fxa.ServerConfig
import mozilla.components.service.fxa.SyncConfig
import mozilla.components.service.fxa.SyncEngine
import mozilla.components.service.fxa.manager.FxaAccountManager
import mozilla.components.service.fxa.sync.GlobalSyncableStoreProvider
import org.mozilla.reference.browser.NotificationManager
import org.mozilla.reference.browser.ext.components
import org.mozilla.reference.browser.tabs.SyncedTabsIntegration

/**
 * Component group for background services. These are components that need to be accessed from
 * within a background worker.
 */
class BackgroundServices(
    context: Context,
    push: Push,
    placesHistoryStorage: PlacesHistoryStorage,
    private val remoteTabsStorage: RemoteTabsStorage = RemoteTabsStorage()
) {
    companion object {
        const val CLIENT_ID = "3c49430b43dfba77"
        const val REDIRECT_URL = "https://accounts.firefox.com/oauth/success/$CLIENT_ID"
    }

    init {
        // Make the sync stores accessible to workers spawned by the sync manager.
        GlobalSyncableStoreProvider.configureStore(SyncEngine.History to placesHistoryStorage)
        GlobalSyncableStoreProvider.configureStore(SyncEngine.Tabs to remoteTabsStorage)
    }

    private val serverConfig = ServerConfig(Server.RELEASE, CLIENT_ID, REDIRECT_URL)
    private val deviceConfig = DeviceConfig(
        name = "Reference Browser on " + Build.MANUFACTURER + " " + Build.MODEL,
        type = DeviceType.MOBILE,
        capabilities = setOf(DeviceCapability.SEND_TAB)
    )
    private val syncConfig = SyncConfig(
        supportedEngines = setOf(SyncEngine.History, SyncEngine.Tabs),
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
        ).also { accountManager ->

            SendTabFeature(accountManager) { device, tabs ->
                NotificationManager.showReceivedTabs(context, device, tabs)
            }

            push.feature?.let { push -> FxaPushSupportFeature(context, accountManager, push) }

            SyncedTabsIntegration(context, accountManager).also {
                it.launch()
            }

            CoroutineScope(Dispatchers.Main).launch { accountManager.initAsync().await() }
        }
    }

    val syncedTabs by lazy { SyncedTabsFeature(accountManager, context.components.core.store, remoteTabsStorage) }
}
