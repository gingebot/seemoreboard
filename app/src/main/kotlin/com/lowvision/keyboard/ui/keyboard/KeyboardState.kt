package com.lowvision.keyboard.ui.keyboard

data class KeyboardState(
    val lastKeyPressed: String = "",
    val pressCount: Int = 0,
    val zoomLevel: Float = 1.0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val hapticEnabled: Boolean = true,
    val highContrastEnabled: Boolean = true
)