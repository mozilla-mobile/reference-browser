/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.autofill

import android.os.Build
import androidx.annotation.RequiresApi
import mozilla.components.feature.autofill.AbstractAutofillService
import mozilla.components.feature.autofill.AutofillConfiguration
import org.mozilla.reference.browser.ext.components

class AutofillService : AbstractAutofillService() {
    override val configuration: AutofillConfiguration by lazy { components.autofillConfiguration }
}
