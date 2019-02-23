/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.web.sugar.Web.onWebView
import org.mozilla.reference.browser.R

/**
 * Implementation of Robot Pattern for awesomebar.
 */

class AwesomeBarRobot {

    fun clearUrl() = urlBarSearch().perform(clearText())

    class Transition {

        fun openWebPage(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {

            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }
    }
}

fun browser(interact: AwesomeBarRobot.() -> Unit): AwesomeBarRobot.Transition {
    AwesomeBarRobot().interact()
    return AwesomeBarRobot.Transition()
}
private fun webView() = onWebView()
private fun urlBarSearch() = onView(withId(R.id.mozac_browser_toolbar_edit_url_view))
