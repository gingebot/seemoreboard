package com.lowvision.keyboard.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lowvision.keyboard.domain.settings.KeyboardSettings
import com.lowvision.keyboard.ui.theme.KeyboardTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.keyboardDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "keyboard_settings"
)

class DataStoreSettingsRepository(context: Context) {

    private val appContext = context.applicationContext

    private val hapticEnabledKey = booleanPreferencesKey("haptic_enabled")
    private val zoomLevelKey = floatPreferencesKey("zoom_level")
    private val keyboardHeightDpKey = intPreferencesKey("keyboard_height_dp")
    private val themeKey = stringPreferencesKey("keyboard_theme")

    val settingsFlow: Flow<KeyboardSettings> = appContext.keyboardDataStore.data.map { prefs ->
        KeyboardSettings(
            zoomLevel = prefs[zoomLevelKey] ?: 1f,
            hapticEnabled = prefs[hapticEnabledKey] ?: true,
            keyboardHeightDp = prefs[keyboardHeightDpKey] ?: 350,
            theme = prefs[themeKey]?.let { value ->
                runCatching { KeyboardTheme.valueOf(value) }.getOrDefault(KeyboardTheme.BLACK_WHITE)
            } ?: KeyboardTheme.BLACK_WHITE
        )
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        appContext.keyboardDataStore.edit { prefs ->
            prefs[hapticEnabledKey] = enabled
        }
    }

    suspend fun setZoomLevel(level: Float) {
        appContext.keyboardDataStore.edit { prefs ->
            prefs[zoomLevelKey] = level.coerceIn(1f, 3f)
        }
    }

    suspend fun setKeyboardHeightDp(dp: Int) {
        appContext.keyboardDataStore.edit { prefs ->
            prefs[keyboardHeightDpKey] = dp.coerceIn(200, 600)
        }
    }

    suspend fun setTheme(theme: KeyboardTheme) {
        appContext.keyboardDataStore.edit { prefs ->
            prefs[themeKey] = theme.name
        }
    }
}