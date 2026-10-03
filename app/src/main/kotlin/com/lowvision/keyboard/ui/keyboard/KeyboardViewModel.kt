package com.lowvision.keyboard.ui.keyboard

import androidx.lifecycle.ViewModel
import com.lowvision.keyboard.ime.integration.InputConnectionBridge
import com.lowvision.keyboard.utils.Logging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class KeyboardViewModel(
    private val inputBridge: InputConnectionBridge
) : ViewModel() {

    private val _state = MutableStateFlow(KeyboardState())
    val state: StateFlow<KeyboardState> = _state.asStateFlow()

    fun onKeyPress(key: String) {
        inputBridge.typeCharacter(key)
        _state.update { it.copy(lastKeyPressed = key, pressCount = it.pressCount + 1) }
        Logging.log("Key pressed: $key")
    }

    fun updateZoom(zoom: Float) {
        _state.update { it.copy(zoomLevel = zoom.coerceIn(1.0f, 2.5f)) }
    }

    fun updatePan(dx: Float, dy: Float) {
        _state.update {
            it.copy(
                offsetX = it.offsetX + dx,
                offsetY = it.offsetY + dy
            )
        }
    }
}