package com.lowvision.keyboard.ui.keyboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lowvision.keyboard.domain.settings.SettingsUseCases
import com.lowvision.keyboard.ime.integration.InputConnectionBridge
import com.lowvision.keyboard.utils.Logging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class KeyboardViewModel(
    private val inputBridge: InputConnectionBridge,
    private val settingsUseCases: SettingsUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(KeyboardState())
    val state: StateFlow<KeyboardState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsUseCases.settingsFlow.collect { settings ->
                _state.update {
                    it.copy(
                        hapticEnabled = settings.hapticEnabled,
                        highContrastEnabled = settings.highContrastEnabled
                    )
                }
            }
        }
    }

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

    fun toggleHaptics() {
        val current = _state.value.hapticEnabled
        settingsUseCases.setHapticEnabled(!current)
    }

    fun toggleHighContrast() {
        val current = _state.value.highContrastEnabled
        settingsUseCases.setHighContrastEnabled(!current)
    }
}