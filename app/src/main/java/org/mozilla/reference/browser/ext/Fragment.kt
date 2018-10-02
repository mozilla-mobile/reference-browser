/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ext

import android.support.v4.app.Fragment
import org.mozilla.reference.browser.Components

/**
 * Get the requireComponents of this application.
 */
val Fragment.requireComponents: Components
    get() = requireContext().components
