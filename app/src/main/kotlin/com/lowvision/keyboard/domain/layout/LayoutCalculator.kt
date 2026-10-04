package com.lowvision.keyboard.domain.layout

import com.lowvision.keyboard.ui.keyboard.KeyboardLayer

object LayoutCalculator {

    val alphabetLayout = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", ""),
        listOf("z", "x", "c", "v", "b", "n", "m", "", "", "")
    )

    val symbolsLayout = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("/", ":", ";", "(", ")", "\u00A3", "&", "@", "\"", ""),
        listOf("!", "?", ",", ".", "+", "-", "_", "*", "", "")
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