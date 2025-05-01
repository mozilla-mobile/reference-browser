/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.addons

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import mozilla.components.concept.engine.EngineSession
import mozilla.components.concept.engine.EngineView
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.ui.translateName
import mozilla.components.support.utils.ext.getParcelableCompat
import mozilla.components.support.utils.ext.getParcelableExtraCompat
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components

/**
 * [AddonSettingsActivity] is an activity that displays the settings of a specific add-on.
 * It creates and hosts a [AddonSettingsFragment], passing the add-on details to the fragment
 * for rendering the settings page of the add-on.
 */
class AddonSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_on_settings)

        val addon = requireNotNull(
            intent.getParcelableExtraCompat("add_on", Addon::class.java),
        )

        title = addon.translateName(this)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.addonSettingsContainer, AddonSettingsFragment.create(addon))
            .commit()
    }

    override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? =
        when (name) {
            EngineView::class.java.name -> components.core.engine.createView(context, attrs).asView()
            else -> super.onCreateView(parent, name, context, attrs)
        }

    /**
     * [AddonSettingsFragment] is a fragment responsible for displaying the settings of an add-on.
     * It uses [EngineView] to load and render the add-on's settings page, specified by its `optionsPageUrl`.
     */
    class AddonSettingsFragment : Fragment() {
        private lateinit var optionsPageUrl: String
        private lateinit var engineSession: EngineSession

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            optionsPageUrl = requireNotNull(
                arguments?.getParcelableCompat(
                    "add_on",
                    Addon::class.java,
                )?.installedState?.optionsPageUrl,
            )

            engineSession = requireContext().components.core.engine.createSession()

            return inflater.inflate(R.layout.fragment_add_on_settings, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val addonSettingsEngineView = view.findViewById<View>(R.id.addonSettingsEngineView) as EngineView
            addonSettingsEngineView.render(engineSession)
            engineSession.loadUrl(optionsPageUrl)
        }

        override fun onDestroyView() {
            engineSession.close()
            super.onDestroyView()
        }

        /**
         * Companion object for [AddonSettingsFragment].
         *
         * Provides a method to create a new instance of [AddonSettingsFragment] with the required
         * add-on details passed as arguments.
         */
        companion object {
            /**
             * Creates a new instance of [AddonSettingsFragment] with the provided [Addon] passed as an argument.
             *
             * @param addon The [Addon] for which settings are being displayed.
             * @return A new instance of [AddonSettingsFragment] with the add-on data passed in.
             */
            fun create(addon: Addon) = AddonSettingsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable("add_on", addon)
                }
            }
        }
    }
}
