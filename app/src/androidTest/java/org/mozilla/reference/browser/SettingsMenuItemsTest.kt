package org.mozilla.reference.browser

import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import androidx.test.filters.LargeTest
import android.view.View
import android.view.ViewGroup

import org.hamcrest.Matchers.allOf
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText

@LargeTest
@RunWith(AndroidJUnit4::class)
class SettingsMenuItemsTest {

    @get:Rule
    var mActivityTestRule = ActivityTestRule(BrowserActivity::class.java)

    @Test
    fun browserActivityTest() {
        val appCompatImageButton = onView(
                allOf(withContentDescription("Menu"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.toolbar),
                                        0),
                                2),
                        isDisplayed()))
        appCompatImageButton.perform(click())

        val textView = onView(
                allOf(withText("Share"),
                        childAtPosition(
                                allOf(withId(R.id.mozac_browser_menu_recyclerView),
                                        childAtPosition(
                                                IsInstanceOf.instanceOf(android.widget.FrameLayout::class.java),
                                                0)),
                                1),
                        isDisplayed()))
        textView.check(matches(withText("Share")))

        val textView2 = onView(
                allOf(withText("Settings"),
                        childAtPosition(
                                allOf(withId(R.id.mozac_browser_menu_recyclerView),
                                        childAtPosition(
                                                IsInstanceOf.instanceOf(android.widget.FrameLayout::class.java),
                                                0)),
                                2),
                        isDisplayed()))
        textView2.check(matches(withText("Settings")))

        val textView3 = onView(
                allOf(withText("Clear Data"),
                        childAtPosition(
                                allOf(withId(R.id.mozac_browser_menu_recyclerView),
                                        childAtPosition(
                                                IsInstanceOf.instanceOf(android.widget.FrameLayout::class.java),
                                                0)),
                                3),
                        isDisplayed()))
        textView3.check(matches(withText("Clear Data")))

        val checkBox = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.mozac_browser_menu_recyclerView),
                                childAtPosition(
                                        IsInstanceOf.instanceOf(android.widget.FrameLayout::class.java),
                                        0)),
                        4),
                        isDisplayed()))
        checkBox.check(matches(isDisplayed()))
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>,
        position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return (parent is ViewGroup && parentMatcher.matches(parent) &&
                        view == parent.getChildAt(position))
            }
        }
    }
}
