package com.lowvision.keyboard.domain.settings

import com.lowvision.keyboard.ui.theme.KeyboardTheme

data class KeyboardSettings(
    val zoomLevel: Float = 1.0f,
    val panSensitivity: Float = 1.0f,
    val hapticEnabled: Boolean = true,
    val audioEnabled: Boolean = false,
    val keyboardHeightDp: Int = 350,
    val theme: KeyboardTheme = KeyboardTheme.BLACK_WHITE,
    val emojiSize: EmojiSize = EmojiSize.LARGE
)