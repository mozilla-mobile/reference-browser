/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.components

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.View
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.EngineView
import mozilla.components.concept.engine.selection.SelectionActionDelegate
import org.mockito.Mockito.mock

class TestEngineView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr), EngineView {

    override var selectionActionDelegate: SelectionActionDelegate? =
            mock(SelectionActionDelegate::class.java)

    override fun captureThumbnail(onFinish: (Bitmap?) -> Unit) {
    }

    override fun release() {
    }

    override fun render(session: EngineSession) {
    }

    override fun setDynamicToolbarMaxHeight(height: Int) {
    }

    override fun setVerticalClipping(clippingHeight: Int) {
    }
}