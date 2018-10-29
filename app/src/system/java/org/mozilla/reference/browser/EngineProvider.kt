/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.content.Context
import mozilla.components.browser.engine.system.SystemEngine
import mozilla.components.concept.engine.DefaultSettings
import mozilla.components.concept.engine.Engine

object EngineProvider {
    fun getEngine(context: Context, defaultSettings: DefaultSettings): Engine {
        return SystemEngine(context, defaultSettings)
    }
}
