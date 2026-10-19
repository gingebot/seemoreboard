package com.seemoreboard.keyboard.ui.keyboard

import android.content.Context
import android.content.Intent
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seemoreboard.keyboard.domain.settings.SettingsUseCases
import com.seemoreboard.keyboard.ime.integration.InputConnectionBridge
import com.seemoreboard.keyboard.ui.settings.SettingsActivity
import com.seemoreboard.keyboard.ui.theme.KeyboardTheme
import com.seemoreboard.keyboard.utils.Logging
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
                        theme = settings.theme,
                        emojiSize = settings.emojiSize
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

    fun setEditorInfo(info: EditorInfo?) {
        val options = info?.imeOptions ?: 0
        val action = if (options and EditorInfo.IME_FLAG_NO_ENTER_ACTION != 0) {
            EditorInfo.IME_ACTION_NONE
        } else {
            options and EditorInfo.IME_MASK_ACTION
        }
        _state.update { it.copy(editorAction = action) }
    }

    fun onEditorAction() {
        val action = _state.value.editorAction
        if (action == EditorInfo.IME_ACTION_NONE) {
            inputBridge.submit()
        } else {
            inputBridge.performEditorAction(action)
        }
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

    fun showAlphabetLayer() {
        selectLayer(KeyboardLayer.ALPHA)
    }

    fun showNumericSymbolsLayer() {
        selectLayer(KeyboardLayer.NUMERIC_SYMBOLS)
    }

    fun showExtendedSymbolsLayer() {
        selectLayer(KeyboardLayer.EXTENDED_SYMBOLS)
    }

    private fun selectLayer(layer: KeyboardLayer) {
        _state.update { it.copy(activeLayer = layer, shiftState = ShiftState.OFF) }
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