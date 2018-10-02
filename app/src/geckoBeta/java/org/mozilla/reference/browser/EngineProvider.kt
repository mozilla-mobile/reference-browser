package org.mozilla.reference.browser/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import android.content.Context
import mozilla.components.browser.engine.gecko.GeckoEngine
import mozilla.components.concept.engine.DefaultSettings
import mozilla.components.concept.engine.Engine
import org.mozilla.geckoview.GeckoRuntime

object EngineProvider {
    fun getEngine(context: Context, defaultSettings: DefaultSettings): Engine {
        val runtime = GeckoRuntime.getDefault(context)
        return GeckoEngine(runtime, defaultSettings)
    }
}
