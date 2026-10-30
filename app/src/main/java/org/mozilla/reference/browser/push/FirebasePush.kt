/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.push

import android.annotation.SuppressLint
import mozilla.components.lib.push.firebase.AbstractFirebasePushService

/**
 * Receives Firebase Cloud Messaging events and hands them to the push feature. The token refresh is handled by
 * [AbstractFirebasePushService], so the lint warning about a missing override does not apply.
 */
@SuppressLint("MissingFirebaseInstanceTokenRefresh") class FirebasePush : AbstractFirebasePushService()
