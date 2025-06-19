/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.autofill

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.view.WindowInsetsCompat
import mozilla.components.feature.autofill.AutofillConfiguration
import mozilla.components.feature.autofill.ui.AbstractAutofillUnlockActivity
import org.mozilla.reference.browser.ext.applyWindowInsets
import org.mozilla.reference.browser.ext.components

@RequiresApi(Build.VERSION_CODES.O)
class AutofillUnlockActivity : AbstractAutofillUnlockActivity() {
    override val configuration: AutofillConfiguration by lazy { components.autofillConfiguration }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        applyWindowInsets(WindowInsetsCompat.Type.systemBars())
    }
}
