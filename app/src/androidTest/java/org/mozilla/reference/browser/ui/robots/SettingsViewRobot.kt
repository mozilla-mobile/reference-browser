/* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0. If a copy of the MPL was not distributed with this
* file, You can obtain one at http://mozilla.org/MPL/2.0/. */

@file:Suppress("TooManyFunctions")

package org.mozilla.reference.browser.ui.robots

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import org.hamcrest.CoreMatchers.allOf
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.helpers.TestAssetHelper
import org.mozilla.reference.browser.helpers.TestAssetHelper.waitingTime
import org.mozilla.reference.browser.helpers.TestHelper
import org.mozilla.reference.browser.helpers.TestHelper.packageName
import org.mozilla.reference.browser.helpers.click
import org.mozilla.reference.browser.helpers.hasCousin

/**
 * Implementation of Robot Pattern for the settings menu.
 */
class SettingsViewRobot {
    fun verifySettingsViewExists() = assertSettingsView()

    fun verifyNavigateUp() = assertNavigateUpButton()
    fun verifySyncSigninButton() = assertSyncSigninButton()
    fun verifySyncHistorySummary() = assertSyncHistorySummary()
    fun verifySyncQrCodeButton() = assertSyncQrCodeButton()
    fun verifySyncQrSummary() = assertSyncQrSummary()
    fun verifyPrivacyButton() = assertPrivacyButton()
    fun verifyPrivacySummary() = assertPrivacySummary()
    fun verifyOpenLinksInApps() = assertOpenLinksInApps()
    fun verifyMakeDefaultBrowserButton() = assertMakeDefaultBrowserButton()
    fun verifyAutofillAppsButton() = assertAutofillAppsButton()
    fun varifyAutofillAppsSummary() = assertAutofillAppsSummary()
    fun verifyJetpackComposeButton() = assertJetpackComposeButton()
    fun verifyDeveloperToolsHeading() = assertDeveloperToolsHeading()
    fun verifyRemoteDebugging() = assertRemoteDebugging()
    fun verifyCustomAddonCollectionButton() = assertCustomAddonCollectionButton()
    fun verifyMozillaHeading() = assertMozillaHeading()
    fun verifyAboutReferenceBrowserButton() = assertAboutReferenceBrowserButton()
    fun verifySettingsRecyclerViewToExist() = waitForSettingsRecyclerViewToExist()

    fun clickCustomAddonCollectionButton() = customAddonCollectionButton().click()
    fun verifyCustomAddonCollectionPanelExist() = assertCustomAddonCollectionPanel()

    fun clickOpenLinksInApps() = openLinksInAppsToggle().click()

    // toggleRemoteDebugging does not yet verify that the debug service is started
    // server runs on port 6000
    fun toggleRemoteDebuggingOn() = {
        Espresso.onView(withText("OFF")).check(matches(isDisplayed()))
        remoteDebuggingToggle().click()
        Espresso.onView(withText("ON")).check(matches(isDisplayed()))
    }

    fun toggleRemoteDebuggingOff() = {
        Espresso.onView(withText("ON")).check(matches(isDisplayed()))
        remoteDebuggingToggle().click()
        Espresso.onView(withText("OFF")).check(matches(isDisplayed()))
    }

    class Transition {
        fun openSettingsViewPrivacy(interact: SettingsViewPrivacyRobot.() -> Unit):
            SettingsViewPrivacyRobot.Transition {
            privacyButton().click()
            SettingsViewPrivacyRobot().interact()
            return SettingsViewPrivacyRobot.Transition()
        }

