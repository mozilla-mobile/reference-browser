/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@file:Suppress("TooManyFunctions")

package org.mozilla.fenix.ui.robots

import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiSelector
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert.assertEquals
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime
import org.mozilla.reference.browser.helpers.TestHelper.packageName
import org.mozilla.reference.browser.helpers.click
import org.mozilla.reference.browser.ui.robots.mDevice

/**
 * Implementation of Robot Pattern for Reader View UI.
 */
class ReaderViewRobot {

    fun verifyAppearanceButtonExists() = assertAppearanceButtonExists()
    fun verifyAppearanceButtonDoesntExists() = assertAppearanceButtonDoesntExists()
    fun verifyAppearanceMenuExists() = assertAppearanceMenu()
    fun verifyFontGroupButtons() = assertFontGroupButtons()
    fun verifyIncreaseFontSizeButton() = assertIncreaseFontSizeButton()
    fun verifyDecreaseFontSizeButton() = assertDecreaseFontSizeButton()
    fun verifyColorSchemeGroupButtons() = assertColorSchemeGroupButtons()
    fun clickAppearanceButton() = appearanceButton().click()
    fun clickSansSerifButton() = sansSerifButton().click()
    fun clickSerifButton() = serifButton().click()
    fun clickIncreaseFontSizeButton() = increaseFontSizeButton().click()
    fun clickDecreaseFontSizeButton() = decreaseFontSizeButton().click()
    fun clickLightColorButton() = lightColorButton().click()
    fun clickSepiaColorButton() = sepiaColorButton().click()
    fun clickDarkColorButton() = darkColorButton().click()

    fun verifyActiveAppearanceFont(fontType: String) {
        val fontTypeKey: String = "mozac-readerview-fonttype"

        val prefs = InstrumentationRegistry.getInstrumentation()
            .targetContext.getSharedPreferences(
                "mozac_feature_reader_view",
                Context.MODE_PRIVATE,
            )

        assertEquals(fontType, prefs.getString(fontTypeKey, ""))
    }

    fun verifyAppearanceFontSize(expectedFontSize: Int) {
        val fontSizeKey: String = "mozac-readerview-fontsize"

        val prefs = InstrumentationRegistry.getInstrumentation()
            .targetContext.getSharedPreferences(
                "mozac_feature_reader_view",
                Context.MODE_PRIVATE,
            )

        val fontSizeKeyValue = prefs.getInt(fontSizeKey, 3)

        assertEquals(expectedFontSize, fontSizeKeyValue)
    }

    fun verifyAppearanceColorScheme(expectedColorScheme: String) {
        val colorSchemeKey: String = "mozac-readerview-colorscheme"

        val prefs = InstrumentationRegistry.getInstrumentation()
            .targetContext.getSharedPreferences(
                "mozac_feature_reader_view",
                Context.MODE_PRIVATE,
            )

        assertEquals(expectedColorScheme, prefs.getString(colorSchemeKey, ""))
    }

    class Transition {
        fun dismissAppearanceMenu(interact: ReaderViewRobot.() -> Unit): ReaderViewRobot.Transition {
            mDevice.pressBack()

            ReaderViewRobot().interact()
            return ReaderViewRobot.Transition()
        }
    }
}

private fun appearanceButton() =
    onView(
        allOf(
            withId(R.id.readerViewAppearanceButton),
            hasSibling(withId(R.id.toolbar)),
        ),
    )

private fun appearanceMenu() =
    onView(
        allOf(
            withId(R.id.readerViewAppearanceButton),
            hasSibling(withId(R.id.swipeRefresh)),
        ),
    )

private fun fontGroupButtons() =
    onView(
        allOf(
            withId(R.id.mozac_feature_readerview_font_group),
            withParent(withId(R.id.readerViewBar)),
        ),
    )

private fun sansSerifButton() =
    onView(
        allOf(
            withId(R.id.mozac_feature_readerview_font_sans_serif),
            withParent(withId(R.id.mozac_feature_readerview_font_group)),
        ),
    )

private fun serifButton() =
    onView(
        allOf(
            withId(R.id.mozac_feature_readerview_font_serif),
            withParent(withId(R.id.mozac_feature_readerview_font_group)),
        ),
    )

private fun increaseFontSizeButton() =
    onView(
        allOf(
            withText("+"),
            withParent(withId(R.id.readerViewBar)),
        ),
    )

private fun decreaseFontSizeButton() =
    onView(
        allOf(
            withText("−"),
            withParent(withId(R.id.readerViewBar)),
        ),
    )

private fun colorSchemeGroupButtons() =
    onView(
        allOf(
            withId(R.id.mozac_feature_readerview_color_scheme_group),
            withParent(withId(R.id.readerViewBar)),
        ),
    )

private fun lightColorButton() =
    onView(
        allOf(
            withText("Light"),
            withParent(withId(R.id.mozac_feature_readerview_color_scheme_group)),
        ),
    )

private fun sepiaColorButton() =
    onView(
        allOf(
            withText("Sepia"),
            withParent(withId(R.id.mozac_feature_readerview_color_scheme_group)),
        ),
    )

private fun darkColorButton() =
    onView(
        allOf(
            withText("Dark"),
            withParent(withId(R.id.mozac_feature_readerview_color_scheme_group)),
        ),
    )

private fun assertAppearanceButtonExists() {
    mDevice.findObject(UiSelector().resourceId("$packageName:id/readerViewAppearanceButton"))
        .waitForExists(waitingTime)
    appearanceButton().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
}

private fun assertAppearanceButtonDoesntExists() {
    mDevice.findObject(UiSelector().resourceId("$packageName:id/readerViewAppearanceButton"))
        .waitUntilGone(waitingTime)
    appearanceButton().check((matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE))))
}

private fun assertAppearanceMenu() {
    mDevice.waitForIdle()
    mDevice.findObject(UiSelector().resourceId("$packageName:id/readerViewBar")).waitForExists(waitingTime)
    appearanceMenu().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
}

private fun assertFontGroupButtons() {
    mDevice.findObject(
        UiSelector().resourceId("$packageName:id/:id/mozac_feature_readerview_font_group"),
    ).waitForExists(waitingTime)
    fontGroupButtons().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
}

private fun assertIncreaseFontSizeButton() {
    mDevice.findObject(
        UiSelector().resourceId("$packageName:id/mozac_feature_readerview_font_size_increase"),
    ).waitForExists(waitingTime)
    increaseFontSizeButton().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
}

private fun assertDecreaseFontSizeButton() {
    mDevice.findObject(
        UiSelector().resourceId("$packageName:id/mozac_feature_readerview_font_size_decrease"),
    ).waitForExists(waitingTime)
    decreaseFontSizeButton().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
}

private fun assertColorSchemeGroupButtons() {
    mDevice.findObject(
        UiSelector().resourceId("$packageName:id/mozac_feature_readerview_color_scheme_group"),
    ).waitForExists(waitingTime)
    colorSchemeGroupButtons().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
}
