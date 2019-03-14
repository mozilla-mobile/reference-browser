/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.reference.browser.pip

import android.app.Activity
import mozilla.components.browser.session.SelectionAwareSessionObserver
import mozilla.components.browser.session.Session
import mozilla.components.browser.session.SessionManager
import mozilla.components.feature.session.PictureInPictureFeature
import mozilla.components.support.base.feature.LifecycleAwareFeature

class PictureInPictureIntegration(
    sessionManager: SessionManager,
    activity: Activity
) : LifecycleAwareFeature {
    private val pictureFeature = PictureInPictureFeature(sessionManager, activity)
    private val observer = PictureInPictureObserver(sessionManager) { whiteListed = it }
    private var whiteListed = false

    override fun start() {
        observer.observeSelected()
    }

    override fun stop() {
        observer.stop()
    }

    fun onHomePressed() = if (whiteListed) {
        pictureFeature.enterPipModeCompat()
    } else {
        pictureFeature.onHomePressed()
    }
}

internal class PictureInPictureObserver(
    sessionManager: SessionManager,
    private val whiteListed: (Boolean) -> Unit
) : SelectionAwareSessionObserver(sessionManager) {
    private val whiteList = listOf("youtube.com/tv")

    override fun onSessionSelected(session: Session) {
        super.onSessionSelected(session)
        whiteListed(isWhitelisted(session.url))
    }

    override fun onUrlChanged(session: Session, url: String) {
        whiteListed(isWhitelisted(session.url))
    }

    private fun isWhitelisted(url: String): Boolean {
        val exists = whiteList.firstOrNull { url.contains(it) }
        return exists != null
    }
}
