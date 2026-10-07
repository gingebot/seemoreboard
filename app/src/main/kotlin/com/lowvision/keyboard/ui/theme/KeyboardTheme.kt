package com.lowvision.keyboard.ui.theme

import androidx.compose.ui.graphics.Color

enum class KeyboardTheme(
    val displayName: String,
    val keyBackground: Color,
    val keyLabel: Color,
    val keyBorder: Color
) {
    BLACK_WHITE(
        displayName = "Black & White",
        keyBackground = Color.Black,
        keyLabel = Color.White,
        keyBorder = Color.White
    ),
    WHITE_BLACK(
        displayName = "White & Black",
        keyBackground = Color.White,
        keyLabel = Color.Black,
        keyBorder = Color.Black
    ),
    YELLOW_BLACK(
        displayName = "Yellow & Black",
        keyBackground = Color.Yellow,
        keyLabel = Color.Black,
        keyBorder = Color.Black
    ),
    BLUE_WHITE(
        displayName = "Blue & White",
        keyBackground = Color(0xFF0D47A1),
        keyLabel = Color.White,
        keyBorder = Color.White
    )
}