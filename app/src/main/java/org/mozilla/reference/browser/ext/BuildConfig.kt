/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ext

import org.mozilla.reference.browser.BuildConfig

val isCrashReportActive: Boolean
    get() = !BuildConfig.DEBUG && BuildConfig.CRASH_REPORTING_ENABLED
