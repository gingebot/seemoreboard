package com.seemoreboard.keyboard.domain.layout

import com.seemoreboard.keyboard.ui.keyboard.KeyboardLayer

object LayoutCalculator {

    val alphabetLayout = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", ""),
        listOf("\u25B6Shift", "z", "x", "c", "v", "b", "n", "m", ",", "\u25B6\u232B"),
        listOf("\u25B6123?", "\u25B6\u2699", "\u25B6Space", "\u25B6\uD83D\uDE42", "\u25B6.", "\u25B6\u23CE")
    )

    val numericSymbolsLayout = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("@", "#", "\u00A3", "_", "&", "-", "+", "(", ")", "/"),
        listOf("\u25B6=<", "*", "'", "\"", ":", ";", "!", "?", "\u25B6\u232B"),
        listOf("\u25B6ABC", ",", "\u25B6Space", ".", "\u25B6\u23CE")
    )

    val extendedSymbolsLayout = listOf(
        listOf("~", "|", "\u221A", "\u03C0", "\u00D7", "\u00A7", "\u0394"),
        listOf("\u20AC", "\u00A5", "$", "\u00A2", "^", "{", "}", "\\"),
        listOf("\u25B6?123", "%", "\u00A9", "\u2122", "\u2713", "[", "]", "\u25B6\u232B"),
        listOf("\u25B6ABC", "<", "\u25B6Space", ">", "\u25B6\u23CE")
    )

    fun getLayout(layer: KeyboardLayer): List<List<String>> {
        return when (layer) {
            KeyboardLayer.ALPHA -> alphabetLayout
            KeyboardLayer.NUMERIC_SYMBOLS -> numericSymbolsLayout
            KeyboardLayer.EXTENDED_SYMBOLS -> extendedSymbolsLayout
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