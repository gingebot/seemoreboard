package com.seemoreboard.keyboard.ui.settings

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.seemoreboard.keyboard.data.settings.DataStoreSettingsRepository
import com.seemoreboard.keyboard.domain.settings.EmojiSize
import com.seemoreboard.keyboard.domain.settings.KeyboardSettings
import com.seemoreboard.keyboard.domain.settings.SuggestionTextSize
import com.seemoreboard.keyboard.domain.settings.SettingsUseCases
import com.seemoreboard.keyboard.ime.service.SeeMoreBoardService
import kotlin.math.roundToInt

class SettingsActivity : ComponentActivity() {
    private lateinit var settingsUseCases: SettingsUseCases
    private var isSeeMoreBoardEnabled by mutableStateOf(false)
    private var isSeeMoreBoardDefault by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsUseCases = SettingsUseCases(DataStoreSettingsRepository(applicationContext))
        setContent {
            SettingsScreen(
                useCases = settingsUseCases,
                onClose = { finish() },
                isSeeMoreBoardEnabled = { isSeeMoreBoardEnabled },
                isSeeMoreBoardDefault = { isSeeMoreBoardDefault },
                onEnableSeeMoreBoard = ::openInputMethodSettings,
                onSelectSeeMoreBoard = ::showInputMethodPicker
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refreshInputMethodState()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            refreshInputMethodState()
        }
    }

    private fun refreshInputMethodState() {
        isSeeMoreBoardEnabled = isInputMethodEnabled()
        isSeeMoreBoardDefault = isDefaultInputMethod()
    }

    private fun seeMoreBoardInputMethod(): String = ComponentName(
        this,
        SeeMoreBoardService::class.java
    ).flattenToShortString()

    private fun isInputMethodEnabled(): Boolean {
        val inputMethodManager = getSystemService(InputMethodManager::class.java)
            ?: return false
        return inputMethodManager.enabledInputMethodList.any { inputMethodInfo ->
            inputMethodInfo.packageName == packageName &&
                inputMethodInfo.serviceName == SeeMoreBoardService::class.java.name
        }
    }

    private fun isDefaultInputMethod(): Boolean {
        val currentInputMethod = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        )
        return currentInputMethod == seeMoreBoardInputMethod()
    }

    private fun openInputMethodSettings() {
        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
    }

    private fun showInputMethodPicker() {
        getSystemService(InputMethodManager::class.java)?.showInputMethodPicker()
    }
}

@Composable
fun SettingsScreen(
    useCases: SettingsUseCases,
    onClose: () -> Unit,
    isSeeMoreBoardEnabled: () -> Boolean = { true },
    isSeeMoreBoardDefault: () -> Boolean = { true },
    onEnableSeeMoreBoard: () -> Unit = {},
    onSelectSeeMoreBoard: () -> Unit = {}
) {
    val settings by useCases.settingsFlow.collectAsState(initial = KeyboardSettings())
    var zoomValue by remember { mutableFloatStateOf(settings.zoomLevel) }
    var heightValue by remember { mutableIntStateOf(settings.keyboardHeightDp) }
    var suggestionTextSizeValue by remember { mutableIntStateOf(settings.suggestionTextSizeSp) }
    LaunchedEffect(settings.zoomLevel) { zoomValue = settings.zoomLevel }
    LaunchedEffect(settings.keyboardHeightDp) { heightValue = settings.keyboardHeightDp }
    LaunchedEffect(settings.suggestionTextSizeSp) { suggestionTextSizeValue = settings.suggestionTextSizeSp }

    SettingsContent(
        settings = settings,
        isSeeMoreBoardEnabled = isSeeMoreBoardEnabled(),
        isSeeMoreBoardDefault = isSeeMoreBoardDefault(),
        onEnableSeeMoreBoard = if (isSeeMoreBoardEnabled()) onSelectSeeMoreBoard else onEnableSeeMoreBoard,
        zoomValue = zoomValue,
        heightValue = heightValue,
        onZoomChanged = { zoomValue = it; useCases.setZoomLevel(it) },
        onHeightChanged = {
            val stepped = (it / 10f).roundToInt() * 10
            heightValue = stepped
            useCases.setKeyboardHeightDp(stepped)
        },
        onThemeChanged = { useCases.setTheme(useCases.cycleTheme(settings.theme)) },
        onHapticsChanged = { useCases.setHapticEnabled(!settings.hapticEnabled) },
        onEmojiSizeChanged = useCases::setEmojiSize,
        suggestionTextSizeValue = suggestionTextSizeValue,
        onPredictiveTextChanged = useCases::setPredictiveTextEnabled,
        onAutocorrectChanged = useCases::setAutocorrectEnabled,
        onSuggestionTextSizeChanged = { sizeSp ->
            val stepped = SuggestionTextSize.clamp(
                (sizeSp.toFloat() / SuggestionTextSize.STEP_SP).roundToInt() * SuggestionTextSize.STEP_SP
            )
            suggestionTextSizeValue = stepped
            useCases.setSuggestionTextSizeSp(stepped)
        },
        onClose = onClose
    )
}

