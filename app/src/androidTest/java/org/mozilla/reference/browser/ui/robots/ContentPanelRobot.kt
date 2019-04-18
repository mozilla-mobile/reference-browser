/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withText

/**
 * Implementation of Robot Pattern for the Content Panel.
 */
class ContentPanelRobot {
    fun verifyContentPanel() = shareContentPanel()

    class Transition {
        fun contentPanel(interact: ContentPanelRobot.() -> Unit): ContentPanelRobot.Transition {
            return ContentPanelRobot.Transition()
        }
    }
}

private fun shareContentPanel() = onView(withText("Share with..."))
