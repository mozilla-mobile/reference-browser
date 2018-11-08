/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.launch
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.item.BrowserMenuItemToolbar
import mozilla.components.browser.menu.item.SimpleBrowserMenuCheckbox
import mozilla.components.browser.menu.item.SimpleBrowserMenuItem
import mozilla.components.browser.search.SearchEngineManager
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.session.storage.DefaultSessionStorage
import mozilla.components.concept.engine.DefaultSettings
import mozilla.components.concept.engine.Engine
import mozilla.components.feature.intent.IntentProcessor
import mozilla.components.feature.search.SearchUseCases
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.tabs.TabsUseCases
import org.mozilla.reference.browser.browser.FirefoxAccountsIntegration
import org.mozilla.reference.browser.ext.share
import org.mozilla.reference.browser.settings.SettingsActivity

class Components(
    private val applicationContext: Context
) {

    // Engine
    val engine: Engine by lazy {
        val defaultSettings = DefaultSettings(
            requestInterceptor = AppRequestInterceptor(applicationContext)
        )
        EngineProvider.getEngine(applicationContext, defaultSettings)
    }

    // Session
    val sessionStorage by lazy { DefaultSessionStorage(applicationContext) }

    val sessionManager by lazy {
        SessionManager(engine, defaultSession = { Session("about:blank") }).apply {
            sessionStorage.read(engine)?.let {
                restore(it)
            }

            if (size == 0) {
                val initialSession = Session("https://www.mozilla.org")
                add(initialSession)
            }
        }
    }

    val sessionUseCases by lazy { SessionUseCases(sessionManager) }

    // Search
    private val searchEngineManager by lazy {
        SearchEngineManager().apply {
            CoroutineScope(Dispatchers.Default).launch {
                load(applicationContext).await()
            }
        }
    }
    private val searchUseCases by lazy { SearchUseCases(applicationContext, searchEngineManager, sessionManager) }
    val defaultSearchUseCase by lazy { { searchTerms: String -> searchUseCases.defaultSearch.invoke(searchTerms) } }

    // Intent
    val sessionIntentProcessor by lazy { IntentProcessor(sessionUseCases, sessionManager, searchUseCases) }

    // Menu
    val menuBuilder by lazy { BrowserMenuBuilder(menuItems) }

    private val menuItems by lazy {
        listOf(
                menuToolbar,
                SimpleBrowserMenuItem("Share") {
                    val url = sessionManager.selectedSession?.url ?: ""
                    applicationContext.share(url)
                },
                SimpleBrowserMenuItem("Settings") {
                    openSettingsActivity()
                },
                SimpleBrowserMenuItem("Clear Data") {
                    sessionUseCases.clearData.invoke()
                },
                SimpleBrowserMenuCheckbox("Request desktop site") { checked ->
                    sessionUseCases.requestDesktopSite.invoke(checked)
                }
        )
    }

    private fun openSettingsActivity() {
        val intent = Intent(applicationContext, SettingsActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        applicationContext.startActivity(intent)
    }

    private val menuToolbar by lazy {
        val forward = BrowserMenuItemToolbar.Button(
                mozilla.components.ui.icons.R.drawable.mozac_ic_forward,
                iconTintColorResource = R.color.photonBlue90,
                contentDescription = "Forward") {
            sessionUseCases.goForward.invoke()
        }

        val refresh = BrowserMenuItemToolbar.Button(
                mozilla.components.ui.icons.R.drawable.mozac_ic_refresh,
                iconTintColorResource = R.color.photonBlue90,
                contentDescription = "Refresh") {
            sessionUseCases.reload.invoke()
        }

        val stop = BrowserMenuItemToolbar.Button(
                mozilla.components.ui.icons.R.drawable.mozac_ic_stop,
                iconTintColorResource = R.color.photonBlue90,
                contentDescription = "Stop") {
            sessionUseCases.stopLoading.invoke()
        }

        BrowserMenuItemToolbar(listOf(forward, refresh, stop))
    }

    // Tabs
    val tabsUseCases: TabsUseCases by lazy { TabsUseCases(sessionManager) }

    // Firefox Accounts
    val firefoxAccountsIntegration: FirefoxAccountsIntegration by lazy {
        FirefoxAccountsIntegration(applicationContext, tabsUseCases).apply {
            init()
        }
    }
}
