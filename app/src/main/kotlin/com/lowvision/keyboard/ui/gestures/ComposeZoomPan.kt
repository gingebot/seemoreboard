package com.lowvision.keyboard.ui.gestures

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs
import kotlin.math.sqrt

@Composable
fun ComposeZoomPan(
    zoom: Float,
    offsetX: Float,
    offsetY: Float,
    onZoomChange: (Float) -> Unit,
    onPanChange: (Float, Float) -> Unit,
    content: @Composable () -> Unit
) {
    val currentZoom = rememberUpdatedState(zoom)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = zoom
                scaleY = zoom
                translationX = offsetX
                translationY = offsetY
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.type != PointerEventType.Press) continue
                        if (event.changes.isEmpty()) continue

                        if (event.changes.size >= 2) {
                            // Multi-touch zoom
                            event.changes.forEach { it.consume() }
                            var previousSpan = span(event.changes)

                            while (true) {
                                val moveEvent = awaitPointerEvent(PointerEventPass.Initial)
                                moveEvent.changes.forEach { it.consume() }
                                val active = moveEvent.changes.filter { it.pressed }
                                if (active.size >= 2) {
                                    val currentSpan = span(active)
                                    if (previousSpan > 0f && currentSpan > 0f) {
                                        val factor = currentSpan / previousSpan
                                        if (kotlin.math.abs(factor - 1f) > 0.001f) {
                                            onZoomChange(currentZoom.value * factor)
                                        }
                                    }
                                    previousSpan = currentSpan
                                }
                                if (active.isEmpty()) break
                            }
                        } else {
                            // Single finger — distinguish drag (pan) from tap
                            val firstPos = event.changes.first().position
                            var lastPos = firstPos
                            var accumulated = Offset.Zero
                            var isPanning = false

                            while (true) {
                                val moveEvent = awaitPointerEvent(PointerEventPass.Initial)
                                val active = moveEvent.changes.filter { it.pressed }

                                if (active.size >= 2) {
                                    // Became multi-touch — switch to zoom
                                    moveEvent.changes.forEach { it.consume() }
                                    var prevSpan = span(active)
                                    while (true) {
                                        val zoomEvent = awaitPointerEvent(PointerEventPass.Initial)
                                        zoomEvent.changes.forEach { it.consume() }
                                        val zoomActive = zoomEvent.changes.filter { it.pressed }
                                        if (zoomActive.size >= 2) {
                                            val curSpan = span(zoomActive)
                                            if (prevSpan > 0f && curSpan > 0f) {
                                                val factor = curSpan / prevSpan
                                                if (kotlin.math.abs(factor - 1f) > 0.001f) {
                                                    onZoomChange(currentZoom.value * factor)
                                                }
                                            }
                                            prevSpan = curSpan
                                        }
                                        if (zoomActive.isEmpty()) break
                                    }
                                    break
                                }

                                if (active.isEmpty()) break

                                val pos = active.first().position
                                val delta = pos - lastPos
                                lastPos = pos

                                if (!isPanning) {
                                    accumulated += delta
                                    if (accumulated.getDistance() > 20f) {
                                        isPanning = true
                                        moveEvent.changes.forEach { it.consume() }
                                        onPanChange(accumulated.x, accumulated.y)
                                        accumulated = Offset.Zero
                                    }
                                } else {
                                    moveEvent.changes.forEach { it.consume() }
                                    if (delta.x != 0f || delta.y != 0f) {
                                        onPanChange(delta.x, delta.y)
                                    }
                                }
                            }
                        }
                    }
                }
            }
    ) {
        content()
    }
}

private fun span(changes: List<androidx.compose.ui.input.pointer.PointerInputChange>): Float {
    if (changes.size < 2) return 0f
    val p0 = changes[0].position
    val p1 = changes[1].position
    val dx = p0.x - p1.x
    val dy = p0.y - p1.y
    return sqrt(dx * dx + dy * dy)
}