/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@file:Suppress("TooManyFunctions")

package org.mozilla.fenix.ui.robots

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiSelector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime
import org.mozilla.reference.browser.helpers.TestHelper.packageName
import org.mozilla.reference.browser.ui.robots.mDevice

/**
 * Implementation of Robot Pattern for Reader View UI.
 */
class ReaderViewRobot {

    fun verifyAppearanceButton() = assertAppearanceButtonExists()
    fun verifyAppearanceButtonDoesntExist() = assertAppearanceButtonDoesntExist()
    fun openReaderViewAppearance() = appearanceButton().click()
    fun verifyAppearanceControlsMenu() = assertAppearanceMenuExists()
    fun verifyAppearanceMenuDoesntExist() = assertAppearanceMenuDoesntExist()
    fun verifyFontGroupButtons() = assertFontGroupButtons()
    fun verifyIncreaseFontSizeButton() = assertIncreaseFontSizeButton()
    fun verifyDecreaseFontSizeButton() = assertDecreaseFontSizeButton()
    fun verifyColorSchemeGroupButtons() = assertColorSchemeGroupButtons()
    fun clickSansSerifButton() = sansSerifButton().click()
    fun clickSerifButton() = serifButton().click()
    fun clickIncreaseFontSizeButton() = increaseFontSizeButton().click()
    fun clickDecreaseFontSizeButton() = decreaseFontSizeButton().click()
    fun clickDarkColorSchemeButton() = darkColorSchemeButton().click()
    fun clickSepiaColorSchemeButton() = sepiaColorSchemeButton().click()
    fun clickLightColorSchemeButton() = lightColorSchemeButton().click()

    fun verifyAppearanceFont(fontType: String) {
        val fontTypeKey: String = "mozac-readerview-fonttype"

        val prefs = InstrumentationRegistry.getInstrumentation()
            .targetContext.getSharedPreferences(
                "mozac_feature_reader_view",
                Context.MODE_PRIVATE
            )

        assertEquals(fontType, prefs.getString(fontTypeKey, ""))
    }

    fun verifyAppearanceFontSize(expectedFontSize: Int) {
        val fontSizeKey: String = "mozac-readerview-fontsize"

        val prefs = InstrumentationRegistry.getInstrumentation()
            .targetContext.getSharedPreferences(
                "mozac_feature_reader_view",
                Context.MODE_PRIVATE
            )

        val fontSizeKeyValue = prefs.getInt(fontSizeKey, 3)

        assertEquals(expectedFontSize, fontSizeKeyValue)
    }

    fun verifyAppearanceColorScheme(expectedColorScheme: String) {
        val colorSchemeKey: String = "mozac-readerview-colorscheme"

        val prefs = InstrumentationRegistry.getInstrumentation()
            .targetContext.getSharedPreferences(
                "mozac_feature_reader_view",
                Context.MODE_PRIVATE
            )

        assertEquals(expectedColorScheme, prefs.getString(colorSchemeKey, ""))
    }

    class Transition {
        fun dismissAppearanceControlsMenu(interact: ReaderViewRobot.() -> Unit): ReaderViewRobot.Transition {
            mDevice.pressBack()

            ReaderViewRobot().interact()
            return ReaderViewRobot.Transition()
        }
    }
}

private fun appearanceButton() =
    mDevice.findObject(UiSelector().resourceId("$packageName:id/readerViewAppearanceButton"))

private fun sansSerifButton() =
    mDevice.findObject(
        UiSelector().resourceId("$packageName:id/mozac_feature_readerview_font_sans_serif")
    )

private fun serifButton() =
    mDevice.findObject(
        UiSelector().resourceId("$packageName:id/mozac_feature_readerview_font_serif")
    )

private fun increaseFontSizeButton() =
    mDevice.findObject(
        UiSelector().resourceId("$packageName:id/mozac_feature_readerview_font_size_increase")
    )

private fun decreaseFontSizeButton() =
    mDevice.findObject(
        UiSelector().resourceId("$packageName:id/mozac_feature_readerview_font_size_decrease")
    )

private fun darkColorSchemeButton() =
    mDevice.findObject(
        UiSelector().resourceId("$packageName:id/mozac_feature_readerview_color_dark")
    )

private fun sepiaColorSchemeButton() =
    mDevice.findObject(
        UiSelector().resourceId("$packageName:id/mozac_feature_readerview_color_sepia")
    )

private fun lightColorSchemeButton() =
    mDevice.findObject(
        UiSelector().resourceId("$packageName:id/mozac_feature_readerview_color_light")
    )

private fun assertAppearanceButtonExists() =
        assertTrue(
            mDevice.findObject(UiSelector().resourceId("$packageName:id/readerViewAppearanceButton"))
                .waitForExists(waitingTime)
        )

private fun assertAppearanceButtonDoesntExist() =
    assertFalse(
        mDevice.findObject(UiSelector().resourceId("$packageName:id/readerViewAppearanceButton"))
            .waitForExists(waitingTime)
    )

private fun assertAppearanceMenuExists() {
    mDevice.waitForIdle()
    assertTrue(
        mDevice.findObject(UiSelector().resourceId("$packageName:id/readerViewBar"))
            .waitForExists(waitingTime)
    )
}

private fun assertAppearanceMenuDoesntExist() = assertFalse(
    mDevice.findObject(UiSelector().resourceId("$packageName:id/readerViewBar"))
        .waitForExists(waitingTime)
)

private fun assertFontGroupButtons() =
    assertTrue(
        mDevice.findObject(UiSelector().resourceId("$packageName:id/mozac_feature_readerview_font_group"))
            .waitForExists(waitingTime)
    )

private fun assertIncreaseFontSizeButton() =
    assertTrue(
        mDevice.findObject(UiSelector().resourceId("$packageName:id/mozac_feature_readerview_font_size_increase"))
            .waitForExists(waitingTime)
    )

private fun assertDecreaseFontSizeButton() =
    assertTrue(
        mDevice.findObject(UiSelector().resourceId("$packageName:id/mozac_feature_readerview_font_size_decrease"))
            .waitForExists(waitingTime)
    )

private fun assertColorSchemeGroupButtons() =
    assertTrue(
        mDevice.findObject(UiSelector().resourceId("$packageName:id/mozac_feature_readerview_color_scheme_group"))
            .waitForExists(waitingTime)
    )
