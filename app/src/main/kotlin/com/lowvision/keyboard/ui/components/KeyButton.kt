package com.lowvision.keyboard.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lowvision.keyboard.ui.theme.KeyboardTheme

@Composable
fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    theme: KeyboardTheme = KeyboardTheme.BLACK_WHITE,
    height: Dp = 56.dp,
    enabled: Boolean = true,
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
            .then(
                if (enabled) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ) else Modifier
            )
            .padding(2.dp)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}