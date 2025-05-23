/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.helpers

import androidx.test.espresso.IdlingResource
import androidx.test.platform.app.InstrumentationRegistry
import mozilla.components.browser.state.selector.selectedTab
import org.mozilla.reference.browser.BrowserApplication

/**
 * An IdlingResource implementation that waits until the current session is not loading anymore.
 * Only after loading has completed further actions will be performed.
 */

class SessionLoadedIdlingResource : IdlingResource {
    private var resourceCallback: IdlingResource.ResourceCallback? = null

    override fun getName(): String = SessionLoadedIdlingResource::class.java.simpleName

    override fun isIdleNow(): Boolean {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
            as BrowserApplication

        val store = context.components.core.store

        return if (store.state.selectedTab
            ?.content
            ?.loading == true
        ) {
            false
        } else {
            invokeCallback()
            true
        }
    }

    private fun invokeCallback() {
        if (resourceCallback != null) {
            resourceCallback!!.onTransitionToIdle()
        }
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        this.resourceCallback = callback
    }
}
