/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

// This is, roughly, an "auto-complete aggregator" - it first tries to find a match via "places",
// and if that fails, falls back to the "domain" provider.
package org.mozilla.reference.browser.browser

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.experimental.launch
import mozilla.components.browser.domains.DomainAutoCompleteProvider
import mozilla.components.browser.toolbar.BrowserToolbar
import mozilla.components.support.base.log.Log
import mozilla.components.ui.autocomplete.InlineAutocompleteEditText
import java.net.URL
import org.mozilla.places.PlacesConnection


fun debug(message: String) {
    Log.log(tag="BrowserAutocomplete", message=message);
}

class BrowserAutocompleteProvider (
        context: Context,
        toolbar: BrowserToolbar,
        sessionId: String? = null) {

    private val domainAutoCompleteProvider = DomainAutoCompleteProvider().apply {
        initialize(context)
    }

    // XXX - this should be in the "profile" dir, but I'm not sure how to fetch that.
    private val placesConnection = PlacesConnection(context.getExternalFilesDir(null).absolutePath + "/places.sqlite", "")

    init {
        toolbar.setAutocompleteFilter { value, view ->
            view?.let { _ ->
                // use a coroutine to do this off the main thread.
                launch {
                    // First try places.
                    // XXX - given the inlineautocomplete functionality, we eventually want to perform
                    // an OriginOrUrl search - however, for now, we just perform a "normal" search,
                    // then iterate the results until we find one that matches at the start.
                    debug("starting places search for $value")
                    val placesResults = placesConnection.queryAutocomplete(value)
                    debug("places search for $value, gave ${placesResults.size} matches")

                    // Iterate all results and find something that matches at the start.
                    var finalResult: String? = null
                    var finalSource: String? = null
                    var finalSize: Int? = 0;
                    for (r in placesResults) {
                        var h = URL(r.url).host;
                        if (h.startsWith("www.")) {
                            h = h.substring(4)
                        }
                        if (h.startsWith(value)) {
                            debug("places found matching result $h")
                            finalResult = h
                            finalSource = "places"
                            finalSize = placesResults.size
                        }
                    }
                    if (finalResult == null) {
                        debug("places found no matching result - trying domain provider")
                        val result = domainAutoCompleteProvider.autocomplete(value)
                        finalResult = result.text
                        finalSource = result.source
                        finalSize = result.size
                    }
                    // domainAutoCompleteProvider always provides a result, even if it is empty strings.
                    val activity = view.context as Activity
                    activity.runOnUiThread {
                        view.applyAutocompleteResult(
                                InlineAutocompleteEditText.AutocompleteResult(finalResult, finalSource!!, finalSize!!)
                        )
                    }
                }
            }
        }
    }
}
