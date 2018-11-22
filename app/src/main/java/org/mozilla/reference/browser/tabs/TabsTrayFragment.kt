/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabs

import android.content.res.Resources
import android.graphics.PorterDuff.Mode.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_tabstray.*
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.concept.toolbar.Toolbar
import mozilla.components.feature.tabs.tabstray.TabsFeature
import mozilla.components.ui.colors.R.color.*
import org.mozilla.reference.browser.BackHandler
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.browser.BrowserFragment
import org.mozilla.reference.browser.ext.requireComponents

/**
 * A fragment for displaying the tabs tray.
 */
class TabsTrayFragment : Fragment(), BackHandler {
    private var tabsFeature: TabsFeature? = null
    private lateinit var tabsButton: BrowserToolbar.TwoStateButton
    private lateinit var privateTabsButton: BrowserToolbar.TwoStateButton
    private val browserActions: MutableList<DisplayAction> = mutableListOf()
    private var regularTabs = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_tabstray, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setNavigationIcon(R.drawable.mozac_ic_back)
        toolbar.setNavigationOnClickListener {
            closeTabsTray()
        }

        toolbar.inflateMenu(R.menu.tabstray_menu)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.newTab -> {
                    when (regularTabs) {
                        true ->
                            requireComponents.tabsUseCases.addTab.invoke("about:blank", selectTab = true)
                        false ->
                            requireComponents.tabsUseCases.addPrivateTab.invoke("about:privatebrowsing", selectTab = true)
                    }
                    closeTabsTray()
                }
            }
            true
        }

        tabsButton = BrowserToolbar.TwoStateButton(
                resources.getThemedDrawable(R.drawable.mozac_ic_tab),
                "Tabs",
                resources.getThemedDrawable(R.drawable.mozac_ic_tab).colorTint(photonPurple50),
                "Tabs selected",
                isEnabled = { regularTabs }
        ) {
        }

        privateTabsButton = BrowserToolbar.TwoStateButton(
                resources.getThemedDrawable(R.drawable.mozac_ic_globe),
                "Private tabs",
                resources.getThemedDrawable(R.drawable.mozac_ic_globe).colorTint(photonPurple50),
                "Private tabs selected",
                isEnabled = { !regularTabs }
        ) {
        }

        toolbar.apply {
            addBrowserAction(tabsButton)
            addBrowserAction(privateTabsButton)
        }

        tabsFeature = TabsFeature(
            tabsTray,
            requireComponents.sessionManager,
            requireComponents.tabsUseCases,
            ::closeTabsTray)
    }

    override fun onStart() {
        super.onStart()

        tabsFeature?.start()
    }

    override fun onStop() {
        super.onStop()

        tabsFeature?.stop()
    }

    override fun onBackPressed(): Boolean {
        closeTabsTray()
        return true
    }

    private fun closeTabsTray() {
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.container, BrowserFragment.create())
            commit()
        }
    }

    private fun Resources.getThemedDrawable(@DrawableRes resId: Int) = getDrawable(resId, context?.theme)

    private fun Drawable.colorTint(@ColorRes color: Int) = apply {
        mutate()
        setColorFilter(ContextCompat.getColor(requireContext(), color), SRC_IN)
    }

    private fun ViewGroup.addBrowserAction(action: Toolbar.Action) {
        val displayAction = DisplayAction(action)

        if (action.visible()) {
            action.createView(toolbar).let {
                displayAction.view = it
                addView(it)
            }
        }

        browserActions.add(displayAction)
    }

    private class DisplayAction(
            var actual: Toolbar.Action,
            var view: View? = null
    )
}
