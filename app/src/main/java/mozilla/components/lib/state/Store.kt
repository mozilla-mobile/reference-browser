/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.lib.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import mozilla.components.lib.state.ext.observe
import androidx.compose.runtime.State as ComposeState

/**
 * Starts observing this [Store] and represents the mapped state (using [map]) via [ComposeState].
 * Every time the [Store] state changes the returned [ComposeState] will be updated causing
 * recomposition of every [ComposeState.value] usage.
 *
 * The [Store] observer will automatically be removed when this composable disposes or the current
 * [LifecycleOwner] moves to the [Lifecycle.State.DESTROYED] state.
 */
@Composable
fun <S : State, A : Action, R> Store<S, A>.observeAsState(map: (S) -> R): ComposeState<R?> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember { mutableStateOf<R?>(map(state)) }

    observe(lifecycleOwner) { browserState ->
        state.value = map(browserState)
    }

    return state
}
