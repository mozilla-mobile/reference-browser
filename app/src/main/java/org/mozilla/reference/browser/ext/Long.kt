/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ext

import android.content.Context
import android.text.format.DateUtils
import org.mozilla.reference.browser.R
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

@Suppress("MagicNumber")
fun Long.timeSince(context: Context, time: Long): String {
    val earliestValidSyncDate: Date = GregorianCalendar.getInstance().run {
        set(2000, Calendar.JANUARY, 1, 0, 0, 0)
        getTime()
    }

    if (Date(time).before(earliestValidSyncDate)) {
        return context.getString(R.string.preferences_sync_never_synced_summary)
    }

    val relativeTimeSpanString = DateUtils.getRelativeTimeSpanString(time, this, DateUtils.MINUTE_IN_MILLIS)
    return context.resources.getString(R.string.preferences_sync_last_synced_summary, relativeTimeSpanString)
}
