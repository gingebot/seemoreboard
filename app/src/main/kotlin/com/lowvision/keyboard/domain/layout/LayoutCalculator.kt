package com.lowvision.keyboard.domain.layout

object LayoutCalculator {

    /**
     * Transform a touch coordinate from screen space back to the logical
     * (un‑zoomed, un‑panned) layout space.
     */
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

    /**
     * Determine which key (if any) contains the given touch point,
     * accounting for zoom and pan.
     */
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