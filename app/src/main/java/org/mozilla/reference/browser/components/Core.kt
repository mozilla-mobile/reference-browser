/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.components

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import mozilla.components.browser.icons.BrowserIcons
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.session.storage.SessionStorage
import mozilla.components.browser.storage.sync.PlacesHistoryStorage
import mozilla.components.concept.engine.DefaultSettings
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.engine.EngineSession.TrackingProtectionPolicy
import mozilla.components.concept.fetch.Client
import mozilla.components.feature.media.MediaFeature
import mozilla.components.feature.media.RecordingDevicesNotificationFeature
import mozilla.components.feature.media.state.MediaStateMachine
import mozilla.components.feature.session.HistoryDelegate
import org.mozilla.reference.browser.AppRequestInterceptor
import org.mozilla.reference.browser.EngineProvider
import org.mozilla.reference.browser.ext.getPreferenceKey
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.R.string.pref_key_remote_debugging
import org.mozilla.reference.browser.R.string.pref_key_tracking_protection_normal
import org.mozilla.reference.browser.R.string.pref_key_tracking_protection_private
import java.util.concurrent.TimeUnit

/**
 * Component group for all core browser functionality.
 */
class Core(private val context: Context) {
    /**
     * The browser engine component initialized based on the build
     * configuration (see build variants).
     */
    val engine: Engine by lazy {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val defaultSettings = DefaultSettings(
            requestInterceptor = AppRequestInterceptor(context),
            remoteDebuggingEnabled = prefs.getBoolean(context.getPreferenceKey(pref_key_remote_debugging), false),
            testingModeEnabled = prefs.getBoolean(context.getPreferenceKey(R.string.pref_key_testing_mode), false),
            trackingProtectionPolicy = createTrackingProtectionPolicy(prefs),
            historyTrackingDelegate = HistoryDelegate(historyStorage)
        )
        EngineProvider.createEngine(context, defaultSettings)
    }

    /**
     * The [Client] implementation (`concept-fetch`) used for HTTP requests.
     */
    val client: Client by lazy {
        EngineProvider.createClient(context)
    }

    /**
     * The session manager component provides access to a centralized registry of
     * all browser sessions (i.e. tabs). It is initialized here to persist and restore
     * sessions from the [SessionStorage], and with a default session (about:blank) in
     * case all sessions/tabs are closed.
     */
    val sessionManager by lazy {
        val sessionStorage = SessionStorage(context, engine)

        SessionManager(engine).apply {
            sessionStorage.restore()?.let { snapshot -> restore(snapshot) }

            sessionStorage.autoSave(this)
                .periodicallyInForeground(interval = 30, unit = TimeUnit.SECONDS)
                .whenGoingToBackground()
                .whenSessionsChange()

            // Install the "icons" WebExtension to automatically load icons for every visited website.
            icons.install(engine, sessionManager = this)

            // Show an ongoing notification when recording devices (camera, microphone) are used by web content
            RecordingDevicesNotificationFeature(context, sessionManager = this)
                .enable()

            MediaStateMachine.start(this)

            // Enable media features like showing an ongoing notification with media controls when
            // media in web content is playing.
            MediaFeature(context).enable()
        }
    }

    /**
     * The storage component to persist browsing history (with the exception of
     * private sessions).
     */
    val historyStorage by lazy { PlacesHistoryStorage(context) }

    /**
     * Icons component for loading, caching and processing website icons.
     */
    val icons by lazy { BrowserIcons(context, client) }

    /**
     * Constructs a [TrackingProtectionPolicy] based on current preferences.
     *
     * @param prefs the shared preferences to use when reading tracking
     * protection settings.
     * @param normalMode whether or not tracking protection should be enabled
     * in normal browsing mode, defaults to the current preference value.
     * @param privateMode whether or not tracking protection should be enabled
     * in private browsing mode, default to the current preference value.
     * @return the constructed tracking protection policy based on preferences.
     */
    fun createTrackingProtectionPolicy(
        prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context),
        normalMode: Boolean = prefs.getBoolean(context.getPreferenceKey(pref_key_tracking_protection_normal), true),
        privateMode: Boolean = prefs.getBoolean(context.getPreferenceKey(pref_key_tracking_protection_private), true)
    ): TrackingProtectionPolicy {

        val tackingPolicy = TrackingProtectionPolicy.recommended()
        return when {
            normalMode && privateMode -> tackingPolicy
            normalMode && !privateMode -> tackingPolicy.forRegularSessionsOnly()
            !normalMode && privateMode -> tackingPolicy.forPrivateSessionsOnly()
            else -> TrackingProtectionPolicy.select(
                    trackingCategories = arrayOf(TrackingProtectionPolicy.TrackingCategory.NONE),
                    safeBrowsingCategories = arrayOf(TrackingProtectionPolicy.SafeBrowsingCategory.RECOMMENDED),
                    cookiePolicy = TrackingProtectionPolicy.CookiePolicy.ACCEPT_ALL
            )
        }
    }
}
