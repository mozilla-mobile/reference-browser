/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@file:Suppress("DEPRECATION")

package org.mozilla.reference.browser.helpers

import androidx.test.espresso.IdlingRegistry
import androidx.test.rule.ActivityTestRule
import org.mozilla.reference.browser.BrowserActivity

/**
 * A [org.junit.Rule] to handle shared test set up for tests on [BrowserActivity].
 *
 * @param initialTouchMode See [ActivityTestRule]
 * @param launchActivity See [ActivityTestRule]
 */
class BrowserActivityTestRule(
    initialTouchMode: Boolean = false,
    launchActivity: Boolean = true,

) : ActivityTestRule<BrowserActivity>(BrowserActivity::class.java, initialTouchMode, launchActivity) {

    /**
     * Ensures the test doesn't advance until session page load is completed.
     *
     * N.B.: in the current implementation, tests pass without this so it seems to be
     * unnecessary: I think this is because the progress bar animation acts as the
     * necessary idling resource. However, we leave this in just in case the
     * implementation changes and the tests break. In that case, this code might be
     * broken because it's not used, and thus tested, at present.
     */

    private lateinit var loadingIdlingResource: SessionLoadedIdlingResource

    override fun beforeActivityLaunched() {
        loadingIdlingResource = SessionLoadedIdlingResource().also {
            IdlingRegistry.getInstance().register(it)
        }
    }

    override fun afterActivityFinished() {
        IdlingRegistry.getInstance().unregister(loadingIdlingResource)
    }
}
