/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.services

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import mozilla.components.browser.storage.sync.PlacesHistoryStorage
import mozilla.components.browser.storage.sync.SyncAuthInfo
import mozilla.components.concept.storage.SyncError
import mozilla.components.concept.storage.SyncOk
import mozilla.components.feature.sync.FirefoxSyncFeature
import mozilla.components.feature.sync.SyncResult
import mozilla.components.feature.sync.SyncStatusObserver
import mozilla.components.service.fxa.AccountObserver
import mozilla.components.service.fxa.FirefoxAccountShaped
import mozilla.components.service.fxa.FxaAccountManager
import mozilla.components.service.fxa.FxaUnauthorizedException
import mozilla.components.service.fxa.Profile
import mozilla.components.support.base.observer.Observable
import mozilla.components.support.base.observer.ObserverRegistry
import org.mozilla.reference.browser.ext.components
import java.lang.Exception
import java.util.concurrent.TimeUnit

private enum class SyncWorkerTag {
    Common,
    Immediate,
    Periodic
}

private enum class SyncWorkerName {
    Periodic,
    Immediate
}

private const val SYNC_STATE_PREFS_KEY = "syncPrefs"
private const val SYNC_LAST_SYNCED_KEY = "lastSynced"

private const val SYNC_STAGGER_BUFFER_MS = 10 * 60 * 1000L // 10 minutes.
private const val SYNC_STARTUP_DELAY_MS = 5 * 1000L // 5 seconds.
private const val SYNC_PERIODIC_START_DELAY_MS = SYNC_STAGGER_BUFFER_MS

fun getLastSynced(context: Context): Long {
    return context
        .getSharedPreferences(SYNC_STATE_PREFS_KEY, Context.MODE_PRIVATE)
        .getLong(SYNC_LAST_SYNCED_KEY, 0)
}

fun setLastSynced(context: Context, ts: Long) {
    context
        .getSharedPreferences(SYNC_STATE_PREFS_KEY, Context.MODE_PRIVATE)
        .edit()
        .putLong(SYNC_LAST_SYNCED_KEY, ts)
        .apply()
}

/**
 * An incubating manager of interactions with an underlying sync implementation.
 * Once wrinkles are ironed out, this should be lifted into the 'android-components' project.
 */
