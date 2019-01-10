package org.mozilla.reference.browser.ui.robots


class NavigationRobot {

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    fun goForward() = forwardButton().click()
    fun reload() = reloadButton().click()
    fun openSettings() = settingsButton().click()

    fun assertCanGoForward(canGoForward: Boolean) = forwardButton().assertIsEnabled(canGoForward)
    fun assertCanReload(canReload: Boolean) = reloadButton().assertIsEnabled(canReload)
    fun assertURLBarTextContains(expectedText: String) = urlBar().check(matches(withText(containsString(expectedText))))
    fun assertDesktopModeEnabled(desktopModeEnabled: Boolean) = desktopModeButton().assertIsEnabled(desktopModeEnabled)


    fun openSettings(interact: SettingsRobot.() -> Unit): SettingsRobot.Transition {
        settingsButton().click()

        SettingsRobot().interact()
        return SettingsRobot.Transition()
    }
}