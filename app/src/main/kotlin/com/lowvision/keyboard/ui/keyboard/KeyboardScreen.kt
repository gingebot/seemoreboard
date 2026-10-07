package com.lowvision.keyboard.ui.keyboard

import android.view.HapticFeedbackConstants
import androidx.emoji2.text.EmojiCompat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.lowvision.keyboard.domain.layout.LayoutCalculator
import com.lowvision.keyboard.ui.components.KeyButton
import com.lowvision.keyboard.ui.gestures.PanContainer

@Composable
fun KeyboardScreen(viewModel: KeyboardViewModel) {
    val state by viewModel.state.collectAsState()
    val view = LocalView.current
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (state.emojiPickerVisible) {
                    Modifier.fillMaxHeight()
                } else {
                    Modifier.height(with(density) { state.keyboardHeightDp.dp })
                }
            )
            .background(Color(0xFF1A1A1A))
    ) {
        if (state.emojiPickerVisible) {
            EmojiPickerContent(viewModel)
        } else {
            PanContainer(
            zoom = state.zoomLevel,
            offsetX = state.offsetX,
            offsetY = state.offsetY,
            onPanChange = { dx, dy -> viewModel.updatePan(dx, dy) }
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
                onOpenSettings = { viewModel.openSettingsActivity() },
                onOpenEmojiPicker = { viewModel.handleEmojiKey() }
            )
        }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EmojiPickerContent(viewModel: KeyboardViewModel) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Back to keyboard",
            color = Color.White,
            fontSize = 24.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF333333))
                .clickable { viewModel.closeEmojiPicker() }
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .semantics { contentDescription = "Close emoji picker" }
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A)),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(LOW_VISION_EMOJI_SAMPLE, key = { it }) { emoji ->
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                        .background(Color(0xFF333333), RoundedCornerShape(8.dp))
                        .clickable { viewModel.commitEmoji(emoji) }
                        .semantics { contentDescription = "Emoji $emoji" },
                    factory = {
                        android.widget.TextView(context).apply {
                            gravity = android.view.Gravity.CENTER
                            setTextColor(android.graphics.Color.WHITE)
                            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 56f)
                            isClickable = false
                            isFocusable = false
                        }
                    },
                    update = { cell ->
                        cell.text = processEmojiForDisplay(emoji)
                    }
                )
            }
        }
    }
}

private fun processEmojiForDisplay(emoji: String): CharSequence {
    return if (EmojiCompat.isConfigured()) {
        EmojiCompat.get().process(emoji) ?: emoji
    } else {
        emoji
    }
}

private val LOW_VISION_EMOJI_SAMPLE = listOf(
    "😀", "😂", "😍", "🥰",
    "😎", "😭", "😡", "🤔",
    "👍", "👎", "👏", "🙏"
)

@Composable
private fun KeyboardLayout(
    state: KeyboardState,
    onKeyPress: (String) -> Unit,
    onToggleShift: () -> Unit,
    onToggleLayer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenEmojiPicker: () -> Unit
) {
    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val layout = LayoutCalculator.getLayout(state.activeLayer)
        val isAlpha = state.activeLayer == KeyboardLayer.ALPHA

        for (rowIdx in layout.indices) {
            val row = layout[rowIdx]
            val isBottomRow = rowIdx == layout.lastIndex
            val keyHeight = if (isBottomRow) 60.dp else 48.dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (colIdx in row.indices) {
                    val entry = row[colIdx]
                    if (entry.isEmpty()) {
                        val spacerWeight = if (!isBottomRow &&
                            (colIdx == 0 || colIdx == row.lastIndex)) 0.5f else 1f
                        Box(modifier = Modifier.weight(spacerWeight))
                        continue
                    }

                    if (entry.startsWith("\u25B6")) {
                        val label = entry.removePrefix("\u25B6")
                        val weight = when (label) {
                            "Space" -> 3f
                            "123?" -> 1.8f
                            "ABC" -> 1.8f
                            else -> 1f
                        }
                        val onClick: () -> Unit = when (label) {
                            "Shift" -> onToggleShift
                            "\u232B" -> ({ onKeyPress("⌫") })
                            "123?" -> onToggleLayer
                            "ABC" -> onToggleLayer
                            "\u2699" -> onOpenSettings
                            "\uD83D\uDE42" -> onOpenEmojiPicker
                            "Space" -> ({ onKeyPress(" ") })
                            "." -> ({ onKeyPress(".") })
                            "\u23CE" -> ({ onKeyPress("\n") })
                            else -> ({})
                        }
                        // Shift key gets state-based background color
                        val shiftMod = if (label == "Shift") {
                            val shiftBg = when (state.shiftState) {
                                ShiftState.OFF -> state.theme.keyBackground
                                ShiftState.ON -> Color(0xFF444444)
                                ShiftState.LOCK -> Color(0xFF666666)
                            }
                            Modifier.weight(weight).background(shiftBg, RoundedCornerShape(8.dp))
                        } else {
                            Modifier.weight(weight)
                        }
                        KeyButton(
                            label = if (label == "Shift") "\u21E7" else label,
                            theme = state.theme,
                            modifier = shiftMod,
                            enabled = true,
                            contentDescription = if (label == "\uD83D\uDE42") {
                                "Open emoji picker"
                            } else {
                                label
                            },
                            height = keyHeight,
                            onClick = onClick
                        )
                    } else {
                        // Regular character key
                        val label = if (isAlpha && state.shiftState != ShiftState.OFF) {
                            entry.uppercase()
                        } else {
                            entry
                        }
                        KeyButton(
                            label = label,
                            theme = state.theme,
                            modifier = Modifier.weight(1f),
                            height = keyHeight,
                            onClick = { onKeyPress(entry) }
                        )
                    }
                }
            }
        }
    }
}