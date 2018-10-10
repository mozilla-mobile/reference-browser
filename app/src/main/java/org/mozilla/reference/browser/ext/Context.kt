/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ext

import android.content.Context
import android.support.annotation.StringRes
import org.mozilla.reference.browser.BrowserApplication
import org.mozilla.reference.browser.Components

/**
 * Get the BrowserApplication object from a context.
 */
val Context.application: BrowserApplication
    get() = applicationContext as BrowserApplication

/**
 * Get the requireComponents of this application.
 */
val Context.components: Components
    get() = application.components

fun Context.getPreferenceKey(@StringRes resourceId: Int): String =
    resources.getString(resourceId)
