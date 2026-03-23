/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.helpers

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.IdlingRegistry
import org.junit.rules.ExternalResource
import org.mozilla.reference.browser.BrowserActivity

/**
 * A [org.junit.Rule] to handle shared test set up for tests on [BrowserActivity].
 */
class BrowserActivityTestRule : ExternalResource() {
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
    private lateinit var scenario: ActivityScenario<BrowserActivity>

    val activity: BrowserActivity
        get() {
            var result: BrowserActivity? = null
            scenario.onActivity { result = it }
            return result!!
        }

    override fun before() {
        loadingIdlingResource = SessionLoadedIdlingResource().also {
            IdlingRegistry.getInstance().register(it)
        }
        scenario = ActivityScenario.launch(BrowserActivity::class.java)
    }

    override fun after() {
        scenario.close()
        IdlingRegistry.getInstance().unregister(loadingIdlingResource)
    }
}
