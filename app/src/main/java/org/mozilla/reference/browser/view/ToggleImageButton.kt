/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.Checkable
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import mozilla.components.support.base.android.Padding
import mozilla.components.support.ktx.android.view.setPadding
import org.mozilla.reference.browser.R

/**
 * Based off the internal version of com.google.android.material.internal.CheckableImageButton
 */
class ToggleImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = androidx.appcompat.R.attr.imageButtonStyle,
) : AppCompatImageButton(context, attrs, defStyle), Checkable {
    private var onCheckedChangeListener: ((ToggleImageButton, Boolean) -> Unit)? = null

    init {
        ViewCompat.setAccessibilityDelegate(
            this,
            object : AccessibilityDelegateCompat() {
                override fun onInitializeAccessibilityEvent(host: View, event: AccessibilityEvent) {
                    super.onInitializeAccessibilityEvent(host, event)
                    event.isChecked = this@ToggleImageButton.isChecked
                }

                override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                    super.onInitializeAccessibilityNodeInfo(host, info)
                    info.isCheckable = true
                    info.isChecked = this@ToggleImageButton.isChecked
                }
            },
        )

        TypedValue().apply {
            context.theme.resolveAttribute(R.attr.selectableItemBackgroundBorderless, this, true)
            setBackgroundResource(resourceId)
        }
        setPadding(PADDING)

        setChecked(attrs)
    }

    private fun setChecked(attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.ToggleImageButton).apply {
            isChecked = getBoolean(R.styleable.ToggleImageButton_android_checked, false)
            recycle()
        }
    }

    override fun isChecked(): Boolean {
        return isSelected
    }

    override fun setChecked(checked: Boolean) {
        isSelected = checked
        onCheckedChangeListener?.invoke(this, checked)
    }

    override fun toggle() {
        isChecked = !isChecked
    }

    override fun performClick(): Boolean {
        toggle()
        return super.performClick()
    }

    fun setOnCheckedChangeListener(listener: (ToggleImageButton, Boolean) -> Unit) {
        this.onCheckedChangeListener = listener
    }

    companion object {
        private const val ACTION_PADDING_DP = 16
        private val PADDING = Padding(ACTION_PADDING_DP, ACTION_PADDING_DP, ACTION_PADDING_DP, ACTION_PADDING_DP)
    }
}
