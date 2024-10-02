/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.autofill

import android.os.Build
import androidx.annotation.RequiresApi
import mozilla.components.feature.autofill.AutofillConfiguration
import mozilla.components.feature.autofill.ui.AbstractAutofillConfirmActivity
import org.mozilla.reference.browser.ext.components

/**
 * [AutofillConfirmActivity] is an activity that confirms autofill actions, part of
 * the Android Autofill framework. It extends [AbstractAutofillConfirmActivity] and provides
 * a configuration to the autofill system.
 *
 * This activity is only available for devices running Android O (API level 26) or above.
 */
@RequiresApi(Build.VERSION_CODES.O)
class AutofillConfirmActivity : AbstractAutofillConfirmActivity() {
    /**
     * The [AutofillConfiguration] for this activity, retrieved lazily from the
     * application's components. This configuration defines how autofill should behave
     * and what data it should use.
     */
    override val configuration: AutofillConfiguration by lazy { components.autofillConfiguration }
}
