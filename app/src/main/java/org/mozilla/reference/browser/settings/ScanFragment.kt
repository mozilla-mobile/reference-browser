/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.settings

import android.content.pm.PackageManager
import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.mozilla.reference.browser.ext.requireComponents

class ScanFragment : Fragment(), ZXingScannerView.ResultHandler {
    private var scannerView: ZXingScannerView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val cameraPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 1)
        }
        scannerView = ZXingScannerView(requireActivity());
        return scannerView;
    }

    override fun onResume() {
        super.onResume()
        scannerView?.setResultHandler(this)
        scannerView?.startCamera()
    }

    override fun onPause() {
        super.onPause()
        scannerView?.stopCamera()
    }

    override fun handleResult(rawResult: Result) {
        activity?.finish()
        requireComponents.firefoxAccountsIntegration.pair(rawResult.text)
    }
}
