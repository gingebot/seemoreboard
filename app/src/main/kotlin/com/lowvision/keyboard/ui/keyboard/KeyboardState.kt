package com.lowvision.keyboard.ui.keyboard

import com.lowvision.keyboard.ui.theme.KeyboardTheme
import com.lowvision.keyboard.domain.settings.EmojiSize

enum class ShiftState { OFF, ON, LOCK }

enum class KeyboardLayer { ALPHA, SYMBOLS }

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
    val emojiSize: EmojiSize = EmojiSize.LARGE
)