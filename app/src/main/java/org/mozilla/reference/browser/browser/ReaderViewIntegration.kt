/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.browser

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.concept.engine.Engine
import mozilla.components.feature.findinpage.view.FindInPageBar
import mozilla.components.feature.readerview.ReaderViewFeature
import mozilla.components.feature.readerview.view.ReaderViewControlsView
import mozilla.components.support.base.feature.LifecycleAwareFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.reference.browser.R

class ReaderViewIntegration(
    context: Context,
    engine: Engine,
    store: BrowserStore,
    toolbar: BrowserToolbar,
    view: ReaderViewControlsView,
    readerViewAppearanceButton: FloatingActionButton,
) : LifecycleAwareFeature, UserInteractionHandler {

    private var readerViewButtonVisible = false

    private val readerViewButton: BrowserToolbar.ToggleButton = BrowserToolbar.ToggleButton(
        image = ContextCompat.getDrawable(context, R.drawable.mozac_ic_reader_mode)!!,
        imageSelected = ContextCompat.getDrawable(context, R.drawable.mozac_ic_reader_mode)!!.mutate().apply {
            setTint(ContextCompat.getColor(context, R.color.photonBlue40))
        },
        contentDescription = "Enable Reader View",
        contentDescriptionSelected = "Disable Reader View",
        selected = store.state.selectedTab?.readerState?.active ?: false,
        visible = { readerViewButtonVisible },
    ) { enabled ->
        if (enabled) {
            feature.showReaderView()
            readerViewAppearanceButton.show()
        } else {
            feature.hideReaderView()
            feature.hideControls()
            readerViewAppearanceButton.hide()
        }
    }

    init {
        toolbar.addPageAction(readerViewButton)
        readerViewAppearanceButton.setOnClickListener { feature.showControls() }
    }

    private val feature = ReaderViewFeature(context, engine, store, view) { available, active ->
        readerViewButtonVisible = available
        readerViewButton.setSelected(active)

        if (active) readerViewAppearanceButton.show() else readerViewAppearanceButton.hide()
        toolbar.invalidateActions()
    }

    override fun start() {
        feature.start()
    }

    override fun stop() {
        feature.stop()
    }

    override fun onBackPressed(): Boolean {
        return feature.onBackPressed()
    }
}

/**
 * [CoordinatorLayout.Behavior] that will always position the reader view appearance button above
 * the [FindInPageBar] (including when the browser toolbar is scrolling or performing a snap animation).
 */
@Suppress("unused") // Referenced from XML
class ReaderViewAppearanceButtonBehavior(
    context: Context,
    attrs: AttributeSet,
) : CoordinatorLayout.Behavior<FloatingActionButton>(context, attrs) {
    override fun layoutDependsOn(parent: CoordinatorLayout, child: FloatingActionButton, dependency: View): Boolean {
        if (dependency is FindInPageBar || dependency is BrowserToolbar) {
            return true
        }

        return super.layoutDependsOn(parent, child, dependency)
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: FloatingActionButton,
        dependency: View,
    ): Boolean {
        return if (dependency is FindInPageBar || dependency is BrowserToolbar) {
            repositionReaderViewAppearanceButton(child, dependency)
            true
        } else {
            false
        }
    }

    private fun repositionReaderViewAppearanceButton(button: FloatingActionButton, toolbar: View) {
        button.translationY = (toolbar.translationY + toolbar.height * -1.0).toFloat()
    }
}
