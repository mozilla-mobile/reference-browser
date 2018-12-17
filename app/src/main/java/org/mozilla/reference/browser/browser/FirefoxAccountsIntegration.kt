/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import mozilla.components.browser.storage.sync.SyncAuthInfo
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.request.RequestInterceptor
import mozilla.components.feature.sync.FirefoxSyncFeature
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.service.fxa.Config
import mozilla.components.service.fxa.FirefoxAccount
import mozilla.components.service.fxa.FxaException
import mozilla.components.service.fxa.Profile
import mozilla.components.support.base.log.Log
import kotlin.coroutines.CoroutineContext

@Suppress("TooManyFunctions")
class FirefoxAccountsIntegration(
    private val context: Context,
    private val tabsUseCases: TabsUseCases,
    private val firefoxSyncFeature: FirefoxSyncFeature<SyncAuthInfo>
) : CoroutineScope, LifecycleObserver {

    companion object {
        const val CLIENT_ID = "3c49430b43dfba77"
        const val REDIRECT_URL = "https://accounts.firefox.com/oauth/success/3c49430b43dfba77"
        const val SUCCESS_PATH = "connect_another_device?showSuccessMessage=true"
        const val FXA_STATE_PREFS_KEY = "fxaAppState"
        const val FXA_STATE_KEY = "fxaState"
        const val FXA_LAST_SYNCED_KEY = "lastSyncedAt"
        const val FXA_NEVER_SYNCED_TS: Long = 0
        val CONFIG = Config.release(CLIENT_ID, REDIRECT_URL)

        // This is slightly messy - here we need to know the union of all "scopes"
        // needed by components which rely on FxA integration. If this list
        // grows too far we probably want to find a way to determine the set
        // at runtime.
        val SCOPES: Array<String> = arrayOf("profile", "https://identity.mozilla.com/apps/oldsync")
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        Log.log(
            Log.Priority.ERROR,
            message = "Unexpected error occurred during Firefox Account authentication",
            throwable = e,
            tag = "Reference-Browser")
    }

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job + exceptionHandler

    lateinit var account: FirefoxAccount

    @Volatile var profile: Profile? = null
        private set

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun init() {
        job = Job()
        account = initAccount()
    }

    private fun initAccount(): FirefoxAccount {
        val authenticatedAccount = restoreAuthenticatedAccount()
        return if (authenticatedAccount != null) {
            launch {
                try {
                    // Ideally, we shouldn't need to fetch the profile for accounts as
                    // an indicator of successful authentication.
                    // See: https://github.com/mozilla/application-services/issues/483
                    // https://github.com/mozilla/application-services/issues/483
                    profile = account.getProfile(true).await()
                } catch (e: FxaException) {
                    Log.log(
                        Log.Priority.ERROR,
                        message = "Failed to get profile for authenticated account",
                        throwable = e,
                        tag = "Reference-Browser")
                }
            }
            authenticatedAccount
        } else {
            FirefoxAccount(CONFIG)
        }
    }

    fun authenticate() {
        launch {
            if (restoreAuthenticatedAccount() != null) {
                // If the user is authenticated but we failed to fetch the profile we try again
                // and make sure an error is displayed if the FxA endpoint can't be reached.
                if (profile == null) {
                    account = initAccount()
                    tabsUseCases.addTab.invoke(CONFIG.contentUrl)
                }
            } else {
                try {
                    val url = account.beginOAuthFlow(SCOPES, true).await()
                    tabsUseCases.addTab.invoke(url)
                } catch (e: FxaException) {
                    // TODO we need a specific FxA exception for network errors:
                    // https://github.com/mozilla/application-services/issues/479

                    // Instead of just a log statement and no error indicator for the user, this
                    // will give us a new tab pointing to the FxA server and showing "Unable to connect"
                    // or similar, depending on the current network state.
                    tabsUseCases.addTab.invoke(CONFIG.contentUrl)
                    account = initAccount()
                }
            }
        }
    }

    suspend fun syncNow() {
        firefoxSyncFeature.sync(account)
        setLastSynced(System.currentTimeMillis())
    }

    fun logout() {
        profile = null
        getSharedPreferences()
                .edit()
                .remove(FXA_STATE_KEY)
                .remove(FXA_LAST_SYNCED_KEY)
                .apply()
        init()
    }

    fun getLastSynced(): Long {
        return getSharedPreferences().getLong(FXA_LAST_SYNCED_KEY, FXA_NEVER_SYNCED_TS)
    }

    fun setLastSynced(ts: Long) {
        getSharedPreferences().edit().putLong(FXA_LAST_SYNCED_KEY, ts).apply()
    }

    private fun persistAuthenticatedAccount() {
        account.toJSONString().let {
            getSharedPreferences().edit().putString(FXA_STATE_KEY, it).apply()
        }
    }

    private fun restoreAuthenticatedAccount(): FirefoxAccount? {
        val savedJSON = getSharedPreferences().getString(FXA_STATE_KEY, null)
        return savedJSON?.let {
            try {
                FirefoxAccount.fromJSONString(it)
            } catch (e: FxaException) {
                null
            }
        }
    }

    private fun getSharedPreferences(): SharedPreferences {
        return context.getSharedPreferences(FXA_STATE_PREFS_KEY, Context.MODE_PRIVATE)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        try {
            account.close()
        } catch (e: FxaException) {
            Log.log(
                    Log.Priority.DEBUG,
                    message = "Error occurred when closing Firefox Account",
                    throwable = e,
                    tag = "Reference-Browser"
            )
        }

        job.cancel()
    }

    val interceptor = object : RequestInterceptor {
        override fun onLoadRequest(session: EngineSession, uri: String): RequestInterceptor.InterceptionResponse? {
            if (uri.startsWith(REDIRECT_URL)) {
                val parsedUri = Uri.parse(uri)
                val code = parsedUri.getQueryParameter("code") as String
                val state = parsedUri.getQueryParameter("state") as String
                launch {
                    account.completeOAuthFlow(code, state).await()
                    this@FirefoxAccountsIntegration.profile = account.getProfile().await()
                    persistAuthenticatedAccount()

                    // Now that we're logged in, kick off initial sync.
                    CoroutineScope(Dispatchers.IO).launch { syncNow() }
                }
                // TODO this can be simplified once https://github.com/mozilla/application-services/issues/305 lands
                val successUrl = "${parsedUri.scheme}://${parsedUri.host}/$SUCCESS_PATH"
                return RequestInterceptor.InterceptionResponse.Url(successUrl)
            }
            return null
        }
    }
}
