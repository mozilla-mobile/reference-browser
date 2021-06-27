/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.compose.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mozilla.components.browser.state.state.createTab
import mozilla.components.concept.tabstray.Tabs
import mozilla.components.feature.tabs.TabsUseCases
import org.mozilla.reference.browser.R
import java.util.UUID
import androidx.compose.ui.graphics.Color as Colour
import mozilla.components.concept.tabstray.Tab as BrowserTab

@Composable
fun Tab(
    tab: BrowserTab,
    selected: Boolean = false,
    onClick: (String) -> Unit = {},
    onClose: (String) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .background(if (selected) Colour(0xFFFF45A1FF.toInt()) else Colour.Unspecified)
            .size(width = Dp.Unspecified, height = 72.dp)
            .fillMaxWidth()
            .clickable { onClick.invoke(tab.id) }
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            BrowserThumbnail(tab)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
                    .padding(8.dp)
            ) {
                Text(
                    text = tab.title,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    color = Colour.White
                )
                Text(
                    text = tab.url,
                    style = MaterialTheme.typography.body2,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    color = Colour.White.copy(alpha = ContentAlpha.medium)
                )
            }
            IconButton(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .requiredSize(24.dp),
                onClick = { onClose.invoke(tab.id) }
            ) {
                Icon(
                    painter = painterResource(R.drawable.mozac_ic_close),
                    contentDescription = "close",
                    tint = Colour.White
                )
            }
        }
    }
}

@Composable
fun TabsTrayList(
    tabs: Tabs,
    useCases: TabsUseCases? = null,
    closeTabsTray: () -> Unit = {}
) {
    LazyColumn {
        itemsIndexed(tabs.list) { index, tab ->
            Tab(
                tab = tab,
                selected = index == tabs.selectedIndex,
                onClick = {
                    useCases?.selectTab?.invoke(it)
                    closeTabsTray.invoke()
                },
                onClose = { useCases?.removeTab?.invoke(it) }
            )
        }
    }
}

@Preview(name = "Tab preview", showBackground = true)
@Composable
fun TabPreview() {
    val tab = BrowserTab(
        id = UUID.randomUUID().toString(),
        url = "https://mozilla.org",
        title = "Mozilla - Maker of Reference Browser and other stuff."
    )
    Tab(tab = tab)
}

@Preview(name = "TabsTray preview", showBackground = true)
@Composable
fun TabsTrayListPreview() {
    val list = listOf(
        createTab(id = "1", url = "https://mozilla.org", title = "Mozilla - Maker of Reference Browser.").toTab(),
        createTab(id = "2", url = "https://firefox.com", title = "Firefox - Firefox, does things.").toTab()
    )

    val tabs = Tabs(list, 1)

    TabsTrayList(tabs)
}
