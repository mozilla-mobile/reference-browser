/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_browser.*
import mozilla.components.feature.session.SessionFeature
import mozilla.components.feature.tabs.toolbar.TabsToolbarFeature
import org.mozilla.reference.browser.BackHandler
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.requireComponents
import org.mozilla.reference.browser.tabs.TabsTrayFragment

class BrowserFragment : Fragment(), BackHandler {
    private lateinit var sessionFeature: SessionFeature
    private lateinit var tabsToolbarFeature: TabsToolbarFeature

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_browser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionId = arguments?.getString(SESSION_ID)

        sessionFeature = SessionFeature(
                requireComponents.sessionManager,
                requireComponents.sessionUseCases,
                engineView,
                requireComponents.sessionStorage,
                sessionId)

        lifecycle.addObserver(ToolbarIntegration(requireComponents, toolbar, sessionId))

        tabsToolbarFeature = TabsToolbarFeature(context!!, toolbar, ::showTabs)
    }

    private fun showTabs() {
        // For now we are performing manual fragment transactions here. Once we can use the new
        // navigation support library we may want to pass navigation graphs around.
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.container, TabsTrayFragment())
            commit()
        }
    }

    override fun onStart() {
        super.onStart()

        sessionFeature.start()
    }

    override fun onStop() {
        super.onStop()

        sessionFeature.stop()
    }

    override fun onBackPressed(): Boolean {
        if (toolbar.onBackPressed()) {
            return true
        }

        if (sessionFeature.handleBackPressed()) {
            return true
        }

        return false
    }

    companion object {
        private const val SESSION_ID = "session_id"

        fun create(sessionId: String? = null): BrowserFragment = BrowserFragment().apply {
            arguments = Bundle().apply {
                putString(SESSION_ID, sessionId)
            }
        }
    }
}
