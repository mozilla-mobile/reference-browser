package org.mozilla.reference.browser.media

import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.media.service.AbstractMediaSessionService
import org.mozilla.reference.browser.ext.components

/**
 * [AbstractMediaSessionService] implementation for injecting [BrowserStore] singleton.
 */
class MediaSessionService : AbstractMediaSessionService() {
    override val store: BrowserStore by lazy { components.core.store }
}
