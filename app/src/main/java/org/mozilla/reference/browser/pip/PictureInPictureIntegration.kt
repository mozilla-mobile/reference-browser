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

    override fun onLoadingStateChanged(session: Session, loading: Boolean) {
        super.onLoadingStateChanged(session, loading)
        println("SEVTEST: loading: $loading, session: $session")
    }

// The following was printed on my first page load after opening a new tab. Not reproducible on following page loads
/*
2019-10-18 16:43:02.119 14883-14883/org.mozilla.reference.browser.debug I/System.out: SEVTEST: loading: true, session: Session(363e8f25-3d40-49df-a0b7-e6dadd90d364, http://google.com)
2019-10-18 16:43:02.151 14883-14883/org.mozilla.reference.browser.debug I/System.out: SEVTEST: loading: false, session: Session(363e8f25-3d40-49df-a0b7-e6dadd90d364, http://google.com)
2019-10-18 16:43:02.553 14883-14883/org.mozilla.reference.browser.debug I/System.out: SEVTEST: loading: true, session: Session(363e8f25-3d40-49df-a0b7-e6dadd90d364, http://google.com)
2019-10-18 16:43:02.581 14883-14883/org.mozilla.reference.browser.debug I/System.out: SEVTEST: loading: false, session: Session(363e8f25-3d40-49df-a0b7-e6dadd90d364, http://google.com)
2019-10-18 16:43:02.631 14883-14883/org.mozilla.reference.browser.debug I/System.out: SEVTEST: loading: true, session: Session(363e8f25-3d40-49df-a0b7-e6dadd90d364, http://google.com)
2019-10-18 16:43:03.626 14883-14883/org.mozilla.reference.browser.debug I/System.out: SEVTEST: loading: false, session: Session(363e8f25-3d40-49df-a0b7-e6dadd90d364, https://www.google.com/?gws_rd=ssl)
*/

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
