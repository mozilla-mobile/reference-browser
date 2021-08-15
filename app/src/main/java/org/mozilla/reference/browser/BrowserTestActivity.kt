/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import org.mozilla.reference.browser.ext.components

/**
 * This activity is used for performance testing with Raptor/tp6:
 * https://wiki.mozilla.org/Performance_sheriffing/Raptor
 */
class BrowserTestActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // We're creating a default session here so that we don't end up
        // with a gecko session that we don't manage. We want to receive
        // callbacks to update the UI (toolbar) which might have an impact
        // on performance as well (e.g. progress bar animations).
        components.useCases.tabsUseCases.addTab("about:blank")

        startActivity(Intent(this, BrowserActivity::class.java))
    }
}
