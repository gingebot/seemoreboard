package com.lowvision.keyboard.domain.layout

import com.lowvision.keyboard.ui.keyboard.KeyboardLayer

object LayoutCalculator {

    val alphabetLayout = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", ""),
        listOf("\u25B6Shift", "z", "x", "c", "v", "b", "n", "m", ",", "\u25B6\u232B"),
        listOf("\u25B6123?", "\u25B6\u2699", "\u25B6Space", "\u25B6\uD83D\uDE42", "\u25B6.", "\u25B6\u23CE")
    )

    val symbolsLayout = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("/", ":", ";", "(", ")", "\u00A3", "&", "@", "\"", ""),
        listOf("\u25B6Shift", "!", "?", ",", ".", "+", "-", "_", "*", "\u25B6\u232B"),
        listOf("\u25B6ABC", "\u25B6\u2699", "\u25B6Space", "\u25B6\uD83D\uDE42", "\u25B6.", "\u25B6\u23CE")
    )

    fun getLayout(layer: KeyboardLayer): List<List<String>> {
        return when (layer) {
            KeyboardLayer.ALPHA -> alphabetLayout
            KeyboardLayer.SYMBOLS -> symbolsLayout
        }
    }

    fun transformTouch(
        touchX: Float,
        touchY: Float,
        zoomLevel: Float,
        offsetX: Float,
        offsetY: Float
    ): Pair<Float, Float> {
        val logicalX = (touchX - offsetX) / zoomLevel
        val logicalY = (touchY - offsetY) / zoomLevel
        return Pair(logicalX, logicalY)
    }

    fun hitTest(
        touchX: Float,
        touchY: Float,
        keys: List<KeyDefinition>,
        zoomLevel: Float,
        offsetX: Float,
        offsetY: Float
    ): KeyDefinition? {
        val (logicalX, logicalY) = transformTouch(
            touchX, touchY, zoomLevel, offsetX, offsetY
        )
        return keys.find { key ->
            logicalX >= key.x && logicalX <= key.x + key.width &&
            logicalY >= key.y && logicalY <= key.y + key.height
        }
    }
}