package com.lowvision.keyboard.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.lowvision.keyboard.domain.settings.KeyboardSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.keyboardDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "keyboard_settings"
)

class DataStoreSettingsRepository(context: Context) {

    private val appContext = context.applicationContext

    private val hapticEnabledKey = booleanPreferencesKey("haptic_enabled")
    private val highContrastEnabledKey = booleanPreferencesKey("high_contrast_enabled")
    private val zoomLevelKey = floatPreferencesKey("zoom_level")

    val settingsFlow: Flow<KeyboardSettings> = appContext.keyboardDataStore.data.map { prefs ->
        KeyboardSettings(
            zoomLevel = prefs[zoomLevelKey] ?: 1f,
            hapticEnabled = prefs[hapticEnabledKey] ?: true,
            highContrastEnabled = prefs[highContrastEnabledKey] ?: true
        )
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        appContext.keyboardDataStore.edit { prefs ->
            prefs[hapticEnabledKey] = enabled
        }
    }

    suspend fun setHighContrastEnabled(enabled: Boolean) {
        appContext.keyboardDataStore.edit { prefs ->
            prefs[highContrastEnabledKey] = enabled
        }
    }

    suspend fun setZoomLevel(level: Float) {
        appContext.keyboardDataStore.edit { prefs ->
            prefs[zoomLevelKey] = level.coerceIn(1f, 3f)
        }
    }
}