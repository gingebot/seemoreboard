package com.lowvision.keyboard.ui.keyboard

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lowvision.keyboard.domain.settings.SettingsUseCases
import com.lowvision.keyboard.ime.integration.InputConnectionBridge
import com.lowvision.keyboard.ui.settings.SettingsActivity
import com.lowvision.keyboard.ui.theme.KeyboardTheme
import com.lowvision.keyboard.utils.Logging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class KeyboardViewModel(
    private val appContext: Context,
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
                        zoomLevel = settings.zoomLevel,
                        hapticEnabled = settings.hapticEnabled,
                        keyboardHeightDp = settings.keyboardHeightDp,
                        theme = settings.theme
                    )
                }
            }
        }
        viewModelScope.launch {
            settingsUseCases.recentEmojiFlow.collect { recent ->
                _state.update { it.copy(recentEmoji = recent) }
            }
        }
    }

    fun handleEmojiKey() {
        _state.update { it.copy(emojiPickerVisible = true) }
    }

    fun closeEmojiPicker() {
        _state.update { it.copy(emojiPickerVisible = false) }
    }

    fun commitEmoji(emoji: String) {
        inputBridge.commitText(emoji)
        settingsUseCases.addRecentEmoji(emoji)
        viewModelScope.launch {
            delay(150L)
            closeEmojiPicker()
            delay(50L)
            inputBridge.refreshCursor()
        }
    }

    fun onKeyPress(key: String) {
        if (key == "\b" || key == "⌫") {
            inputBridge.deleteCharacter()
            _state.update { it.copy(lastKeyPressed = "⌫", pressCount = it.pressCount + 1) }
            Logging.log("Key pressed: ⌫")
            return
        }
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

    fun openSettingsActivity() {
        appContext.startActivity(
            Intent(appContext, SettingsActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
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

    fun setZoomLevel(level: Float) {
        _state.update { it.copy(zoomLevel = level.coerceIn(1f, 3f)) }
        settingsUseCases.setZoomLevel(level)
    }

    fun setKeyboardHeightDp(dp: Int) {
        val clamped = dp.coerceIn(200, 600)
        _state.update { it.copy(keyboardHeightDp = clamped) }
        settingsUseCases.setKeyboardHeightDp(clamped)
    }

    fun cycleTheme() {
        val nextTheme = settingsUseCases.cycleTheme(_state.value.theme)
        _state.update { it.copy(theme = nextTheme) }
        settingsUseCases.setTheme(nextTheme)
    }

    fun setTheme(theme: KeyboardTheme) {
        _state.update { it.copy(theme = theme) }
        settingsUseCases.setTheme(theme)
    }
}