        fun openFXASignin(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            syncSigninButton().click()
            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

        fun openFXAQrCode(interact: ExternalAppsRobot.() -> Unit): ExternalAppsRobot.Transition {
            syncQrCodeButton().click()
            ExternalAppsRobot().interact()
            return ExternalAppsRobot.Transition()
        }

        fun makeDefaultBrowser(interact: ExternalAppsRobot.() -> Unit):
            ExternalAppsRobot.Transition {
            makeDefaultBrowserButton().click()
            ExternalAppsRobot().interact()
            return ExternalAppsRobot.Transition()
        }

        fun clickAutofillAppsButton(interact: ExternalAppsRobot.() -> Unit):
            ExternalAppsRobot.Transition {
            autofillAppsButton().click()
            ExternalAppsRobot().interact()
            return ExternalAppsRobot.Transition()
        }

        fun openAboutReferenceBrowser(interact: BrowserRobot.() -> Unit): BrowserRobot.Transition {
            aboutReferenceBrowserButton().click()
            BrowserRobot().interact()
            return BrowserRobot.Transition()
        }

        fun goBack(interact: NavigationToolbarRobot.() -> Unit): NavigationToolbarRobot.Transition {
            val navigateUpButton = mDevice.findObject(UiSelector().descriptionContains("Navigate up"))
            navigateUpButton.clickAndWaitForNewWindow()

            NavigationToolbarRobot().interact()
            return NavigationToolbarRobot.Transition()
        }
    }
}

private fun waitForSettingsRecyclerViewToExist() {
    mDevice.findObject(UiSelector().resourceId("$packageName:id/recycler_view"))
        .waitForExists(
            waitingTime,
        )
}

private fun assertSettingsView() {
    // verify that we are in the correct settings view
    Espresso.onView(withText(R.string.settings))
    Espresso.onView(withText(R.string.preferences_about_page))
}

private fun syncSigninButton() = Espresso.onView(withText(R.string.sign_in))
private fun syncHistorySummary() = Espresso.onView(withText(R.string.preferences_sign_in_summary))
private fun syncQrCodeButton() = Espresso.onView(withText(R.string.pair_sign_in))
private fun syncQrSummary() = Espresso.onView(withText(R.string.preferences_pair_sign_in_summary))
private fun privacyButton() = Espresso.onView(withText(R.string.privacy))
private fun privacySummary() = Espresso.onView(withText(R.string.preferences_privacy_summary))
private fun openLinksInAppsToggle() = Espresso.onView(allOf(withId(R.id.switchWidget), hasCousin(withText(R.string.open_links_in_apps))))
private fun makeDefaultBrowserButton() = Espresso.onView(withText(R.string.preferences_make_default_browser))
private fun autofillAppsButton() = onView(withText("Autofill apps"))
private fun jetpackComposeButton() = onView(withText("Use experimental Jetpack Compose UI"))
private fun autofillAppsSummary() = onView(withText("Autofill logins and passwords in other apps"))
private fun developerToolsHeading() = Espresso.onView(withText(R.string.developer_tools_category))
private fun remoteDebuggingToggle() = Espresso.onView(allOf(withId(R.id.switchWidget), hasCousin(withText(R.string.preferences_remote_debugging))))
private fun customAddonCollectionButton() = onView(withText("Custom Add-on collection"))
private fun mozillaHeading() = Espresso.onView(withText(R.string.mozilla_category))
private fun aboutReferenceBrowserButton() = Espresso.onView(withText(R.string.preferences_about_page))

private fun assertNavigateUpButton() {
    mDevice.wait(Until.findObject(By.text("Navigate up")), TestAssetHelper.waitingTimeShort)
}
private fun assertSyncSigninButton() = syncSigninButton()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSyncHistorySummary() = syncHistorySummary()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSyncQrCodeButton() = syncQrCodeButton()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertSyncQrSummary() = syncQrSummary()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertPrivacyButton() = privacyButton()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertPrivacySummary() = privacySummary()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertOpenLinksInApps() = openLinksInAppsToggle()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertMakeDefaultBrowserButton() = makeDefaultBrowserButton()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertAutofillAppsButton() = autofillAppsButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertAutofillAppsSummary() = autofillAppsSummary()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertJetpackComposeButton() = jetpackComposeButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertDeveloperToolsHeading() = developerToolsHeading()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertRemoteDebugging() = remoteDebuggingToggle()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertCustomAddonCollectionButton() = customAddonCollectionButton()
    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertMozillaHeading() {
    TestHelper.scrollToElementByText("About Reference Browser")
    mozillaHeading().check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
}
private fun assertAboutReferenceBrowserButton() = aboutReferenceBrowserButton()
    .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
private fun assertCustomAddonCollectionPanel() {
    mDevice.waitForIdle()
    mDevice.findObject(UiSelector().resourceId("$packageName:id/parentPanel"))
        .waitForExists(waitingTime)
    onView(
        allOf(
            withText(R.string.preferences_customize_amo_collection),
            isDescendantOfA(withId(R.id.title_template)),
        ),
    ).check(matches(isCompletelyDisplayed()))
}