class SyncManager(
    val context: Context,
    val accountManager: FxaAccountManager,
    val storage: PlacesHistoryStorage
) : Observable<SyncStatusObserver> by ObserverRegistry() {
    companion object {
        // Periodically sync in the background, to make our syncs a little more incremental.
        // This isn't strictly necessary, and could be considered an optimization.
        //
        // Assuming that we synchronize during app startup, our trade-offs are:
        // - not syncing in the background at all might mean longer syncs, more arduous startup syncs
        // - on a slow mobile network, these delays could be significant
        // - a delay during startup sync may affect user experience, since our data will be stale
        // for longer
        // - however, background syncing eats up some of the device resources
        // - ... so we only do so a few times per day
        // - we also rely on the OS and the WorkManager APIs to minimize those costs. It promises to
        // bundle together tasks from different applications that have similar resource-consumption
        // profiles. Specifically, we need device radio to be ON to talk to our servers; OS will run
        // our periodic syncs bundled with another tasks that also need radio to be ON, thus "spreading"
        // the overall cost.
        //
        // If we wanted to be very fancy, this period could be driven by how much new activity an
        // account is actually expected to generate. For now, it's just a hard-coded constant.
        const val SYNC_PERIOD = 4L
        val SYNC_PERIOD_UNIT = TimeUnit.HOURS
    }

    private val syncImpl = FirefoxSyncFeature(mapOf("history" to storage)) {
        SyncAuthInfo(it.kid, it.fxaAccessToken, it.syncKey, it.tokenServerUrl)
    }
    private val syncWorkInfoList = WorkManager.getInstance().getWorkInfosByTagLiveData(SyncWorkerTag.Common.name)

    init {
        // If we're authenticated, let's start syncing periodically.
        // Delay start of periodic syncing a little bit, so that it doesn't begin at the same time
        // as our startup syncing.
        if (accountManager.authenticatedAccount() != null) {
            syncNow(startup = true)

            WorkManager.getInstance().beginWith(
                OneTimeWorkRequestBuilder<PeriodicSyncKickoffWorker>()
                .setInitialDelay(SYNC_PERIODIC_START_DELAY_MS, TimeUnit.MILLISECONDS)
                .build()
            ).enqueue()
        }

        // Monitor account state for authentication changes.
        accountManager.register(object : AccountObserver {
            override fun onAuthenticated(account: FirefoxAccountShaped) {
                startPeriodicSync()
            }

            override fun onLoggedOut() {
                stopPeriodicSync()
            }

            override fun onError(error: Exception) {
                if (error is FxaUnauthorizedException) {
                    stopPeriodicSync()
                }
            }

            override fun onProfileUpdated(profile: Profile) { }
        })
    }

    fun isSyncRunning(): Boolean {
        syncWorkInfoList.value?.let { workers ->
            return workers.any { it.state == WorkInfo.State.RUNNING }
        }
        return false
    }

    /**
     * Kick-off an immediate sync.
     *
     * @param startup Boolean flag indicating if we're in a startup situation.
     */
    fun syncNow(startup: Boolean = false) {
        val delayMs = if (startup) {
            // Startup delay is there to avoid SQLITE_BUSY crashes, since we currently do a poor job
            // of managing database connections, and we expect there to be database writes at the start.
            // FIXME https://github.com/mozilla-mobile/android-components/issues/1369
            SYNC_STARTUP_DELAY_MS
        } else {
            0L
        }
        WorkManager.getInstance().beginUniqueWork(
                SyncWorkerName.Immediate.name,
                // Use the 'keep' policy to minimize overhead from multiple "sync now" operations coming in
                // at the same time.
                ExistingWorkPolicy.KEEP,
                immediateSyncWorkRequest(delayMs)
        ).enqueue()
    }

    /**
     * Periodic background syncing is mainly intended to reduce workload when we sync during
     * application startup.
     */
    private fun startPeriodicSync() {
        // Use the 'replace' policy as a simple way to upgrade periodic worker configurations across
        // application versions. We do this instead of versioning workers.
        WorkManager.getInstance().enqueueUniquePeriodicWork(
                SyncWorkerName.Periodic.name,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicSyncWorkRequest()
        )
    }

    private fun stopPeriodicSync() {
        WorkManager.getInstance().cancelUniqueWork(SyncWorkerName.Periodic.name)
    }

    private fun periodicSyncWorkRequest(): PeriodicWorkRequest {
        // Periodic interval must be at least PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
        // e.g. not more frequently than 15 minutes.
        return PeriodicWorkRequestBuilder<SyncWorker>(SYNC_PERIOD, SYNC_PERIOD_UNIT)
                .addTag(SyncWorkerTag.Common.name)
                .addTag(SyncWorkerTag.Periodic.name)
                .build()
    }

    private fun immediateSyncWorkRequest(delayMs: Long = 0L): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                        Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                )
                .addTag(SyncWorkerTag.Common.name)
                .addTag(SyncWorkerTag.Immediate.name)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .build()
    }

    private suspend fun doSync(account: FirefoxAccountShaped): SyncResult {
        return syncImpl.sync(account)
    }

    // We can't set an initial delay on periodic tasks. Since we want to delay the start of periodic
    // syncing, we wrap that kick-off process in a one-off, delayed task.
    private class PeriodicSyncKickoffWorker(
        private val context: Context,
        params: WorkerParameters
    ) : Worker(context, params) {
        override fun doWork(): Result {
            context.components.backgroundServices.syncManager.startPeriodicSync()
            return Result.success()
        }
    }

    private class SyncWorker(
        private val context: Context,
        private val params: WorkerParameters
    ) : CoroutineWorker(context, params) {
        private val logTag = "SyncWorker"

        @Suppress("ReturnCount", "ComplexMethod")
        override suspend fun doWork(): Result {
            val syncManager = context.components.backgroundServices.syncManager

            Log.d(logTag, "Starting sync... Tagged as: ${params.tags}")

            // If this is a periodic sync task, and we've very recently synced successfully, skip it.
            // There could have been a manual or heuristic-driven syc very recently, and it's unlikely
            // that an additional sync so shortly afterward will be valuable.
            // NB: this does not affect manually triggered syncing in any way.
            val lastSyncedTs = getLastSynced(context)
            if (params.tags.contains(SyncWorkerTag.Periodic.name) &&
                lastSyncedTs != 0L &&
                (System.currentTimeMillis() - lastSyncedTs) < SYNC_STAGGER_BUFFER_MS
            ) {
                return Result.success()
            }

            // We need an account to sync. Make sure that it's present.
            val account = context.components.backgroundServices.syncManager.accountManager.authenticatedAccount()
            if (account == null) {
                Log.w(logTag, "Account is absent. Can't sync, declaring failure.")
                return Result.failure()
            }

            // If another sync is already in progress, bail out. For example, we might have received
            // a request for an immediate sync while periodic sync was already running.
            // Overlaps like are possible in theory, but should be quite rare in practice.
            // NB: feature-sync maintains its own mutex lock on actual sync operations.
            if (syncManager.isSyncRunning()) {
                Log.w(logTag, "Sync is already running. Requesting retry.")
                return Result.retry()
            }

            syncManager.notifyObservers { onStarted() }

            val getResult: suspend () -> Pair<Result, Exception?> = f@{
                val syncResult = syncManager.doSync(account)

                if (!syncResult.contains("history")) {
                    Log.e(logTag, "Expected to synchronize history, but did not.")
                    return@f Result.failure() to null
                }

                val historySyncStatus = syncResult["history"]!!.status
                return@f when (historySyncStatus) {
                    SyncOk -> Result.success() to null
                    is SyncError -> {
                        Log.e(logTag, "History sync encountered an error: ", historySyncStatus.exception)
                        Result.failure() to historySyncStatus.exception
                    }
                }
            }

            val result = getResult()
            when (result.first) {
                is Result.Success -> {
                    setLastSynced(context, System.currentTimeMillis())
                    syncManager.notifyObservers { onIdle() }
                }
                is Result.Retry -> {
                    syncManager.notifyObservers { onIdle() }
                }
                is Result.Failure -> {
                    syncManager.notifyObservers { onError(result.second) }
                }
            }

            return result.first
        }
    }
}
