/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.uiautomator.UiSelector
import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers.allOf
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime

/**
 * Implementation of Robot Pattern for the addons manager.
 */
class AddonsManagerRobot {

    fun verifyAddonsView() = assertAddonsView()

    class Transition {
        fun addonsManager(interact: AddonsManagerRobot.() -> Unit): AddonsManagerRobot.Transition {
            AddonsManagerRobot().interact()
            return AddonsManagerRobot.Transition()
        }
    }

    private fun assertAddonsView() {
        assertTrue(mDevice.findObject(UiSelector().text("Recommended")).waitForExists(waitingTime))
        // Check uBlock is displayed in the "Recommended" section
        onView(
            allOf(
                withId(R.id.add_on_item),
                    hasDescendant(withText("uBlock Origin")),
                    hasDescendant(withText("Finally, an efficient wide-spectrum content blocker. Easy on CPU and memory.")),
                    hasDescendant(withId(R.id.rating)),
                    hasDescendant(withId(R.id.users_count))
            )
        ).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }
}
