/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.compose.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mozilla.components.concept.tabstray.Tab
import org.mozilla.reference.browser.R

@Composable
fun BrowserThumbnail(tab: Tab) {
    Surface(
        modifier = Modifier
            .requiredSize(width = 100.dp, height = Dp.Unspecified)
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp)),
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
    ) {
        val image = tab.thumbnail?.asImageBitmap()
        val description = "thumbnail image of " + tab.title
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = description,
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.TopStart
            )
        } else {
            @Suppress("MagicNumber")
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .alpha(0.5f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.mozac_ic_globe),
                    contentDescription = description
                )
            }
        }
    }
}
