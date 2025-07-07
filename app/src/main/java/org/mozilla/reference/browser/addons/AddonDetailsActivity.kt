/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.addons

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.text.HtmlCompat
import mozilla.components.feature.addons.Addon
import mozilla.components.feature.addons.ui.translateDescription
import mozilla.components.feature.addons.ui.translateName
import mozilla.components.support.ktx.android.view.setupPersistentInsets
import mozilla.components.support.utils.ext.getParcelableExtraCompat
import org.mozilla.reference.browser.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * An activity to show the details of an add-on.
 */
class AddonDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(SystemBarStyle.dark(Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_on_details)
        window.setupPersistentInsets()

        val addon = requireNotNull(
            intent.getParcelableExtraCompat("add_on", Addon::class.java),
        )
        bind(addon)
    }

    private fun bind(addon: Addon) {
        title = addon.translateName(this)

        bindDetails(addon)

        bindAuthor(addon)

        bindVersion(addon)

        bindLastUpdated(addon)

        bindWebsite(addon)

        bindRating(addon)
    }

    private fun bindRating(addon: Addon) {
        addon.rating?.let {
            val ratingView = findViewById<RatingBar>(R.id.rating_view)
            val userCountView = findViewById<TextView>(R.id.users_count)

            val ratingContentDescription = getString(R.string.mozac_feature_addons_rating_content_description_2)
            ratingView.contentDescription = String.format(ratingContentDescription, it.average)
            ratingView.rating = it.average

            userCountView.text = getFormattedAmount(it.reviews)
        }
    }

    private fun bindWebsite(addon: Addon) {
        findViewById<View>(R.id.home_page_text).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, addon.homepageUrl.toUri())
            startActivity(intent)
        }
    }

    private fun bindLastUpdated(addon: Addon) {
        val lastUpdatedView = findViewById<TextView>(R.id.last_updated_text)
        lastUpdatedView.text = formatDate(addon.updatedAt)
    }

    private fun bindVersion(addon: Addon) {
        val versionView = findViewById<TextView>(R.id.version_text)
        versionView.text = addon.version
    }

    private fun bindAuthor(addon: Addon) {
        val authorsView = findViewById<TextView>(R.id.author_text)

        authorsView.text = addon.author?.name.orEmpty()
    }

    private fun bindDetails(addon: Addon) {
        val detailsView = findViewById<TextView>(R.id.details)
        val detailsText = addon.translateDescription(this)

        val parsedText = detailsText.replace("\n", "<br/>")
        val text = HtmlCompat.fromHtml(parsedText, HtmlCompat.FROM_HTML_MODE_COMPACT)

        detailsView.text = text
        detailsView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun formatDate(text: String): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        return DateFormat.getDateInstance().format(formatter.parse(text)!!)
    }
}
