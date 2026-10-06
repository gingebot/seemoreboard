package com.lowvision.keyboard.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Slider
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lowvision.keyboard.data.settings.DataStoreSettingsRepository
import com.lowvision.keyboard.domain.settings.KeyboardSettings
import com.lowvision.keyboard.domain.settings.SettingsUseCases
import com.lowvision.keyboard.ui.components.KeyButton
import kotlin.math.roundToInt

class SettingsActivity : ComponentActivity() {

    private lateinit var settingsUseCases: SettingsUseCases

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = DataStoreSettingsRepository(applicationContext)
        settingsUseCases = SettingsUseCases(repository)

        setContent {
            SettingsScreen(settingsUseCases, onClose = { finish() })
        }
    }
}

@Composable
private fun SettingsScreen(useCases: SettingsUseCases, onClose: () -> Unit) {
    val settings by useCases.settingsFlow.collectAsState(initial = KeyboardSettings())
    var zoomValue by remember { mutableFloatStateOf(settings.zoomLevel) }
    var heightValue by remember { mutableIntStateOf(settings.keyboardHeightDp) }

    // Sync local state when persisted settings load (async after initial defaults)
    LaunchedEffect(settings.zoomLevel) {
        zoomValue = settings.zoomLevel
    }
    LaunchedEffect(settings.keyboardHeightDp) {
        heightValue = settings.keyboardHeightDp
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))

        // ---- Zoom Slider ----
        Text(
            text = "Zoom: %.1fx".format(zoomValue),
            color = Color.White,
            fontSize = 22.sp
        )
        Slider(
            value = zoomValue,
            onValueChange = {
                zoomValue = it
                useCases.setZoomLevel(it)
            },
            valueRange = 1f..3f,
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material.SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF666666),
                inactiveTrackColor = Color(0xFF333333)
            )
        )

        Spacer(Modifier.height(12.dp))

        // ---- Height Slider ----
        Text(
            text = "Keyboard Height: ${heightValue}dp",
            color = Color.White,
            fontSize = 22.sp
        )
        Slider(
            value = heightValue.toFloat(),
            onValueChange = {
                // Step in increments of 10dp
                val stepped = (it / 10f).roundToInt() * 10
                heightValue = stepped
                useCases.setKeyboardHeightDp(stepped)
            },
            valueRange = 200f..600f,
            steps = 39, // (600-200)/10 - 1 = 39 discrete steps
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material.SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF666666),
                inactiveTrackColor = Color(0xFF333333)
            )
        )

        Spacer(Modifier.height(12.dp))

        // ---- Preview Key ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            KeyButton(
                label = "k",
                highContrast = settings.highContrastEnabled,
                enabled = false,
                modifier = Modifier.scale(zoomValue),
                onClick = {}
            )
        }

        Spacer(Modifier.height(32.dp))

        RowSetting(
            label = "Haptics",
            checked = settings.hapticEnabled,
            onToggle = { useCases.setHapticEnabled(!settings.hapticEnabled) }
        )

        RowSetting(
            label = "High Contrast",
            checked = settings.highContrastEnabled,
            onToggle = { useCases.setHighContrastEnabled(!settings.highContrastEnabled) }
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
