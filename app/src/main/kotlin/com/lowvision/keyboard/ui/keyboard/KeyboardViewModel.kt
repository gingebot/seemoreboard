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
}