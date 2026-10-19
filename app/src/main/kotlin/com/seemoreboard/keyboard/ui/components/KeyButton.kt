package com.seemoreboard.keyboard.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.seemoreboard.keyboard.ui.theme.KeyboardTheme

@Composable
fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    theme: KeyboardTheme = KeyboardTheme.BLACK_WHITE,
    height: Dp = 56.dp,
    enabled: Boolean = true,
    contentDescription: String = label,
    showSearchIcon: Boolean = false,
    onLongPressRepeat: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val bgColor = if (isPressed) theme.keyLabel else theme.keyBackground
    val textColor = if (isPressed) theme.keyBackground else theme.keyLabel
    val borderColor = if (isPressed) theme.keyBackground else theme.keyBorder
    val scale by animateFloatAsState(if (isPressed) 1.05f else 1f)

    Box(
        modifier = modifier
            .height(height)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .scale(scale)
            .shadow(if (isPressed) 0.dp else 2.dp, shape)
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .then(if (enabled && onLongPressRepeat != null) {
                Modifier.pointerInput(onClick, onLongPressRepeat) {
                    coroutineScope {
                        while (true) {
                            val down = awaitPointerEventScope {
                                awaitFirstDown(requireUnconsumed = false)
                            }
                            down.consume()
                            val press = PressInteraction.Press(down.position)
                            interactionSource.tryEmit(press)

                            onClick()
                            val repeatJob: Job = launch {
                                delay(LONG_PRESS_DELAY_MS)
                                while (isActive) {
                                    onLongPressRepeat()
                                    delay(REPEAT_INTERVAL_MS)
                                }
                            }

                            val up = awaitPointerEventScope {
                                waitForUpOrCancellation()
                            }
                            repeatJob.cancel()
                            if (up == null) {
                                interactionSource.tryEmit(PressInteraction.Cancel(press))
                            } else {
                                up.consume()
                                interactionSource.tryEmit(PressInteraction.Release(press))
                            }
                        }
                    }
                }
            } else if (enabled) {
                Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
            } else {
                Modifier
            })
            .padding(2.dp)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        if (showSearchIcon) {
            Canvas(modifier = Modifier.size(28.dp)) {
                val strokeWidth = 3.dp.toPx()
                val radius = size.minDimension * 0.28f
                val center = androidx.compose.ui.geometry.Offset(
                    x = size.width * 0.42f,
                    y = size.height * 0.42f
                )
                drawCircle(
                    color = textColor,
                    radius = radius,
                    center = center,
                    style = Stroke(strokeWidth)
                )
                drawLine(
                    color = textColor,
                    start = androidx.compose.ui.geometry.Offset(
                        center.x + radius * 0.7f,
                        center.y + radius * 0.7f
                    ),
                    end = androidx.compose.ui.geometry.Offset(
                        size.width * 0.8f,
                        size.height * 0.8f
                    ),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        } else {
            Text(
                text = label,
                color = textColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private const val LONG_PRESS_DELAY_MS = 400L
private const val REPEAT_INTERVAL_MS = 75L