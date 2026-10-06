package com.lowvision.keyboard.ui.gestures

import android.view.ViewConfiguration
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PanContainer(
    zoom: Float,
    offsetX: Float,
    offsetY: Float,
    onPanChange: (dx: Float, dy: Float) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var contentSize by remember { mutableStateOf(IntSize.Zero) }
    val currentOffset = remember { mutableStateOf(Offset(offsetX, offsetY)) }

    var flingJobX by remember { mutableStateOf<Job?>(null) }
    var flingJobY by remember { mutableStateOf<Job?>(null) }
    val flingDecay: DecayAnimationSpec<Float> = remember { exponentialDecay(frictionMultiplier = 1f) }
    var isFlingingX by remember { mutableStateOf(false) }
    var isFlingingY by remember { mutableStateOf(false) }
    val isFlinging = isFlingingX || isFlingingY
    val minFlingVelocity = ViewConfiguration.get(LocalContext.current).scaledMinimumFlingVelocity
    var prevFlingX by remember { mutableFloatStateOf(0f) }
    var prevFlingY by remember { mutableFloatStateOf(0f) }

    // Clamp range: graphicsLayer scales from center, so excess is split equally.
    // Allow panning so the user can reach all edges.
    fun clampRange(): Pair<Float, Float> {
        val excessX = (contentSize.width * zoom) - viewportSize.width
        val excessY = (contentSize.height * zoom) - viewportSize.height
        val halfX = (excessX / 2f).coerceAtLeast(0f)
        val halfY = (excessY / 2f).coerceAtLeast(0f)
        return Pair(halfX, halfY)
    }

    val draggableState = rememberDraggable2DState { delta ->
        flingJobX?.cancel()
        flingJobY?.cancel()
        val (halfX, halfY) = clampRange()
        val scaledDx = delta.x * zoom
        val scaledDy = delta.y * zoom
        val clamped = Offset(
            x = (currentOffset.value.x + scaledDx).coerceIn(-halfX, halfX),
            y = (currentOffset.value.y + scaledDy).coerceIn(-halfY, halfY),
        )
        val appliedDelta = Offset(clamped.x - currentOffset.value.x, clamped.y - currentOffset.value.y)
        currentOffset.value = clamped
        onPanChange(appliedDelta.x, appliedDelta.y)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { viewportSize = it }
            .clipToBounds()
            .graphicsLayer {
                scaleX = zoom
                scaleY = zoom
                translationX = currentOffset.value.x
                translationY = currentOffset.value.y
            }
            .draggable2D(
                state = draggableState,
                onDragStopped = { flingVelocity ->
                    val (halfX, halfY) = clampRange()

                    if (abs(flingVelocity.x) > minFlingVelocity) {
                        prevFlingX = currentOffset.value.x
                        flingJobX = scope.launch {
                            isFlingingX = true
                            try {
                                AnimationState(
                                    initialValue = currentOffset.value.x,
                                    initialVelocity = flingVelocity.x,
                                ).animateDecay(flingDecay) {
                                    val clamped = value.coerceIn(-halfX, halfX)
                                    val delta = clamped - prevFlingX
                                    currentOffset.value = currentOffset.value.copy(x = clamped)
                                    prevFlingX = clamped
                                    onPanChange(delta, 0f)
                                    if (clamped != value || abs(velocity) < 200f) cancelAnimation()
                                }
                            } finally {
                                isFlingingX = false
                            }
                        }
                    }

                    if (abs(flingVelocity.y) > minFlingVelocity) {
                        prevFlingY = currentOffset.value.y
                        flingJobY = scope.launch {
                            isFlingingY = true
                            try {
                                AnimationState(
                                    initialValue = currentOffset.value.y,
                                    initialVelocity = flingVelocity.y,
                                ).animateDecay(flingDecay) {
                                    val clamped = value.coerceIn(-halfY, halfY)
                                    val delta = clamped - prevFlingY
                                    currentOffset.value = currentOffset.value.copy(y = clamped)
                                    prevFlingY = clamped
                                    onPanChange(0f, delta)
                                    if (clamped != value || abs(velocity) < 200f) cancelAnimation()
                                }
                            } finally {
                                isFlingingY = false
                            }
                        }
                    }
                }
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { contentSize = it }
        ) {
            content()
        }

        if (isFlinging) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (event.changes.any { it.pressed }) {
                                    flingJobX?.cancel()
                                    flingJobY?.cancel()
                                    event.changes.forEach { it.consume() }
                                }
                            }
                        }
                    }
            )
        }
    }
}
