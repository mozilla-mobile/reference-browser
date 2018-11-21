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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.request.RequestInterceptor
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.service.fxa.Config
import mozilla.components.service.fxa.FirefoxAccount
import mozilla.components.service.fxa.FxaException
import mozilla.components.service.fxa.Profile
import mozilla.components.service.fxa.OAuthInfo
import kotlin.coroutines.CoroutineContext
import org.mozilla.places.SyncAuthInfo
import org.mozilla.reference.browser.ext.application

class FirefoxAccountsIntegration(
    private val context: Context,
    private val tabsUseCases: TabsUseCases
) : CoroutineScope, LifecycleObserver {

    companion object {
        // This is the production FxA server - we take a special step if
        // FXA_SERVER isn't set to this.
        const val FXA_PRODUCTION_SERVER = "https://accounts.firefox.com"

        // This is the FxA server we wish to hit. Note that eventually this
        // needs to be able to be configured by the user somehow so they
        // can self-host - but for now we keep it as a hard-coded constant.

        // XXX - note that production isn't yet configured for this app.
        // const val FXA_SERVER = "https://accounts.firefox.com"
        const val FXA_SERVER = "https://latest.dev.lcip.org"

        // The client ID of the reference browser.
        const val CLIENT_ID = "3c49430b43dfba77"
        const val REDIRECT_URL = "$FXA_SERVER/oauth/success/$CLIENT_ID"
        const val SUCCESS_PATH = "connect_another_device?showSuccessMessage=true"
        const val FXA_STATE_PREFS_KEY = "fxaAppState"
        const val FXA_STATE_KEY = "fxaState"
        // This is slighly messy - here we need to know the union of all "scopes"
        // needed by components which rely on FxA integration. If this list
        // grows too far we probably want to find a way to determine the set
        // at runtime.
        val SCOPES: Array<String> = arrayOf("profile", "https://identity.mozilla.com/apps/oldsync")

        const val FXA_LAST_SYNCED_AT_KEY = "lastSyncedAt"
    }

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var account: Deferred<FirefoxAccount>

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
            val initConfig = if (FXA_SERVER == FXA_PRODUCTION_SERVER) {
                Config.release();
            } else {
                Config.custom(FXA_SERVER);
            }
            return@async initConfig.await().use { config ->
                FirefoxAccount(config, CLIENT_ID, REDIRECT_URL)
            }
        }
    }

    fun authenticate() {
        launch {
            val url = account.await().beginOAuthFlow(SCOPES, true).await()
            tabsUseCases.addSession.invoke(url)
        }
    }

    fun logout() {
        profile = null
        val prefs = getSharedPreferences().edit()
        prefs.putString(FXA_STATE_KEY, "")
        prefs.putLong(FXA_LAST_SYNCED_AT_KEY, 0)
        prefs.apply()
        init()
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
        val savedJSON = getSharedPreferences().getString(FXA_STATE_KEY, "")
        return savedJSON?.let {
            try {
                FirefoxAccount.fromJSONString(it)
            } catch (e: FxaException) {
                null
            }
        }
    }

    fun syncNow(): Deferred<Unit> {
        return async {
            val tokenServerURL = account.await().getTokenServerEndpointURL()
            val token = account.await().getCachedOAuthToken(SCOPES).await()
                    ?: throw RuntimeException("can't get a token!")

            val keys = token.keys ?: throw RuntimeException("keys are missing!")
            val keyInfo = keys["https://identity.mozilla.com/apps/oldsync"]
                    ?: throw RuntimeException("Key info is missing!")

            val sai = SyncAuthInfo(
                    kid = keyInfo.kid,
                    fxaAccessToken = token.accessToken,
                    syncKey = keyInfo.k,
                    tokenserverURL = tokenServerURL)
            val status = context.application.components.placesSync.sync(sai).await();
            if (status == context.application.components.placesSync.COMPLETED_OK) {
                val now = System.currentTimeMillis()
                getSharedPreferences().edit().putLong(FXA_LAST_SYNCED_AT_KEY, now).apply()
            }
        }
    }

    fun getLastSync(): Long {
        return getSharedPreferences().getLong(FXA_LAST_SYNCED_AT_KEY, 0)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        if (account.isCompleted) {
            account.getCompleted().close()
        } else {
            account.cancel()
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
                    val account = account.await()
                    account.completeOAuthFlow(code, state).await()
                    val profile = account.getProfile().await()
                    persistProfile(profile)
                }
                // TODO this can be simplified once https://github.com/mozilla/application-services/issues/305 lands
                val successUrl = "${parsedUri.scheme}://${parsedUri.host}/$SUCCESS_PATH"
                return RequestInterceptor.InterceptionResponse.Url(successUrl)
            }
            return null
        }
    }
}
