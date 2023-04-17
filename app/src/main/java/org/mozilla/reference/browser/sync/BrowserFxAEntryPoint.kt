/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.sync

import mozilla.components.concept.sync.FxAEntryPoint

/**
 * Reference Browser implementation of [FxAEntryPoint].
 */
enum class BrowserFxAEntryPoint(override val entryName: String) : FxAEntryPoint {
    /**
     * Authenticating from the home menu (the hamburger menu)
     */
    HomeMenu("home-menu"),
}
