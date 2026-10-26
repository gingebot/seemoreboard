package com.seemoreboard.keyboard.ui.keyboard

import android.view.inputmethod.EditorInfo
import com.seemoreboard.keyboard.ui.theme.KeyboardTheme
import com.seemoreboard.keyboard.domain.settings.EmojiSize
import com.seemoreboard.keyboard.domain.settings.SuggestionTextSize

enum class ShiftState { OFF, ON, LOCK }

enum class KeyboardLayer { ALPHA, NUMERIC_SYMBOLS, EXTENDED_SYMBOLS }

data class KeyboardState(
    val lastKeyPressed: String = "",
    val pressCount: Int = 0,
    val zoomLevel: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val hapticEnabled: Boolean = true,
    val shiftState: ShiftState = ShiftState.OFF,
    val activeLayer: KeyboardLayer = KeyboardLayer.ALPHA,
    val keyboardHeightDp: Int = 350,
    val theme: KeyboardTheme = KeyboardTheme.BLACK_WHITE,
    val emojiPickerVisible: Boolean = false,
    val recentEmoji: List<String> = emptyList(),
    val emojiSize: EmojiSize = EmojiSize.LARGE,
    val predictiveTextEnabled: Boolean = true,
    val autocorrectEnabled: Boolean = true,
    val suggestionTextSizeSp: Int = SuggestionTextSize.DEFAULT_SP,
    val editorAction: Int = EditorInfo.IME_ACTION_NONE
)