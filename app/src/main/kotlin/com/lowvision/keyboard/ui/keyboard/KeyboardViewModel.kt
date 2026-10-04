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

    companion object {
        private const val DOUBLE_TAP_MS = 500L
    }

    private var lastShiftTapMs = 0L

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
        val current = _state.value
        val char = if (current.activeLayer == KeyboardLayer.ALPHA &&
            current.shiftState != ShiftState.OFF) {
            key.uppercase()
        } else {
            key
        }
        inputBridge.typeCharacter(char)
        _state.update {
            it.copy(
                lastKeyPressed = char,
                pressCount = it.pressCount + 1,
                shiftState = if (it.shiftState == ShiftState.ON) ShiftState.OFF
                else it.shiftState
            )
        }
        Logging.log("Key pressed: $char")
    }

    fun toggleShift() {
        _state.update {
            val now = System.currentTimeMillis()
            lastShiftTapMs = now
            when (it.shiftState) {
                ShiftState.OFF -> it.copy(shiftState = ShiftState.ON)
                ShiftState.ON -> it.copy(shiftState = ShiftState.LOCK)
                ShiftState.LOCK -> it.copy(shiftState = ShiftState.OFF)
            }
        }
    }

    fun toggleLayer() {
        _state.update {
            it.copy(
                activeLayer = if (it.activeLayer == KeyboardLayer.ALPHA)
                    KeyboardLayer.SYMBOLS else KeyboardLayer.ALPHA,
                shiftState = ShiftState.OFF
            )
        }
    }

    fun openSettings() {
        _state.update { it.copy(isSettingsOpen = true) }
    }

    fun closeSettings() {
        _state.update { it.copy(isSettingsOpen = false) }
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