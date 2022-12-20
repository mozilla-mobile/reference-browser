/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.addons

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.AddonManagerException
import mozilla.components.feature.addons.ui.translateName
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.ext.components

/**
 * An activity to show the details of a installed add-on.
 */
@Suppress("TooManyFunctions")
class InstalledAddonDetailsActivity : AppCompatActivity() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_installed_add_on_details)
        val addon = requireNotNull(intent.getParcelableExtra<Addon>("add_on")).also {
            bindUI(it)
        }
        bindAddon(addon)
    }

    private fun bindAddon(addon: Addon) {
        scope.launch {
            try {
                val addons = baseContext.components.core.addonManager.getAddons()
                scope.launch(Dispatchers.Main) {
                    addons.find { addon.id == it.id }.let {
                        if (it == null) {
                            throw AddonManagerException(Exception("Addon ${addon.id} not found"))
                        } else {
                            bindUI(it)
                        }
                    }
                }
            } catch (e: AddonManagerException) {
                scope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        baseContext,
                        R.string.mozac_feature_addons_failed_to_query_add_ons,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    private fun bindUI(addon: Addon) {
        title = addon.translateName(this)

        bindEnableSwitch(addon)

        bindSettings(addon)

        bindDetails(addon)

        bindPermissions(addon)

        bindAllowInPrivateBrowsingSwitch(addon)

        bindRemoveButton(addon)
    }

    private fun bindVersion(addon: Addon) {
        val versionView = findViewById<TextView>(R.id.version_text)
        versionView.text = addon.version
    }

    private fun bindEnableSwitch(addon: Addon) {
        val switch = findViewById<SwitchCompat>(R.id.enable_switch)
        switch.setState(addon.isEnabled())
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                this.components.core.addonManager.enableAddon(
                    addon,
                    onSuccess = {
                        switch.setState(true)
                        Toast.makeText(
                            this,
                            getString(R.string.mozac_feature_addons_successfully_enabled, addon.translateName(this)),
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                    onError = {
                        Toast.makeText(
                            this,
                            getString(R.string.mozac_feature_addons_failed_to_enable, addon.translateName(this)),
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                )
            } else {
                this.components.core.addonManager.disableAddon(
                    addon,
                    onSuccess = {
                        switch.setState(false)
                        Toast.makeText(
                            this,
                            getString(R.string.mozac_feature_addons_successfully_disabled, addon.translateName(this)),
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                    onError = {
                        Toast.makeText(
                            this,
                            getString(R.string.mozac_feature_addons_failed_to_disable, addon.translateName(this)),
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                )
            }
        }
    }

    private fun bindSettings(addon: Addon) {
        val view = findViewById<View>(R.id.settings)
        view.isVisible = shouldSettingsBeVisible(addon)
        view.isEnabled = shouldSettingsBeVisible(addon)
        view.setOnClickListener {
            val intent = Intent(this, AddonSettingsActivity::class.java)
            intent.putExtra("add_on", addon)
            this.startActivity(intent)
        }
    }

    private fun bindDetails(addon: Addon) {
        findViewById<View>(R.id.details).setOnClickListener {
            val intent = Intent(this, AddonDetailsActivity::class.java)
            intent.putExtra("add_on", addon)
            this.startActivity(intent)
        }
    }

    private fun bindPermissions(addon: Addon) {
        findViewById<View>(R.id.permissions).setOnClickListener {
            val intent = Intent(this, PermissionsDetailsActivity::class.java)
            intent.putExtra("add_on", addon)
            this.startActivity(intent)
        }
    }

    private fun bindAllowInPrivateBrowsingSwitch(addon: Addon) {
        val switch = findViewById<SwitchCompat>(R.id.allow_in_private_browsing_switch)
        switch.isChecked = addon.isAllowedInPrivateBrowsing()
        switch.setOnCheckedChangeListener { _, isChecked ->
            this.components.core.addonManager.setAddonAllowedInPrivateBrowsing(
                addon,
                isChecked,
                onSuccess = {
                    switch.isChecked = isChecked
                },
            )
        }
    }

    private fun bindRemoveButton(addon: Addon) {
        findViewById<View>(R.id.remove_add_on).setOnClickListener {
            this.components.core.addonManager.uninstallAddon(
                addon,
                onSuccess = {
                    Toast.makeText(
                        this,
                        getString(R.string.mozac_feature_addons_successfully_uninstalled, addon.translateName(this)),
                        Toast.LENGTH_SHORT,
                    ).show()
                    finish()
                },
                onError = { _, _ ->
                    Toast.makeText(
                        this,
                        getString(R.string.mozac_feature_addons_failed_to_uninstall, addon.translateName(this)),
                        Toast.LENGTH_SHORT,
                    ).show()
                },
            )
        }
    }

    private fun SwitchCompat.setState(checked: Boolean) {
        val text = if (checked) {
            R.string.mozac_feature_addons_enabled
        } else {
            R.string.mozac_feature_addons_disabled
        }
        setText(text)
        isChecked = checked
    }

    private fun shouldSettingsBeVisible(addon: Addon) = !addon.installedState?.optionsPageUrl.isNullOrEmpty()
}
