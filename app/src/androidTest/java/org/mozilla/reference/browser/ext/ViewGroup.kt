/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ext

import android.view.View
import android.view.ViewGroup

/**
 * The [ViewGroup] children as an [Iterable] on [View]s
 */
val ViewGroup.children: Iterable<View>
    get() = (0 until childCount).map(this::getChildAt)
