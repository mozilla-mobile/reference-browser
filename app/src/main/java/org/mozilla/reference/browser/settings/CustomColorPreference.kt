/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import org.mozilla.reference.browser.R

/**
 * This preference is used to define custom  colors for both title and summary texts.
 * Color code #777777 (placeholder_grey) is used as the fallback color for both title and summary.
 */
class CustomColorPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : Preference(context, attrs, defStyle) {
    private var titleColor: Int = 0
    private var summaryColor: Int = 0

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.CustomColorPreference).apply {
            titleColor = getColor(
                R.styleable.CustomColorPreference_titleColor,
                ContextCompat.getColor(context, R.color.placeholder_grey),
            )
            summaryColor = getColor(
                R.styleable.CustomColorPreference_summaryColor,
                ContextCompat.getColor(context, R.color.placeholder_grey),
            )
            recycle()
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder?.let {
            val title = it.itemView.findViewById(android.R.id.title) as TextView
            val summary = it.itemView.findViewById(android.R.id.summary) as TextView
            title.setTextColor(titleColor)
            summary.setTextColor(summaryColor)
        }
    }
}
