/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ext

import android.app.Activity
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Applies the specified window insets to the activity's [android.R.id.content].
 *
 * The default applied insets are [WindowInsetsCompat.Type.systemBars]
 */
fun Activity.applyWindowInsets(
    insetsTypeMask: Int = WindowInsetsCompat.Type.systemBars(),
) {
    val content = findViewById<ViewGroup>(android.R.id.content)
    ViewCompat.setOnApplyWindowInsetsListener(content) { view, windowInsets ->
        val insets = windowInsets.getInsets(insetsTypeMask)

        view.updatePadding(
            left = insets.left,
            top = insets.top,
            right = insets.right,
            bottom = insets.bottom,
        )

        windowInsets
    }
}
