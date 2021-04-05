/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasSibling
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.uiautomator.UiSelector
import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime
import org.mozilla.reference.browser.helpers.TestHelper.packageName

/**
 * Implementation of Robot Pattern for the addons manager.
 */
class AddonsManagerRobot {

    fun verifyAddonsRecommendedView() = assertAddonsRecommendedView()
    fun verifyAddonsEnabledView() = assertAddonsEnabledView()
    fun verifyInstallAddonPrompt(addonName: String) = assertAddonPrompt(addonName)
    fun verifyAddonDownloadCompletedPrompt(addonName: String) = assertAddonDownloadCompletedPrompt(addonName)
    fun verifyAddonElementsView(addonName: String) = assertAddonElementsView(addonName)
    fun clickInstallAddonButton(addonName: String) = selectInstallAddonButton(addonName)
    fun clickCancelInstallButton() = cancelInstallButton()
    fun clickAllowInstallAddonButton() = allowInstallAddonButton()
    fun waitForAddonDownloadComplete() = waitForDownloadProgressUntilGone()
    fun openAddon(addonName: String) {
        mDevice.waitForIdle()
        verifyAddonsEnabledView()
        onView(
            allOf(
                withId(R.id.add_on_name),
                withText(addonName)))
            .check(matches(isCompletelyDisplayed()))
            .perform(click())
    }

    class Transition {
        fun dismissAddonDownloadCompletedPrompt(interact: AddonsManagerRobot.() -> Unit): AddonsManagerRobot.Transition {
            mDevice.pressBack()
            AddonsManagerRobot().interact()
            return AddonsManagerRobot.Transition()
        }
    }

    private fun assertAddonsRecommendedView() {
        assertTrue(mDevice.findObject(UiSelector().text("Recommended")).waitForExists(waitingTime))
        // Check uBlock is displayed in the "Recommended" section
        onView(
            allOf(
                withId(R.id.add_on_item),
                    hasDescendant(withText("uBlock Origin")),
                    hasDescendant(withText("Finally, an efficient wide-spectrum content blocker. Easy on CPU and memory.")),
                    hasDescendant(withId(R.id.rating)),
                    hasDescendant(withId(R.id.users_count)),
                    hasDescendant(withId(R.id.add_button))
            )
        ).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    private fun assertAddonsEnabledView() {
        assertTrue(mDevice.findObject(UiSelector().text("Enabled")).waitForExists(waitingTime))
        // Check uBlock is displayed in the "Enabled" section
        onView(
            allOf(
                withId(R.id.add_on_item),
                hasDescendant(withText("uBlock Origin")),
                hasDescendant(withText("Finally, an efficient wide-spectrum content blocker. Easy on CPU and memory.")),
                hasDescendant(withId(R.id.rating)),
                hasDescendant(withId(R.id.users_count)),
                hasDescendant(withId(R.id.add_button))
            )
        ).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    private fun installAddonButton(addonName: String) =
        onView(
            allOf(
                withContentDescription(R.string.mozac_feature_addons_install_addon_content_description),
                isDescendantOfA(withId(R.id.add_on_item)),
                hasSibling(hasDescendant(withText(addonName)))
            )
        )

    private fun selectInstallAddonButton(addonName: String) {
        mDevice.waitForIdle()
        mDevice.findObject(UiSelector().textContains(addonName))
            .waitForExists(waitingTime)

        installAddonButton(addonName)
            .check(matches(isCompletelyDisplayed()))
            .perform(click())
    }

    private fun assertAddonPrompt(addonName: String) {
        mDevice.waitForIdle()
        mDevice.findObject(UiSelector()
            .resourceId("$packageName:id/title"))
            .waitForExists(waitingTime)

        assertTrue(mDevice.findObject(UiSelector().textContains("Add $addonName?"))
            .waitForExists(waitingTime))

        onView(
            allOf(
                withId(R.id.permissions),
                withText(containsString("It requires your permission to:"))
            )
        ).check(matches(isCompletelyDisplayed()))

        onView(
            allOf(
                withId(R.id.allow_button),
                withText(R.string.mozac_feature_addons_permissions_dialog_add)))
            .check(matches(isCompletelyDisplayed()))

        onView(
            allOf(
                withId(R.id.deny_button),
                withText(R.string.mozac_feature_addons_permissions_dialog_cancel)))
            .check(matches(isCompletelyDisplayed()))
    }

    private fun cancelInstallButton() {
        onView(
            allOf(
                withId(R.id.deny_button),
                withText(R.string.mozac_feature_addons_permissions_dialog_cancel)))
            .check(matches(isCompletelyDisplayed()))
            .perform(click())
    }

    private fun allowInstallAddonButton() {
        onView(
            allOf(
                withId(R.id.allow_button),
                withText(R.string.mozac_feature_addons_permissions_dialog_add)))
            .check(matches(isCompletelyDisplayed()))
            .perform(click())
    }

    private fun assertAddonDownloadCompletedPrompt(addonName: String) {
        mDevice.waitForIdle()
        assertTrue(mDevice.findObject(UiSelector()
            .textContains("$addonName has been added to Reference Browser"))
            .waitForExists(waitingTime))
    }

    private fun waitForDownloadProgressUntilGone() {
        mDevice.waitForIdle()
        mDevice.findObject(UiSelector().resourceId("$packageName:id/addonProgressOverlay"))
            .waitUntilGone(waitingTime)
    }

    private fun assertAddonElementsView(addonName: String) {
        mDevice.waitForIdle()
        mDevice.findObject(UiSelector().textContains(addonName)).waitForExists(waitingTime)

        onView(
            allOf(
                withId(R.id.remove_add_on),
                hasSibling(withId(R.id.enable_switch)),
                hasSibling(withId(R.id.settings)),
                hasSibling(withId(R.id.details)),
                hasSibling(withId(R.id.permissions)),
                hasSibling(withId(R.id.allow_in_private_browsing_switch))
            )
        ).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }
}
