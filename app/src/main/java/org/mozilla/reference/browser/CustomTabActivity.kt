/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.os.Bundle
import org.mozilla.reference.browser.ext.components

class CustomTabActivity : BrowserActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        // If a Custom Tab is restored by the OS after low memory, we need to process the intent again.
        components.utils.intentProcessor.process(intent)

        super.onCreate(savedInstanceState)
    }
}
