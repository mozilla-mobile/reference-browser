/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser

import android.content.Context
import android.util.AttributeSet
import mozilla.components.browser.session.SessionManager
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.concept.engine.Engine
import mozilla.components.concept.fetch.Client
import mozilla.components.feature.addons.AddonManager
import mozilla.components.feature.addons.update.DefaultAddonUpdater
import mozilla.components.service.fxa.manager.FxaAccountManager
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.stubbing.Stubber
import org.mozilla.reference.browser.components.Analytics
import org.mozilla.reference.browser.components.BackgroundServices
import org.mozilla.reference.browser.components.Core
import org.mozilla.reference.browser.components.TestEngineView

class TestBrowserApplication : BrowserApplication() {

    private val realComponents = Components(this)

    // Mocking Core
    private val engine = mock(Engine::class.java).also {
        `when`(it.createView(any(Context::class.java) ?: this, any(AttributeSet::class.java)))
                .then { invocation ->
                    val context = invocation.arguments[0] as Context
                    val attrs = invocation.arguments[1] as AttributeSet
                    return@then TestEngineView(context, attrs)
                }
    }

    private val core = mock(Core::class.java).also {
        doReturnMock(AddonManager::class.java).`when`(it).addonManager
        doReturnMock(DefaultAddonUpdater::class.java).`when`(it).addonUpdater
        doReturn(engine).`when`(it).engine
        doReturn(SessionManager(engine)).`when`(it).sessionManager
        doReturnMock(Client::class.java).`when`(it).client
        doReturn(BrowserStore()).`when`(it).store
    }

    private val backgroundServices = mock(BackgroundServices::class.java).also {
        doReturnMock(FxaAccountManager::class.java).`when`(it).accountManager
    }

    // Mocking Analytics
    private val analytics = mock(Analytics::class.java)

    // Finally, override the components
    override val components: Components
        get() = spy(realComponents).also {
            doReturn(core).`when`(it).core
            doReturn(backgroundServices).`when`(it).backgroundServices
            doReturn(analytics).`when`(it).analytics
        }
}

private fun <T> doReturnMock(clazz: Class<T>): Stubber = doReturn(mock(clazz))