/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.content.Context
import mozilla.components.browser.engine.gecko.GeckoEngine
import mozilla.components.concept.engine.DefaultSettings
import mozilla.components.concept.engine.Engine
import mozilla.components.lib.crash.handler.CrashHandlerService
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings

object EngineProvider {
    fun getEngine(context: Context, defaultSettings: DefaultSettings): Engine {

        val settings = GeckoRuntimeSettings.Builder()
                .crashHandler(CrashHandlerService::class.java)
                .build()

        // Crashes of this runtime will be forwarded to the crash reporter component
        val runtime = GeckoRuntime.create(context, settings)

        return GeckoEngine(context, defaultSettings, runtime)
    }
}
