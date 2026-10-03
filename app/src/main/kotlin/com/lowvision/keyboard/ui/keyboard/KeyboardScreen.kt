// Pending activation — blocked by Compose ViewTreeLifecycleOwner issue
// in InputMethodService context (see architecture.md §5.1).
package com.lowvision.keyboard.ui.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.lowvision.keyboard.ui.components.SingleKeyButton

@Composable
fun KeyboardScreen(viewModel: KeyboardViewModel) {
    val state by viewModel.state.collectAsState()

    SingleKeyButton(
        onKeyPress = { key ->
            viewModel.onKeyPress(key)
        }
    )

    // state.pressCount and state.lastKeyPressed are collected for reactive
    // recomposition and will be consumed by UI elements in future features.
    @Suppress("UNUSED_EXPRESSION")
    state
}