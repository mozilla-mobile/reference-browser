/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.addons

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import mozilla.components.browser.state.action.WebExtensionAction
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.EngineView
import mozilla.components.concept.engine.window.WindowRequest
import mozilla.components.lib.state.ext.consumeFrom
import mozilla.components.support.ktx.android.view.setupPersistentInsets
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components

/**
 * An activity to show the pop up action of a web extension.
 */
class WebExtensionActionPopupActivity : AppCompatActivity() {
    private lateinit var webExtensionId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_add_on_settings)
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        window.setupPersistentInsets()

        webExtensionId = requireNotNull(intent.getStringExtra("web_extension_id"))
        intent.getStringExtra("web_extension_name")?.let {
            title = it
        }

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.addonSettingsContainer, WebExtensionActionPopupFragment.create(webExtensionId))
            .commit()
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet,
    ): View? =
        when (name) {
            EngineView::class.java.name ->
                components.core.engine
                .createView(context, attrs)
                .asView()
            else -> super.onCreateView(parent, name, context, attrs)
        }

    /**
     * A fragment to show the web extension action popup with [EngineView].
     */
    class WebExtensionActionPopupFragment :
        Fragment(),
        EngineSession.Observer {
        private var engineSession: EngineSession? = null
        private lateinit var webExtensionId: String

        private val addonSettingsEngineView: EngineView
            get() = requireView().findViewById<View>(R.id.addonSettingsEngineView) as EngineView

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
        ): View? {
            webExtensionId = requireNotNull(arguments?.getString("web_extension_id"))
            engineSession = requireContext()
                .components.core.store.state.extensions[webExtensionId]
                ?.popupSession

            return inflater.inflate(R.layout.fragment_add_on_settings, container, false)
        }

        override fun onViewCreated(
            view: View,
            savedInstanceState: Bundle?,
        ) {
            super.onViewCreated(view, savedInstanceState)

            val session = engineSession
            if (session != null) {
                addonSettingsEngineView.render(session)
                consumePopupSession()
            } else {
                consumeFrom(requireContext().components.core.store) { state ->
                    state.extensions[webExtensionId]?.let { extState ->
                        extState.popupSession?.let {
                            if (engineSession == null) {
                                addonSettingsEngineView.render(it)
                                consumePopupSession()
                                engineSession = it
                            }
                        }
                    }
                }
            }
        }

        override fun onStart() {
            super.onStart()
            engineSession?.register(this)
        }

        override fun onStop() {
            super.onStop()
            engineSession?.unregister(this)
        }

        override fun onWindowRequest(windowRequest: WindowRequest) {
            if (windowRequest.type == WindowRequest.Type.CLOSE) {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        }

        private fun consumePopupSession() {
            requireContext().components.core.store.dispatch(
                WebExtensionAction.UpdatePopupSessionAction(webExtensionId, popupSession = null),
            )
        }

        companion object {
            /**
             * Create an [WebExtensionActionPopupFragment] with webExtensionId as a required parameter.
             */
            fun create(webExtensionId: String) =
                WebExtensionActionPopupFragment().apply {
                arguments = Bundle().apply {
                    putString("web_extension_id", webExtensionId)
                }
            }
        }
    }
}
