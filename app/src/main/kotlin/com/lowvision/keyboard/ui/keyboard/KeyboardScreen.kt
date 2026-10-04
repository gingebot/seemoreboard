package com.lowvision.keyboard.ui.keyboard

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import com.lowvision.keyboard.domain.layout.LayoutCalculator
import com.lowvision.keyboard.ui.components.KeyButton
import com.lowvision.keyboard.ui.components.SettingsPanel
import com.lowvision.keyboard.ui.gestures.ComposeZoomPan

@Composable
fun KeyboardScreen(viewModel: KeyboardViewModel) {
    val state by viewModel.state.collectAsState()
    val view = LocalView.current
    var containerWidth by remember { mutableIntStateOf(0) }
    var containerHeight by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onSizeChanged { size ->
                containerWidth = size.width
                containerHeight = size.height
            }
    ) {
        ComposeZoomPan(
            zoom = state.zoomLevel,
            offsetX = state.offsetX,
            offsetY = state.offsetY,
            onZoomChange = {
                viewModel.updateZoom(it)
                val st = viewModel.state.value
                val iw = containerWidth * st.zoomLevel
                val ih = containerHeight * st.zoomLevel
                val rw = containerWidth.toFloat()
                val rh = containerHeight.toFloat()
                if (iw <= rw && ih <= rh) {
                    if (st.offsetX != 0f || st.offsetY != 0f) {
                        viewModel.updatePan(-st.offsetX, -st.offsetY)
                    }
                } else {
                    val maxTx = rw * (st.zoomLevel - 1f) / 2f
                    val minTx = rw * (1f - st.zoomLevel) / 2f
                    val maxTy = rh * (st.zoomLevel - 1f) / 2f
                    val minTy = rh * (1f - st.zoomLevel) / 2f
                    val cx = st.offsetX.coerceIn(minTx, maxTx)
                    val cy = st.offsetY.coerceIn(minTy, maxTy)
                    if (cx != st.offsetX || cy != st.offsetY) {
                        viewModel.updatePan(cx - st.offsetX, cy - st.offsetY)
                    }
                }
            },
            onPanChange = { dx, dy ->
                val state = viewModel.state.value
                val innerW = containerWidth * state.zoomLevel
                val innerH = containerHeight * state.zoomLevel
                val rw = containerWidth.toFloat()
                val rh = containerHeight.toFloat()

                if (innerW <= rw && innerH <= rh) {
                    if (state.offsetX != 0f || state.offsetY != 0f) {
                        viewModel.updatePan(-state.offsetX, -state.offsetY)
                    }
                } else {
                    viewModel.updatePan(dx, dy)
                    val ns = viewModel.state.value
                    val maxTx = rw * (ns.zoomLevel - 1f) / 2f
                    val minTx = rw * (1f - ns.zoomLevel) / 2f
                    val maxTy = rh * (ns.zoomLevel - 1f) / 2f
                    val minTy = rh * (1f - ns.zoomLevel) / 2f
                    val cx = ns.offsetX.coerceIn(minTx, maxTx)
                    val cy = ns.offsetY.coerceIn(minTy, maxTy)
                    if (cx != ns.offsetX || cy != ns.offsetY) {
                        viewModel.updatePan(cx - ns.offsetX, cy - ns.offsetY)
                    }
                }
            }
        ) {
            KeyboardLayout(
                state = state,
                onKeyPress = { key ->
                    if (state.hapticEnabled) {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                    viewModel.onKeyPress(key)
                },
                onToggleShift = { viewModel.toggleShift() },
                onToggleLayer = { viewModel.toggleLayer() },
                onOpenSettings = { viewModel.openSettings() }
            )
        }

        if (state.isSettingsOpen) {
            SettingsPanel(
                state = state,
                onToggleHaptics = { viewModel.toggleHaptics() },
                onToggleHighContrast = { viewModel.toggleHighContrast() },
                onClose = { viewModel.closeSettings() }
            )
        }
    }
}

@Composable
private fun KeyboardLayout(
    state: KeyboardState,
    onKeyPress: (String) -> Unit,
    onToggleShift: () -> Unit,
    onToggleLayer: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val layout = LayoutCalculator.getLayout(state.activeLayer)
        val isAlpha = state.activeLayer == KeyboardLayer.ALPHA

        for (row in layout) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (char in row) {
                    if (char.isNotEmpty()) {
                        val label = if (isAlpha && state.shiftState != ShiftState.OFF) {
                            char.uppercase()
                        } else {
                            char
                        }
                        KeyButton(
                            label = label,
                            highContrast = state.highContrastEnabled,
                            modifier = Modifier.weight(1f),
                            onClick = { onKeyPress(char) }
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Modifier row: Shift | ABC/123 | ⚙
        Row(modifier = Modifier.fillMaxWidth()) {
            val shiftBg = when (state.shiftState) {
                ShiftState.OFF -> if (state.highContrastEnabled) Color.Black else Color(0xFF333333)
                ShiftState.ON -> Color(0xFF444444)
                ShiftState.LOCK -> Color(0xFF666666)
            }
            KeyButton(
                label = "\u21E7",
                highContrast = state.highContrastEnabled,
                modifier = Modifier.weight(1f).background(shiftBg),
                onClick = onToggleShift
            )
            KeyButton(
                label = if (state.activeLayer == KeyboardLayer.ALPHA) "123" else "ABC",
                highContrast = state.highContrastEnabled,
                modifier = Modifier.weight(1f),
                onClick = onToggleLayer
            )
            KeyButton(
                label = "\u2699",
                highContrast = state.highContrastEnabled,
                modifier = Modifier.weight(1f),
                onClick = onOpenSettings
            )
        }
    }
}