package com.cliqz.jsengine

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.facebook.react.ReactInstanceManager
import com.facebook.react.ReactRootView
import com.facebook.react.bridge.ReactContext
import com.facebook.react.common.LifecycleState
import com.facebook.react.modules.storage.ReactDatabaseSupplier
import com.facebook.react.shell.MainReactPackage
import mozilla.components.concept.awesomebar.AwesomeBar
import org.mozilla.reference.browser.BuildConfig
import org.mozilla.reference.browser.ext.application

/**
 * @author Sam Macbeth
 */
class Search(context: Context, attrs: AttributeSet? = null) :
        ReactInstanceManager.ReactInstanceEventListener, AwesomeBar, ReactRootView(context, attrs) {

    var context : ReactContext? = null
    val newReactInstanceManager = ReactInstanceManager.builder()
            .setApplication(context.application)
            .setBundleAssetName("search.bundle.js")
            .setJSMainModulePath("index")
            .addPackage(MainReactPackage())
            .addPackage(BridgePackage())
            .setUseDeveloperSupport(BuildConfig.DEBUG)
            .setInitialLifecycleState(LifecycleState.RESUMED)
            .build()

    init {
        val size = 50L * 1024L * 1024L
        ReactDatabaseSupplier.getInstance(context).setMaximumSize(size)
        newReactInstanceManager.addReactInstanceEventListener(this)
        newReactInstanceManager.createReactContextInBackground()
        startReactApplication(newReactInstanceManager, "ExtensionApp", null)
    }

    override fun onReactContextInitialized(context: ReactContext?) {
        this.context = context;
    }

    override fun addProviders(vararg providers: AwesomeBar.SuggestionProvider) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onInputChanged(text: String) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeAllProviders() {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeProviders(vararg providers: AwesomeBar.SuggestionProvider) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setOnStopListener(listener: () -> Unit) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun asView(): View {
        return this
    }

    override fun onInputCancelled() {
        super.onInputCancelled()
    }

    override fun onInputStarted() {
        super.onInputStarted()
    }
}