package com.lowvision.keyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    highContrast: Boolean = true,
    onClick: () -> Unit
) {
    val bgColor = if (highContrast) Color.Black else Color(0xFF333333)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .background(bgColor)
            .clickable(onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 24.sp
        )
    }
}