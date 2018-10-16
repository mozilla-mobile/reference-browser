/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.request.RequestInterceptor
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.service.fxa.Config
import mozilla.components.service.fxa.FirefoxAccount
import mozilla.components.service.fxa.OAuthInfo
import mozilla.components.service.fxa.Profile

class FirefoxAccountsIntegration(
    private val context: Context,
    private val tabsUseCases: TabsUseCases
) {
    companion object {
        const val CLIENT_ID = "12cc4070a481bc73"
        const val REDIRECT_URL = "fxaclient://android.redirect"

        // TODO get URL from Config object: https://github.com/mozilla/application-services/issues/305
        const val CONFIG_URL = "https://latest.dev.lcip.org"

        // TODO redirect to URL directly: https://github.com/mozilla-mobile/android-components/issues/1106
        const val SUCCESS_PAGE = """
            <head>
                <meta
                    http-equiv="refresh"
                    content="0; URL=$CONFIG_URL/connect_another_device?showSuccessMessage=true" />
            </head>
        """
        const val FXA_STATE_PREFS_KEY = "fxaAppState"
        const val FXA_STATE_KEY = "fxaState"
        val SCOPES: Array<String> = arrayOf("profile")
    }

    @Volatile private var afterInit: ((FirefoxAccount) -> Unit)? = null
    @Volatile private var account: FirefoxAccount? = null
    @Volatile var profile: Profile? = null
        private set

    fun init() {
        // First check if we have a persisted account and are still logged in
        val persistedAccount = getSharedPreferences().getString(FXA_STATE_KEY, "") as String
        if (persistedAccount != "") {
            FirefoxAccount.fromJSONString(persistedAccount).whenComplete { account ->
                this.account = account
                account.getProfile().whenComplete { profile ->
                    this.profile = profile
                }
            }
        } else {
            // Otherwise create a new config and prepare to authenticate
            Config.custom(CONFIG_URL).whenComplete { config ->
                account = FirefoxAccount(config, CLIENT_ID, REDIRECT_URL).apply {
                    afterInit?.invoke(this)
                }
            }
        }
    }

    fun authenticate() {
        val account = account
        if (account != null) {
            authenticate(account)
        } else {
            afterInit = { authenticate(it) }
        }
    }

    private fun authenticate(account: FirefoxAccount) {
        account.beginOAuthFlow(SCOPES, false).whenComplete { url ->
            launch(UI) {
                tabsUseCases.addSession.invoke(url)
            }
        }
    }

    fun logout() {
        account = null
        profile = null
        getSharedPreferences().edit().putString(FXA_STATE_KEY, "").apply()
        init()
    }

    private fun persistProfile(profile: Profile) {
        this.profile = profile
        account?.toJSONString().let {
            getSharedPreferences().edit().putString(FXA_STATE_KEY, it).apply()
        }
    }

    private fun getSharedPreferences(): SharedPreferences {
        return context.getSharedPreferences(FXA_STATE_PREFS_KEY, Context.MODE_PRIVATE)
    }

    val interceptor = object : RequestInterceptor {
        override fun onLoadRequest(session: EngineSession, uri: String): RequestInterceptor.InterceptionResponse? {
            account?.let {
                if (uri.startsWith(REDIRECT_URL)) {
                    val parsedUri = Uri.parse(uri)
                    val code = parsedUri.getQueryParameter("code") as String
                    val state = parsedUri.getQueryParameter("state") as String
                    it.completeOAuthFlow(code, state)
                            .then { _: OAuthInfo -> it.getProfile() }
                            .whenComplete { persistProfile(it) }
                    return RequestInterceptor.InterceptionResponse(SUCCESS_PAGE)
                }
            }
            return null
        }
    }
}
