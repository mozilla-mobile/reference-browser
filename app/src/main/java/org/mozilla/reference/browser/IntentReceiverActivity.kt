/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.mozilla.reference.browser.ext.components

class IntentReceiverActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent?.let { Intent(it) } ?: Intent()
        val processors = components.utils

        MainScope().launch {
            processors.customTabIntentProcessor.process(intent) ||
                processors.tabIntentProcessor.process(intent)

            val className = if (processors.customTabIntentProcessor.matches(intent)) {
                ExternalAppBrowserActivity::class
            } else {
                BrowserActivity::class
            }
            intent.setClassName(applicationContext, className.java.name)

            startActivity(intent)
            finish()
        }
    }
}
