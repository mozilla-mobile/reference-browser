/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser

import android.app.Activity

/**
 * Interface for fragments that want to handle user interactions.
 */
interface UserInteractionHandler {
    /**
     * In most cases, when the home button is pressed, we invoke this callback to inform the app that the user
     * is going to leave the app.
     *
     * See also [Activity.onUserLeaveHint] for more details.
     */
    fun onHomePressed(): Boolean
}
