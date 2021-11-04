/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.tabs

import androidx.recyclerview.widget.ItemTouchHelper
import mozilla.components.browser.state.state.TabSessionState
import mozilla.components.browser.tabstray.TabTouchCallback
import kotlin.math.abs

class TabsTouchHelper(observable: (TabSessionState) -> Unit) :
    ItemTouchHelper(object : TabTouchCallback(observable) {
        override fun alphaForItemSwipe(dX: Float, distanceToAlphaMin: Int): Float {
            return 1f - 2f * abs(dX) / distanceToAlphaMin
        }
    })
