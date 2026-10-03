package com.lowvision.keyboard.domain.settings

// Placeholder — will be implemented in a future feature spec.
data class KeyboardSettings(
    val zoomLevel: Float = 1.0f,
    val panSensitivity: Float = 1.0f,
    val hapticEnabled: Boolean = true,
    val audioEnabled: Boolean = false,
    val highContrastEnabled: Boolean = true
)