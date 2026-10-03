// Pending activation — blocked by Compose ViewTreeLifecycleOwner issue
// in InputMethodService context (see architecture.md §5.1).
package com.lowvision.keyboard.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lowvision.keyboard.ui.theme.HighContrastColors

@Composable
fun SingleKeyButton(onKeyPress: (String) -> Unit) {
    Button(
        onClick = { onKeyPress("A") },
        modifier = Modifier.fillMaxSize(),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = HighContrastColors.background,
            contentColor = HighContrastColors.onBackground
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "A",
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = HighContrastColors.keyText
            )
        }
    }
}