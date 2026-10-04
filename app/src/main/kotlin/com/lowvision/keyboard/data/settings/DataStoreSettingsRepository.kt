package com.lowvision.keyboard.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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

    val settingsFlow: Flow<KeyboardSettings> = appContext.keyboardDataStore.data.map { prefs ->
        KeyboardSettings(
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
}