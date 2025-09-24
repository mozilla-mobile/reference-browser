/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.autofill

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import mozilla.components.feature.autofill.AutofillConfiguration
import mozilla.components.feature.autofill.ui.AbstractAutofillUnlockActivity
import mozilla.components.support.ktx.android.view.setupPersistentInsets
import org.mozilla.reference.browser.ext.components

class AutofillUnlockActivity : AbstractAutofillUnlockActivity() {
    override val configuration: AutofillConfiguration by lazy { components.autofillConfiguration }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        window.setupPersistentInsets()
    }
}
