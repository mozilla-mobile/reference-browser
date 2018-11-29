/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.app.PendingIntent
import android.app.PendingIntent.getBroadcast
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager.getDefaultSharedPreferences
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mozilla.components.browser.domains.ShippedDomainsProvider
import mozilla.components.browser.menu.BrowserMenuBuilder
import mozilla.components.browser.menu.item.BrowserMenuItemToolbar
import mozilla.components.browser.menu.item.SimpleBrowserMenuCheckbox
import mozilla.components.browser.menu.item.SimpleBrowserMenuItem
import mozilla.components.browser.search.SearchEngineManager
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.session.storage.SessionStorage
import mozilla.components.browser.storage.sync.PlacesHistoryStorage
import mozilla.components.concept.engine.DefaultSettings
import mozilla.components.concept.engine.Engine
import mozilla.components.feature.intent.IntentProcessor
import mozilla.components.feature.search.SearchUseCases
import mozilla.components.feature.session.SessionUseCases
import mozilla.components.feature.tabs.TabsUseCases
import mozilla.components.lib.crash.CrashReporter
import mozilla.components.lib.crash.service.MozillaSocorroService
import mozilla.components.lib.crash.service.SentryService
import org.mozilla.reference.browser.BrowserApplication.Companion.NON_FATAL_CRASH_BROADCAST
import org.mozilla.reference.browser.BuildConfig.SENTRY_TOKEN
import org.mozilla.reference.browser.R.string.pref_key_remote_debugging
import org.mozilla.reference.browser.browser.FirefoxAccountsIntegration
import org.mozilla.reference.browser.ext.getPreferenceKey
import org.mozilla.reference.browser.ext.share
import org.mozilla.reference.browser.settings.SettingsActivity
import java.util.concurrent.TimeUnit

class Components(
    private val applicationContext: Context
) {

    // Engine
    val engine: Engine by lazy {
        val defaultSettings = DefaultSettings(
            requestInterceptor = AppRequestInterceptor(applicationContext),
            remoteDebuggingEnabled = getDefaultSharedPreferences(applicationContext)
                    .getBoolean(applicationContext.getPreferenceKey(pref_key_remote_debugging), false)
        )
        EngineProvider.getEngine(applicationContext, defaultSettings)
    }

    // Session
    val sessionStorage by lazy { SessionStorage(applicationContext, engine) }

    val sessionManager by lazy {
        SessionManager(engine, defaultSession = { Session("about:blank") }).apply {
            sessionStorage.restore()?.let { snapshot -> restore(snapshot) }

            if (size == 0) {
                val initialSession = Session("https://www.mozilla.org")
                add(initialSession)
            }

            sessionStorage.autoSave(this)
                .periodicallyInForeground(interval = 30, unit = TimeUnit.SECONDS)
                .whenGoingToBackground()
                .whenSessionsChange()
        }
    }

    val sessionUseCases by lazy { SessionUseCases(sessionManager) }

    // Places.
    val placesHistoryStorage by lazy { PlacesHistoryStorage(applicationContext) }

    // Search
    val searchEngineManager by lazy {
        SearchEngineManager().apply {
            GlobalScope.launch {
                load(applicationContext).await()
            }
        }
    }
    val searchUseCases by lazy { SearchUseCases(applicationContext, searchEngineManager, sessionManager) }
    val defaultSearchUseCase by lazy { { searchTerms: String -> searchUseCases.defaultSearch.invoke(searchTerms) } }

    // Intent
    val sessionIntentProcessor by lazy { IntentProcessor(sessionUseCases, sessionManager, searchUseCases) }

    // Menu
    val menuBuilder by lazy { BrowserMenuBuilder(menuItems) }

    val shippedDomainsProvider by lazy {
        ShippedDomainsProvider().also { it.initialize(applicationContext) }
    }

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
                iconTintColorResource = R.color.icons,
                contentDescription = "Forward") {
            sessionUseCases.goForward.invoke()
        }

        val refresh = BrowserMenuItemToolbar.Button(
                mozilla.components.ui.icons.R.drawable.mozac_ic_refresh,
                iconTintColorResource = R.color.icons,
                contentDescription = "Refresh") {
            sessionUseCases.reload.invoke()
        }

        val stop = BrowserMenuItemToolbar.Button(
                mozilla.components.ui.icons.R.drawable.mozac_ic_stop,
                iconTintColorResource = R.color.icons,
                contentDescription = "Stop") {
            sessionUseCases.stopLoading.invoke()
        }

        BrowserMenuItemToolbar(listOf(forward, refresh, stop))
    }

    // Tabs
    val tabsUseCases: TabsUseCases by lazy { TabsUseCases(sessionManager) }

    // TODO work around until we have https://github.com/mozilla-mobile/android-components/issues/1457
    val removeSessions = {
        sessionManager.all.filter { session ->
            !session.private
        }.forEach { session ->
            sessionManager.remove(session)
        }
    }

    val removePrivateSessions = {
        sessionManager.all.filter { session ->
            session.private
        }.forEach { session ->
            sessionManager.remove(session)
        }
    }

    // Firefox Accounts
    val firefoxAccountsIntegration: FirefoxAccountsIntegration by lazy {
        FirefoxAccountsIntegration(applicationContext, tabsUseCases)
    }

    val crashReporter: CrashReporter by lazy {

        val sentryService = SentryService(
            applicationContext,
            SENTRY_TOKEN,
            sendEventForNativeCrashes = true
        )

        val socorroService = MozillaSocorroService(applicationContext, "ReferenceBrowser")

        CrashReporter(
            services = listOf(sentryService, socorroService),
            shouldPrompt = CrashReporter.Prompt.ALWAYS,
            promptConfiguration = CrashReporter.PromptConfiguration(
                appName = applicationContext.getString(R.string.app_name),
                organizationName = "Mozilla"
            ),
            nonFatalCrashIntent = createNonFatalPendingIntent(),
            enabled = true
        )
    }

    private fun createNonFatalPendingIntent(): PendingIntent {
        return getBroadcast(applicationContext, 0, Intent(NON_FATAL_CRASH_BROADCAST), 0)
    }
}
