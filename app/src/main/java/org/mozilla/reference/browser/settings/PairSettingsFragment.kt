/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.Manifest.permission.CAMERA
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import mozilla.components.feature.qr.QrFeature
import mozilla.components.support.base.feature.UserInteractionHandler
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.requireComponents
import org.mozilla.reference.browser.sync.BrowserFxAEntryPoint

class PairSettingsFragment : Fragment(), UserInteractionHandler {
    private val qrFeature = ViewBoundFeatureWrapper<QrFeature>()
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
                qrFeature.withFeature {
                    it.onPermissionsResult(arrayOf(CAMERA), IntArray(0))
                }
            }
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pairing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        qrFeature.set(
            feature = QrFeature(
                requireContext(),
                fragmentManager = parentFragmentManager,
                onNeedToRequestPermissions = { permissions ->
                    requestPermissionLauncher.launch(permissions[0])
                },
                onScanResult = { pairingUrl ->
                    requireComponents.services.accountsAuthFeature.beginPairingAuthentication(
                        requireContext(),
                        pairingUrl,
                        BrowserFxAEntryPoint.HomeMenu,
                    )
                    activity?.finish()
                },
            ),
            owner = this,
            view = view,
        )

        qrFeature.withFeature {
            it.scan(R.id.pair_layout)
        }
    }

    override fun onBackPressed(): Boolean {
        qrFeature.onBackPressed()
        parentFragmentManager.popBackStack()
        return true
    }
}
