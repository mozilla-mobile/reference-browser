/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import androidx.fragment.app.Fragment
import org.mozilla.reference.browser.browser.ExternalAppBrowserFragment

/**
 * Activity that holds the BrowserFragment that is launched within an external app,
 * such as custom tabs and progressive web apps.
 */
class ExternalAppBrowserActivity : BrowserActivity() {
    override fun createBrowserFragment(sessionId: String?): Fragment =
        if (sessionId != null) {
            ExternalAppBrowserFragment.create(sessionId)
        } else {
            // Fall back to browser fragment
            super.createBrowserFragment(sessionId)
        }
}
