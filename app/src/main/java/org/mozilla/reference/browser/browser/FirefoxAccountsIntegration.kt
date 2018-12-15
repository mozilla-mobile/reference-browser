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
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
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

        // This is slightly messy - here we need to know the union of all "scopes"
        // needed by components which rely on FxA integration. If this list
        // grows too far we probably want to find a way to determine the set
        // at runtime.
        val SCOPES: Array<String> = arrayOf("profile", "https://identity.mozilla.com/apps/oldsync")
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, e ->
        Log.log(
            Log.Priority.ERROR,
            message = "Error occurred during Firefox Account authentication",
            throwable = e,
            tag = "Reference-Browser")
    }

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job + exceptionHandler

    lateinit var account: Deferred<FirefoxAccount>

    var profile: Profile? = null
        private set

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun init() {
        job = Job()

        account = async {
            getAuthenticatedAccount()?.let {
                profile = it.getProfile(true).await()
                return@async it
            }

            FirefoxAccount(Config.release(CLIENT_ID, REDIRECT_URL))
        }
    }

    fun authenticate() {
        launch {
            val url = account.await().beginOAuthFlow(SCOPES, true).await()
            tabsUseCases.addTab.invoke(url)
        }
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

    private fun persistProfile(profile: Profile) {
        this.profile = profile
        launch {
            account.await().toJSONString().let {
                getSharedPreferences().edit().putString(FXA_STATE_KEY, it).apply()
            }
        }
    }

    private fun getSharedPreferences(): SharedPreferences {
        return context.getSharedPreferences(FXA_STATE_PREFS_KEY, Context.MODE_PRIVATE)
    }

    private fun getAuthenticatedAccount(): FirefoxAccount? {
        val savedJSON = getSharedPreferences().getString(FXA_STATE_KEY, null)
        return savedJSON?.let {
            try {
                FirefoxAccount.fromJSONString(it)
            } catch (e: FxaException) {
                null
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        if (account.isCompleted) {
            try {
                account.getCompleted().close()
            } catch (e: FxaException) {
                Log.log(
                    Log.Priority.DEBUG,
                    message = "Error occurred when closing Firefox Account",
                    throwable = e,
                    tag = "Reference-Browser"
                )
            }
        } else {
            account.cancel()
        }

        job.cancel()
    }

    suspend fun syncNow() {
        firefoxSyncFeature.sync(
            account.await()
        )

        setLastSynced(System.currentTimeMillis())
    }

    val interceptor = object : RequestInterceptor {
        override fun onLoadRequest(session: EngineSession, uri: String): RequestInterceptor.InterceptionResponse? {
            if (uri.startsWith(REDIRECT_URL)) {
                val parsedUri = Uri.parse(uri)
                val code = parsedUri.getQueryParameter("code") as String
                val state = parsedUri.getQueryParameter("state") as String
                launch {
                    val account = account.await()
                    account.completeOAuthFlow(code, state).await()
                    val profile = account.getProfile().await()
                    persistProfile(profile)

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
