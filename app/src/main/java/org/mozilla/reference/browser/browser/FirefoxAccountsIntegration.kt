/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.request.RequestInterceptor
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.service.fxa.Config
import mozilla.components.service.fxa.FirefoxAccount
import mozilla.components.service.fxa.FxaException
import mozilla.components.service.fxa.Profile
import kotlin.coroutines.experimental.CoroutineContext

class FirefoxAccountsIntegration(
    private val context: Context,
    private val tabsUseCases: TabsUseCases
) : CoroutineScope, LifecycleObserver {

    companion object {
        const val CLIENT_ID = "3c49430b43dfba77"
        const val REDIRECT_URL = "https://accounts.firefox.com/oauth/success/3c49430b43dfba77"
        const val SUCCESS_PATH = "connect_another_device?showSuccessMessage=true"
        const val FXA_STATE_PREFS_KEY = "fxaAppState"
        const val FXA_STATE_KEY = "fxaState"
        val SCOPES: Array<String> = arrayOf("profile")
    }

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var account: Deferred<FirefoxAccount>

    var profile: Profile? = null
        private set

    fun init() {
        job = Job()

        account = async {
            getAuthenticatedAccount()?.let {
                profile = it.getProfile(true).await()
                return@async it
            }
            return@async Config.release().await().use { config ->
                FirefoxAccount(config, CLIENT_ID, REDIRECT_URL)
            }
        }
    }

    fun authenticate() {
        launch {
            val url = account.await().beginOAuthFlow(SCOPES, false).await()
            tabsUseCases.addSession.invoke(url)
        }
    }

    fun logout() {
        profile = null
        getSharedPreferences().edit().putString(FXA_STATE_KEY, "").apply()
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

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        runBlocking { account.await().close() }
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
