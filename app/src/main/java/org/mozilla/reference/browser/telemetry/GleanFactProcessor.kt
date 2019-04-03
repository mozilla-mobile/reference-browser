/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.telemetry

import mozilla.components.support.base.Component
import mozilla.components.support.base.facts.Action
import mozilla.components.support.base.facts.Fact
import mozilla.components.support.base.facts.FactProcessor
import org.mozilla.reference.browser.GleanMetrics.ToolbarEvents

class GleanFactProcessor : FactProcessor {
    /**
     * If the given [Fact] is relevant to a glean metric, use it to trigger a record.
     */
    override fun process(fact: Fact) {
        // Check for URL Committed event from the toolbar component
        if (fact.component == Component.BROWSER_TOOLBAR &&
            fact.action == Action.COMMIT &&
            fact.item == "toolbar"
        ) {
            ToolbarEvents.urlCommitted.record()
        }
    }
}
