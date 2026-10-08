package com.lowvision.keyboard.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.lowvision.keyboard.domain.layout.LayoutCalculator
import com.lowvision.keyboard.ui.components.KeyButton
import com.lowvision.keyboard.ui.keyboard.KeyboardLayer
import com.lowvision.keyboard.ui.theme.KeyboardTheme

@Composable
fun KeyboardPreview(theme: KeyboardTheme, zoom: Float, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val keyboardHeight = 204.dp
        val viewportWidth = maxWidth

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxHeight),
            contentAlignment = Alignment.Center
        ) {
            PreviewLayer(
                layout = LayoutCalculator.getLayout(KeyboardLayer.ALPHA),
                theme = theme,
                modifier = Modifier
                    .width(viewportWidth)
                    .height(keyboardHeight)
                    .scale(zoom)
            )
        }
    }
}

@Composable
private fun PreviewLayer(
    layout: List<List<String>>,
    theme: KeyboardTheme,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        layout.forEachIndexed { rowIndex, row ->
            val bottomRow = rowIndex == layout.lastIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEachIndexed { columnIndex, entry ->
                    if (entry.isEmpty()) {
                        val weight = if (!bottomRow && (columnIndex == 0 || columnIndex == row.lastIndex)) 0.5f else 1f
                        Box(modifier = Modifier.weight(weight))
                    } else {
                        val label = entry.removePrefix("\u25B6")
                        val weight = when (label) {
                            "Space" -> 3f
                            "123?" -> 1.8f
                            else -> 1f
                        }
                        KeyButton(
                            label = if (label == "Shift") "\u21E7" else label,
                            theme = theme,
                            modifier = Modifier.weight(weight),
                            enabled = false,
                            height = 48.dp,
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}
