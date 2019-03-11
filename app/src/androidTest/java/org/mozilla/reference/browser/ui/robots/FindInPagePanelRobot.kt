/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.uiautomator.UiDevice
import org.mozilla.reference.browser.helpers.click
import org.mozilla.reference.browser.R

/**
 * Implementation of Robot Pattern for the FindInPage Panel.
 */
class FindInPagePanelRobot {

    val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    fun verifyFindInPageNextButton() = findInPageNextButton()
    fun verifyFindInPagePrevButton() = findInPagePrevButton()
    fun verifyFindInPageCloseButton() = findInPageCloseButton()

    fun enterFindInPageQuery(expectedText: String) {
        findInPageQuery().perform(clearText(), typeText(expectedText))
    }

    fun verifyFindNextInPageResult(ratioCounter: String) {
        mDevice.waitForIdle()
        findInPageResult().check(matches(withText((ratioCounter))))
        findInPageNextButton().click()
    }

    fun verifyFindPrevInPageResult(ratioCounter: String) {
        mDevice.waitForIdle()
        findInPageResult().check(matches(withText((ratioCounter))))
        findInPagePrevButton().click()
    }

    class Transition {
        fun findInPage(interact: FindInPagePanelRobot.() -> Unit): FindInPagePanelRobot.Transition {
            return FindInPagePanelRobot.Transition()
        }
    }
}

private fun findInPageQuery() = onView(withId(R.id.find_in_page_query_text))
private fun findInPageResult() = onView(withId(R.id.find_in_page_result_text))
private fun findInPageNextButton() = onView(withId(R.id.find_in_page_next_btn))
private fun findInPagePrevButton() = onView(withId(R.id.find_in_page_prev_btn))
private fun findInPageCloseButton() = onView(withId(R.id.find_in_page_close_btn))
