/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.reference.browser.tabs.compose

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mozilla.components.concept.base.images.ImageLoadRequest
import mozilla.components.ui.icons.R as iconsR
import org.mozilla.reference.browser.R
import org.mozilla.reference.browser.compose.thumbnailStorage

internal val ThumbnailWidth = 78.dp
internal val ThumbnailHeight = 68.dp

private val FallbackIconSize = 24.dp

/**
 * A scaled thumbnail of the content a tab painted last.
 *
 * The bitmap is recycled when this composable leaves the composition. Without that a long list of thumbnails keeps
 * every bitmap it has ever scrolled past alive. The trade-off is that the thumbnail is loaded again when the row
 * scrolls back into view.
 *
 * @param tabId The ID of the tab to load the thumbnail for.
 * @param isPrivate Whether the tab is a private one, which decides the cache the thumbnail is read from.
 */
@Composable
internal fun TabThumbnail(
    tabId: String,
    isPrivate: Boolean,
) {
    val storage = thumbnailStorage()
    val scope = rememberCoroutineScope()
    val sizePx = with(LocalDensity.current) { ThumbnailWidth.toPx() }.toInt()
    val request = remember(tabId, isPrivate, sizePx) { ImageLoadRequest(tabId, sizePx, isPrivate) }

    var bitmap by remember(request) { mutableStateOf<Bitmap?>(null) }

    DisposableEffect(request) {
        val job = scope.launch {
            val thumbnail = storage.loadThumbnail(request).await()
            thumbnail?.prepareToDraw()
            bitmap = thumbnail
        }

        onDispose {
            job.cancel()
            bitmap?.recycle()
            bitmap = null
        }
    }

    Card(
        modifier = Modifier.size(width = ThumbnailWidth, height = ThumbnailHeight),
        shape = MaterialTheme.shapes.extraSmall,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        val thumbnail = bitmap

        if (thumbnail == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(iconsR.drawable.mozac_ic_globe_24),
                    contentDescription = stringResource(R.string.tabs_tray_thumbnail),
                    modifier = Modifier.size(FallbackIconSize),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Image(
                bitmap = thumbnail.asImageBitmap(),
                contentDescription = stringResource(R.string.tabs_tray_thumbnail),
                modifier = Modifier.fillMaxSize(),
                alignment = Alignment.TopCenter,
                contentScale = ContentScale.Crop,
            )
        }
    }
}
