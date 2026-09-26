/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.compose.browser.toolbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import mozilla.components.browser.state.selector.selectedTab
import mozilla.components.lib.state.observeAsState
import org.mozilla.reference.browser.compose.browserStore
import org.mozilla.reference.browser.compose.sessionUseCases

/**
 * Experimental Compose toolbar, shown at the top of the browser when the Compose UI setting is on. The URL comes from
 * the store so it follows tab switches; only edit mode is local state.
 */
@Composable
fun BrowserToolbar() {
    val url: String? by browserStore().observeAsState { state -> state.selectedTab?.content?.url }
    val editMode = remember { mutableStateOf(false) }
    val useCases = sessionUseCases()

    if (editMode.value) {
        BrowserEditToolbar(
            url = url ?: "<empty>",
            onUrlCommitted = { text ->
                useCases.loadUrl(text)
                editMode.value = false
            },
        )
    } else {
        BrowserDisplayToolbar(
            url = url ?: "<empty>",
            onUrlClicked = { editMode.value = true },
        )
    }
}

/** Stateless: a tap is only reported, so [BrowserToolbar] decides when to switch into edit mode. */
@Composable
fun BrowserDisplayToolbar(
    url: String,
    onUrlClicked: () -> Unit = {},
) {
    Text(
        url,
        modifier = Modifier.clickable { onUrlClicked() },
        maxLines = 1,
    )
}

/**
 * Keeps the typed text locally and reports it only on Go, so nothing is loaded and the store's URL is untouched while
 * the user is still typing.
 */
@Composable
fun BrowserEditToolbar(
    url: String,
    onUrlCommitted: (String) -> Unit = {},
) {
    var input by remember { mutableStateOf(url) }

    TextField(
        input,
        onValueChange = { value -> input = value },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
        keyboardActions = KeyboardActions(onGo = { onUrlCommitted(input) }),
    )
}
