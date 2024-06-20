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
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.junit.Assert.assertTrue
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.waitAndInteract
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTimeShort
import org.mozilla.reference.browser.helpers.TestHelper.assertUIObjectExists
import org.mozilla.reference.browser.helpers.TestHelper.getStringResource
import org.mozilla.reference.browser.helpers.TestHelper.itemWithResId
import org.mozilla.reference.browser.helpers.TestHelper.itemWithResIdContainingText
import org.mozilla.reference.browser.helpers.TestHelper.packageName
import org.mozilla.reference.browser.helpers.click

/**
 * Implementation of Robot Pattern for the addons manager.
 */
class AddonsManagerRobot {

    fun verifyAddonsRecommendedView() {
        assertTrue(mDevice.findObject(UiSelector().text("Recommended")).waitForExists(waitingTime))
        // Check uBlock is displayed in the "Recommended" section
        assertUIObjectExists(
            itemWithResIdContainingText("$packageName:id/add_on_name", "uBlock Origin"),
            itemWithResIdContainingText(
                "$packageName:id/add_on_description",
                "Finally, an efficient wide-spectrum content blocker. Easy on CPU and memory.",
            ),
        )
        assertUIObjectExists(
            itemWithResId("$packageName:id/rating"),
            itemWithResId("$packageName:id/review_count"),
            itemWithResId("$packageName:id/add_button"),
        )
    }

    fun verifyInstallAddonPrompt(addonName: String) = assertAddonPrompt(addonName)
    fun verifyAddonDownloadCompletedPrompt(addonName: String) = assertAddonDownloadCompletedPrompt(addonName)
    fun verifyAddonElementsView(addonName: String) = assertAddonElementsView(addonName)
    fun clickInstallAddonButton(addonName: String) = selectInstallAddonButton(addonName)
    fun clickCancelInstallButton() = cancelInstallButton()
    fun clickAllowInstallAddonButton() = allowInstallAddonButton()
    fun waitForAddonDownloadComplete() = waitForDownloadProgressUntilGone()
    fun openAddon(addonName: String) =
        itemWithResIdContainingText("$packageName:id/add_on_name", addonName)
            .also {
                it.waitForExists(waitingTimeShort)
                it.clickAndWaitForNewWindow()
            }

    fun dismissAddonDownloadCompletedPrompt(addonName: String) {
        mDevice.waitForWindowUpdate(packageName, waitingTime)
        mDevice.findObject(UiSelector().text("$addonName has been added to Reference Browser"))
            .waitForExists(waitingTime)
        mDevice.waitAndInteract(
            Until.findObject(
                By.text(
                    getStringResource(R.string.mozac_feature_addons_installed_dialog_okay_button_2),
                ),
            ),
        ) {}
        mDevice.findObject(
            UiSelector().textContains(
                getStringResource(R.string.mozac_feature_addons_installed_dialog_okay_button_2),
            ),
        ).click()
    }

    fun clickRemoveAddonButton() {
        removeAddonButton().click()
        mDevice.waitForIdle()
    }

    class Transition {
        fun addonsManagerRobot(interact: AddonsManagerRobot.() -> Unit): AddonsManagerRobot.Transition {
            AddonsManagerRobot().interact()
            return AddonsManagerRobot.Transition()
        }
    }

    private fun installAddonButton(addonName: String) =
        onView(
            allOf(
                withContentDescription("Install $addonName"),
                isDescendantOfA(withId(R.id.add_on_item)),
                hasSibling(hasDescendant(withText(addonName))),
            ),
        )

    private fun removeAddonButton() = onView(withId(R.id.remove_add_on))

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
        mDevice.findObject(
            UiSelector()
                .resourceId("$packageName:id/title"),
        )
            .waitForExists(waitingTime)

        assertTrue(
            mDevice.findObject(UiSelector().textContains("Add $addonName?"))
                .waitForExists(waitingTime),
        )

        onView(
            allOf(
                withId(R.id.optional_or_required_text),
                withText(containsString("It requires your permission to:")),
            ),
        ).check(matches(isCompletelyDisplayed()))

        onView(
            allOf(
                withId(R.id.allow_button),
                withText(R.string.mozac_feature_addons_permissions_dialog_add),
            ),
        )
            .check(matches(isCompletelyDisplayed()))

        onView(
            allOf(
                withId(R.id.deny_button),
                withText(R.string.mozac_feature_addons_permissions_dialog_cancel),
            ),
        )
            .check(matches(isCompletelyDisplayed()))
    }

    private fun cancelInstallButton() {
        onView(
            allOf(
                withId(R.id.deny_button),
                withText(R.string.mozac_feature_addons_permissions_dialog_cancel),
            ),
        )
            .check(matches(isCompletelyDisplayed()))
            .perform(click())
    }

    private fun allowInstallAddonButton() {
        assertTrue(mDevice.findObject(UiSelector().text("Add")).waitForExists(waitingTime))

        onView(
            allOf(
                withId(R.id.allow_button),
                withText(R.string.mozac_feature_addons_permissions_dialog_add),
            ),
        )
            .check(matches(isCompletelyDisplayed()))
            .perform(click())
    }

    private fun assertAddonDownloadCompletedPrompt(addonName: String) {
        mDevice.waitForIdle()
        assertTrue(
            mDevice.findObject(
                UiSelector()
                    .textContains("$addonName has been added to Reference Browser"),
            )
                .waitForExists(waitingTime),
        )
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
                hasSibling(withId(R.id.allow_in_private_browsing_switch)),
            ),
        ).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }
}
