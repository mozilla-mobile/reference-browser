/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.helpers

import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches

fun ViewInteraction.click(): ViewInteraction = this.perform(ViewActions.click())!!

fun ViewInteraction.assertIsEnabled(isEnabled: Boolean): ViewInteraction = this.check(matches(isEnabled(isEnabled)))!!

fun ViewInteraction.assertIsChecked(isChecked: Boolean): ViewInteraction = this.check(matches(isChecked(isChecked)))!!

fun ViewInteraction.assertIsSelected(isSelected: Boolean): ViewInteraction =
    this.check(matches(isSelected(isSelected)))!!
