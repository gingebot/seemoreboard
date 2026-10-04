package com.lowvision.keyboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lowvision.keyboard.ui.keyboard.KeyboardState

@Composable
fun SettingsPanel(
    state: KeyboardState,
    onToggleHaptics: () -> Unit,
    onToggleHighContrast: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        RowSetting(
            label = "Haptics Enabled",
            checked = state.hapticEnabled,
            onToggle = onToggleHaptics
        )

        RowSetting(
            label = "High-Contrast Mode",
            checked = state.highContrastEnabled,
            onToggle = onToggleHighContrast
        )

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Close",
            color = Color.White,
            fontSize = 24.sp,
            modifier = Modifier
                .clickable(onClick = onClose)
                .background(Color(0xFF333333))
                .padding(16.dp)
        )
    }
}

@Composable
private fun RowSetting(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 22.sp,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF666666)
            )
        )
    }
}