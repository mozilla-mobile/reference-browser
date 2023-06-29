
/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.addons

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import mozilla.components.browser.state.action.WebExtensionAction
import mozilla.components.browser.state.state.extension.WebExtensionPromptRequest
import mozilla.components.browser.state.store.BrowserStore
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.ui.PermissionsDialogFragment
import mozilla.components.lib.state.ext.flowScoped
import mozilla.components.support.base.feature.LifecycleAwareFeature
import org.mozilla.reference.browser.R

/**
 * Feature implementation for handling [WebExtensionPromptRequest] and showing the respective UI.
 */
class WebExtensionPromptFeature(
    private val store: BrowserStore,
    private val provideAddons: suspend () -> List<Addon>,
    private val context: Context,
    private val view: View,
    private val fragmentManager: FragmentManager,
) : LifecycleAwareFeature {

    /**
     * Whether or not an add-on installation is in progress.
     */
    private var isInstallationInProgress = false
    private var scope: CoroutineScope? = null

    /**
     * Starts observing the selected session to listen for window requests
     * and opens / closes tabs as needed.
     */
    override fun start() {
        scope = store.flowScoped { flow ->
            flow.mapNotNull { state ->
                state.webExtensionPromptRequest
            }.distinctUntilChanged().collect { promptRequest ->
                if (promptRequest is WebExtensionPromptRequest.Permissions && !hasExistingPermissionDialogFragment()) {
                    val addon = provideAddons().find { addon ->
                        addon.id == promptRequest.extension.id
                    }

                    // If the add-on is not found, it is already installed because the install process can only
                    // be triggered for add-ons "known" by Fenix (the add-on is either part of the official list
                    // of supported extensions OR part of the user custom AMO collection).
                    if (addon == null) {
                        promptRequest.onConfirm(false)
                        consumePromptRequest()
                        Toast.makeText(
                            context,
                            R.string.mozac_feature_addons_failed_to_query_add_ons,
                            Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        showPermissionDialog(
                            addon,
                            promptRequest,
                        )
                    }
                }
            }
        }
        tryToReAttachButtonHandlersToPreviousDialog()
    }

    /**
     * Stops observing the selected session for incoming window requests.
     */
    override fun stop() {
        scope?.cancel()
    }

    @VisibleForTesting
    internal fun showPermissionDialog(
        addon: Addon,
        promptRequest: WebExtensionPromptRequest.Permissions,
    ) {
        if (!isInstallationInProgress && !hasExistingPermissionDialogFragment()) {
            val dialog = PermissionsDialogFragment.newInstance(
                addon = addon,
                onPositiveButtonClicked = {
                    handleApprovedPermissions(promptRequest)
                },
                onNegativeButtonClicked = {
                    handleDeniedPermissions(promptRequest)
                },
            )
            dialog.show(
                fragmentManager,
                PERMISSIONS_DIALOG_FRAGMENT_TAG,
            )
        }
    }

    private fun tryToReAttachButtonHandlersToPreviousDialog() {
        findPreviousDialogFragment()?.let { dialog ->
            dialog.onPositiveButtonClicked = { addon ->
                store.state.webExtensionPromptRequest?.let { promptRequest ->
                    if (addon.id == promptRequest.extension.id &&
                        promptRequest is WebExtensionPromptRequest.Permissions
                    ) {
                        handleApprovedPermissions(promptRequest)
                    }
                }
            }
            dialog.onNegativeButtonClicked = {
                store.state.webExtensionPromptRequest?.let { promptRequest ->
                    if (promptRequest is WebExtensionPromptRequest.Permissions) {
                        handleDeniedPermissions(promptRequest)
                    }
                }
            }
        }
    }

    private fun handleDeniedPermissions(promptRequest: WebExtensionPromptRequest.Permissions) {
        promptRequest.onConfirm(false)
        consumePromptRequest()
    }

    private fun handleApprovedPermissions(promptRequest: WebExtensionPromptRequest.Permissions) {
        promptRequest.onConfirm(true)
        consumePromptRequest()
    }

    private fun consumePromptRequest() {
        store.dispatch(WebExtensionAction.ConsumePromptRequestWebExtensionAction)
    }

    private fun hasExistingPermissionDialogFragment(): Boolean {
        return findPreviousDialogFragment() != null
    }

    private fun findPreviousDialogFragment(): PermissionsDialogFragment? {
        return fragmentManager.findFragmentByTag(PERMISSIONS_DIALOG_FRAGMENT_TAG) as? PermissionsDialogFragment
    }

    companion object {
        private const val PERMISSIONS_DIALOG_FRAGMENT_TAG = "ADDONS_PERMISSIONS_DIALOG_FRAGMENT"
    }
}
