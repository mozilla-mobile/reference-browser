/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.browser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import mozilla.components.browser.thumbnails.BrowserThumbnails
import mozilla.components.concept.engine.EngineView
import mozilla.components.support.base.feature.ViewBoundFeatureWrapper
import mozilla.components.support.base.log.logger.Logger
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.requireComponents

/**
 * Fragment used for browsing the web within the main app.
 */
class BrowserFragment : BaseBrowserFragment() {

    private val logger = Logger("BrowserFragment")
    private val mainContainerViewModel: MainContainerViewModel by viewModels(
        ownerProducer = { requireParentFragment().requireParentFragment() },
    )

    private val thumbnailsFeature = ViewBoundFeatureWrapper<BrowserThumbnails>()
    private val engineView: EngineView
        get() = requireView().findViewById<View>(R.id.engineView) as EngineView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_browser, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        thumbnailsFeature.set(
            feature = BrowserThumbnails(
                requireContext(),
                engineView,
                requireComponents.core.store,
            ),
            owner = this,
            view = view,
        )

        val toolbarMaxHeight: Int = resources.getDimensionPixelSize(R.dimen.browser_toolbar_height)
        engineView.setDynamicToolbarMaxHeight(toolbarMaxHeight)

        logger.debug("ToolbarMaxHeight: $toolbarMaxHeight")

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainContainerViewModel.toolbarOffset.collect {
                    logger.debug("Toolbar offset: $it")
                    findInPageBar.translationY = -toolbarMaxHeight + it
                }
            }
        }
    }
}