@Composable
fun SettingsContent(
    settings: KeyboardSettings,
    isSeeMoreBoardEnabled: Boolean = true,
    isSeeMoreBoardDefault: Boolean = true,
    onEnableSeeMoreBoard: () -> Unit = {},
    zoomValue: Float,
    heightValue: Int,
    onZoomChanged: (Float) -> Unit,
    onHeightChanged: (Float) -> Unit,
    onThemeChanged: () -> Unit,
    onHapticsChanged: () -> Unit,
    onEmojiSizeChanged: (EmojiSize) -> Unit,
    suggestionTextSizeValue: Int = settings.suggestionTextSizeSp,
    onPredictiveTextChanged: (Boolean) -> Unit = {},
    onAutocorrectChanged: (Boolean) -> Unit = {},
    onSuggestionTextSizeChanged: (Int) -> Unit = {},
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isSeeMoreBoardDefault) {
            val enableLabel = if (isSeeMoreBoardEnabled) {
                "Make SeeMoreBoard default"
            } else {
                "Tap to enable"
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF333333))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "SeeMoreBoard is not the active keyboard",
                    color = Color.White,
                    fontSize = 20.sp
                )
                Text(
                    enableLabel,
                    color = Color.White,
                    fontSize = 22.sp,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .clickable(onClick = onEnableSeeMoreBoard)
                        .background(Color(0xFF555555))
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .semantics { contentDescription = enableLabel }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Zoom: %.1fx".format(zoomValue), color = Color.White, fontSize = 22.sp)
        Slider(value = zoomValue, onValueChange = onZoomChanged, valueRange = 1f..3f, modifier = Modifier.fillMaxWidth(), colors = sliderColors())

        Spacer(Modifier.height(12.dp))
        Text("Keyboard Height: ${heightValue}dp", color = Color.White, fontSize = 22.sp)
        Slider(value = heightValue.toFloat(), onValueChange = onHeightChanged, valueRange = 200f..600f, steps = 39, modifier = Modifier.fillMaxWidth(), colors = sliderColors())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightValue.dp)
                .clipToBounds()
                .background(Color(0xFF1A1A1A))
                .semantics { contentDescription = "Keyboard preview" }
        ) {
            KeyboardPreview(theme = settings.theme, zoom = zoomValue)
        }

        Text("Active Theme: ${settings.theme.displayName}", color = Color.White, fontSize = 22.sp, modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp).semantics { liveRegion = LiveRegionMode.Polite })
        Text("Change Theme", color = Color.White, fontSize = 22.sp, modifier = Modifier.clickable(onClick = onThemeChanged).background(Color(0xFF333333)).padding(horizontal = 24.dp, vertical = 16.dp).semantics { contentDescription = "Change Theme" })

        Spacer(Modifier.height(24.dp))
        RowSetting("Haptics", settings.hapticEnabled) { onHapticsChanged() }
        EmojiSizeSetting(settings.emojiSize, onEmojiSizeChanged)

        Spacer(Modifier.height(16.dp))
        RowSetting("Predictive text", settings.predictiveTextEnabled, onPredictiveTextChanged)
        RowSetting("Autocorrect", settings.autocorrectEnabled, onAutocorrectChanged)
        Text(
            "Suggestion text size: ${suggestionTextSizeValue} sp",
            color = Color.White,
            fontSize = 22.sp,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { liveRegion = LiveRegionMode.Polite }
        )
        Slider(
            value = suggestionTextSizeValue.toFloat(),
            onValueChange = { onSuggestionTextSizeChanged(it.roundToInt()) },
            valueRange = SuggestionTextSize.MIN_SP.toFloat()..SuggestionTextSize.MAX_SP.toFloat(),
            steps = (SuggestionTextSize.MAX_SP - SuggestionTextSize.MIN_SP) / SuggestionTextSize.STEP_SP - 1,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Suggestion text size"
                    stateDescription = "$suggestionTextSizeValue sp"
                },
            colors = sliderColors()
        )

        Spacer(Modifier.height(32.dp))
        Text("Close", color = Color.White, fontSize = 24.sp, modifier = Modifier.clickable(onClick = onClose).background(Color(0xFF333333)).padding(16.dp))
    }
}

@Composable
private fun EmojiSizeSetting(selected: EmojiSize, onSelected: (EmojiSize) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Emoji Size", color = Color.White, fontSize = 22.sp)
        Column(horizontalAlignment = Alignment.End) {
            Text(selected.displayName, color = Color.White, fontSize = 20.sp, modifier = Modifier.background(Color(0xFF333333)).clickable { expanded = true }.padding(horizontal = 20.dp, vertical = 14.dp).semantics { contentDescription = "Emoji Size ${selected.displayName}" })
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                EmojiSize.entries.forEach { size ->
                    DropdownMenuItem(onClick = { expanded = false; onSelected(size) }) { Text(size.displayName, fontSize = 20.sp) }
                }
            }
        }
    }
}

@Composable
private fun RowSetting(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 22.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            modifier = Modifier.semantics {
                contentDescription = label
                stateDescription = if (checked) "On" else "Off"
            },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF666666))
        )
    }
}

@Composable
private fun sliderColors() = androidx.compose.material.SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color(0xFF666666), inactiveTrackColor = Color(0xFF333333))