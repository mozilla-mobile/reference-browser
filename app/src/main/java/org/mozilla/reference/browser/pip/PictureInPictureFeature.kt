/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.pip

import android.annotation.TargetApi
import android.app.Activity
import android.app.PictureInPictureParams
import android.os.Build
import androidx.fragment.app.Fragment
import mozilla.components.browser.session.SessionManager
import org.mozilla.reference.browser.BackHandler

/**
 * A simple implementation of Picture-in-picture mode support.
 *
 * @param sessionManager Session Manager for observing the selected session's fullscreen mode changes.
 * @param activity the activity with the EngineView for calling PIP mode when required; the AndroidX Fragment
 * doesn't support this.
 * @param fragment the current fragment for back button handling.
 */
class PictureInPictureFeature(
    private val sessionManager: SessionManager,
    private val activity: Activity,
    private val fragment: Fragment
) {

    fun onHomePressed(): Boolean {
        val fullScreenMode = sessionManager.selectedSession?.fullScreenMode ?: false
        return fullScreenMode && enterPipModeCompat()
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun enterPipModeCompat() = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
            activity.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
            activity.enterPictureInPictureMode()
            true
        }
        else -> false
    }

    fun onPictureInPictureModeChanged(enabled: Boolean) {
        val fullScreenMode = sessionManager.selectedSession?.fullScreenMode ?: false
        // If we're exiting PIP mode and we're in fullscreen mode, then we should exit fullscreen mode as well.
        if (!enabled && fullScreenMode && fragment is BackHandler) {
            fragment.onBackPressed()
        }
    }
}
