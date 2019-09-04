/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.components

import android.content.Context
import mozilla.components.browser.session.SessionManager
import mozilla.components.feature.customtabs.CustomTabIntentProcessor
import mozilla.components.feature.intent.processing.TabIntentProcessor
import mozilla.components.feature.pwa.ManifestStorage
import mozilla.components.feature.pwa.intent.WebAppIntentProcessor
import mozilla.components.feature.search.SearchUseCases
import mozilla.components.feature.session.SessionUseCases

/**
 * Component group for miscellaneous components.
 */
class Utilities(
    private val context: Context,
    private val sessionManager: SessionManager,
    private val sessionUseCases: SessionUseCases,
    private val searchUseCases: SearchUseCases
) {

    /**
     * Provides intent processing functionality for Progressive Web App and Custom Tab intents.
     */
    val externalIntentProcessors by lazy {
        listOf(
            CustomTabIntentProcessor(sessionManager, sessionUseCases.loadUrl, context.resources),
            WebAppIntentProcessor(sessionManager, sessionUseCases.loadUrl, ManifestStorage(context))
        )
    }

    /**
     * Provides intent processing functionality for ACTION_VIEW and ACTION_SEND intents,
     * along with external intent processors.
     */
    val intentProcessors by lazy {
        externalIntentProcessors +
            TabIntentProcessor(sessionManager, sessionUseCases.loadUrl, searchUseCases.newTabSearch)
    }
}
