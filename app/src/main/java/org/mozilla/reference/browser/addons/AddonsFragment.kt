/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.addons

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.AddonManagerException
import mozilla.components.feature.addons.ui.AddonInstallationDialogFragment
import mozilla.components.feature.addons.ui.AddonsManagerAdapter
import mozilla.components.feature.addons.ui.AddonsManagerAdapterDelegate
import mozilla.components.feature.addons.ui.PermissionsDialogFragment
import mozilla.components.feature.addons.ui.translateName
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components

/**
 * Fragment use for managing add-ons.
 */
class AddonsFragment : Fragment(), AddonsManagerAdapterDelegate {
    private lateinit var recyclerView: RecyclerView
    private val scope = CoroutineScope(Dispatchers.IO)

    private val addonProgressOverlay: View
        get() = requireView().findViewById(R.id.addonProgressOverlay)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_add_ons, container, false)
    }

    override fun onViewCreated(rootView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(rootView, savedInstanceState)
        bindRecyclerView(rootView)
    }

    override fun onStart() {
        super.onStart()

        this@AddonsFragment.view?.let { view ->
            bindRecyclerView(view)
        }

        addonProgressOverlay.visibility = View.GONE

        findPreviousDialogFragment()?.let { dialog ->
            dialog.onPositiveButtonClicked = onPositiveButtonClicked
        }
    }

    private fun bindRecyclerView(rootView: View) {
        recyclerView = rootView.findViewById(R.id.add_ons_list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        scope.launch {
            try {
                val addons = requireContext().components.core.addonManager.getAddons()

                scope.launch(Dispatchers.Main) {
                    val adapter = AddonsManagerAdapter(
                        requireContext().components.core.addonCollectionProvider,
                        this@AddonsFragment,
                        addons,
                    )
                    recyclerView.adapter = adapter
                }
            } catch (e: AddonManagerException) {
                scope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        activity,
                        R.string.mozac_feature_addons_failed_to_query_add_ons,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    override fun onAddonItemClicked(addon: Addon) {
        if (addon.isInstalled()) {
            val intent = Intent(context, InstalledAddonDetailsActivity::class.java)
            intent.putExtra("add_on", addon)
            startActivity(intent)
        } else {
            val intent = Intent(context, AddonDetailsActivity::class.java)
            intent.putExtra("add_on", addon)
            startActivity(intent)
        }
    }

    override fun onInstallAddonButtonClicked(addon: Addon) {
        showPermissionDialog(addon)
    }

    private fun isAlreadyADialogCreated(): Boolean {
        return findPreviousDialogFragment() != null
    }

    private fun findPreviousDialogFragment(): PermissionsDialogFragment? {
        return parentFragmentManager.findFragmentByTag(PERMISSIONS_DIALOG_FRAGMENT_TAG) as? PermissionsDialogFragment
    }

    private fun showPermissionDialog(addon: Addon) {
        if (isInstallationInProgress) {
            return
        }

        val dialog = PermissionsDialogFragment.newInstance(
            addon = addon,
            onPositiveButtonClicked = onPositiveButtonClicked,
        )

        if (!isAlreadyADialogCreated()) {
            dialog.show(parentFragmentManager, PERMISSIONS_DIALOG_FRAGMENT_TAG)
        }
    }

    private fun showInstallationDialog(addon: Addon) {
        if (isInstallationInProgress) {
            return
        }

        val dialog = AddonInstallationDialogFragment.newInstance(
            addon = addon,
            addonCollectionProvider = requireContext().components.core.addonCollectionProvider,
            onConfirmButtonClicked = { _, allowInPrivateBrowsing ->
                if (allowInPrivateBrowsing) {
                    requireContext().components.core.addonManager.setAddonAllowedInPrivateBrowsing(
                        addon,
                        allowInPrivateBrowsing,
                    )
                }
            },
        )

        if (!isAlreadyADialogCreated()) {
            dialog.show(parentFragmentManager, INSTALLATION_DIALOG_FRAGMENT_TAG)
        }
    }

    private val onPositiveButtonClicked: ((Addon) -> Unit) = { addon ->
        addonProgressOverlay.visibility = View.VISIBLE
        isInstallationInProgress = true
        requireContext().components.core.addonManager.installAddon(
            addon,
            onSuccess = {
                runIfFragmentIsAttached {
                    isInstallationInProgress = false
                    this@AddonsFragment.view?.let { view ->
                        bindRecyclerView(view)
                        showInstallationDialog(it)
                    }

                    addonProgressOverlay.visibility = View.GONE
                }
            },
            onError = { _, _ ->
                runIfFragmentIsAttached {
                    context?.let {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.mozac_feature_addons_failed_to_install, addon.translateName(it)),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                    addonProgressOverlay.visibility = View.GONE
                    isInstallationInProgress = false
                }
            },
        )
    }

    /**
     * Whether or not an add-on installation is in progress.
     */
    private var isInstallationInProgress = false

    companion object {
        private const val PERMISSIONS_DIALOG_FRAGMENT_TAG = "ADDONS_PERMISSIONS_DIALOG_FRAGMENT"
        private const val INSTALLATION_DIALOG_FRAGMENT_TAG = "ADDONS_INSTALLATION_DIALOG_FRAGMENT"
    }
}
