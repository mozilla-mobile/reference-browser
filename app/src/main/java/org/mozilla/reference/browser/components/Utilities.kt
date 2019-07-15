/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.components

import android.content.Context
import mozilla.components.browser.session.SessionManager
import mozilla.components.feature.customtabs.CustomTabIntentProcessor
import mozilla.components.feature.intent.TabIntentProcessor
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
     * Provides intent processing functionality for CustomTab intents.
     */
    val customTabIntentProcessor by lazy {
        CustomTabIntentProcessor(sessionManager, sessionUseCases.loadUrl, context.resources)
    }

    /**
     * Provides intent processing functionality for ACTION_VIEW and ACTION_SEND intents.
     */
    val tabIntentProcessor by lazy {
        TabIntentProcessor(sessionManager, sessionUseCases.loadUrl, searchUseCases.newTabSearch)
    }
}
