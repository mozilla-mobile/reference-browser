/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_browser.*
import mozilla.components.feature.downloads.DownloadsFeature
import mozilla.components.feature.downloads.SimpleDownloadDialogFragment.DownloadDialogListener
import mozilla.components.feature.session.SessionFeature
import mozilla.components.feature.tabs.toolbar.TabsToolbarFeature
import mozilla.components.support.ktx.android.content.isPermissionGranted
import org.mozilla.reference.browser.BackHandler
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.requireComponents
import org.mozilla.reference.browser.tabs.TabsTrayFragment

class BrowserFragment : Fragment(), BackHandler, DownloadDialogListener {
    private lateinit var sessionFeature: SessionFeature
    private lateinit var tabsToolbarFeature: TabsToolbarFeature
    private lateinit var downloadsFeature: DownloadsFeature

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

        lifecycle.addObserver(ToolbarIntegration(requireContext(), toolbar, sessionId))
        lifecycle.addObserver(requireComponents.firefoxAccountsIntegration)

        tabsToolbarFeature = TabsToolbarFeature(toolbar, requireComponents.sessionManager, ::showTabs)

        downloadsFeature = DownloadsFeature(
                requireContext(),
                sessionManager = requireComponents.sessionManager,
                fragmentManager = childFragmentManager
        )
        downloadsFeature.onNeedToRequestPermissions = { _, _ ->
            requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), PERMISSION_WRITE_STORAGE_REQUEST)
        }
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
        downloadsFeature.start()
    }

    override fun onStop() {
        super.onStop()

        sessionFeature.stop()
        downloadsFeature.stop()
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_WRITE_STORAGE_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) &&
                        isStoragePermissionAvailable()) {
                    // permission was granted, yay!
                    downloadsFeature.onPermissionsGranted()
                }
            }
        }
    }

    companion object {
        private const val SESSION_ID = "session_id"
        private const val PERMISSION_WRITE_STORAGE_REQUEST = 1

        fun create(sessionId: String? = null): BrowserFragment = BrowserFragment().apply {
            arguments = Bundle().apply {
                putString(SESSION_ID, sessionId)
            }
        }
    }

    private fun isStoragePermissionAvailable() = requireContext().isPermissionGranted(WRITE_EXTERNAL_STORAGE)
}
