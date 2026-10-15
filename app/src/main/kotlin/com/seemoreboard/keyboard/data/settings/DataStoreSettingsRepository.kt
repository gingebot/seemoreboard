package com.seemoreboard.keyboard.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.seemoreboard.keyboard.domain.settings.KeyboardSettings
import com.seemoreboard.keyboard.domain.settings.EmojiSize
import com.seemoreboard.keyboard.ui.theme.KeyboardTheme
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
    private val recentEmojiKey = stringPreferencesKey("recent_emoji")
    private val emojiSizeKey = stringPreferencesKey("emoji_size")

    val settingsFlow: Flow<KeyboardSettings> = appContext.keyboardDataStore.data.map { prefs ->
        KeyboardSettings(
            zoomLevel = prefs[zoomLevelKey] ?: 1f,
            hapticEnabled = prefs[hapticEnabledKey] ?: true,
            keyboardHeightDp = prefs[keyboardHeightDpKey] ?: 350,
            theme = prefs[themeKey]?.let { value ->
                runCatching { KeyboardTheme.valueOf(value) }.getOrDefault(KeyboardTheme.BLACK_WHITE)
            } ?: KeyboardTheme.BLACK_WHITE,
            emojiSize = prefs[emojiSizeKey]?.let { value ->
                runCatching { EmojiSize.valueOf(value) }.getOrDefault(EmojiSize.LARGE)
            } ?: EmojiSize.LARGE
        )
    }

    val recentEmojiFlow: Flow<List<String>> = appContext.keyboardDataStore.data.map { prefs ->
        prefs[recentEmojiKey].orEmpty()
            .split(RECENT_EMOJI_SEPARATOR)
            .filter(String::isNotEmpty)
    }

    suspend fun addRecentEmoji(emoji: String) {
        appContext.keyboardDataStore.edit { prefs ->
            val recent = prefs[recentEmojiKey].orEmpty()
                .split(RECENT_EMOJI_SEPARATOR)
                .filter(String::isNotEmpty)
                .let { listOf(emoji) + it.filterNot { value -> value == emoji } }
                .take(MAX_RECENT_EMOJI)
            prefs[recentEmojiKey] = recent.joinToString(RECENT_EMOJI_SEPARATOR)
        }
    }

    private companion object {
        const val MAX_RECENT_EMOJI = 30
        const val RECENT_EMOJI_SEPARATOR = "\u0001"
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

    suspend fun setEmojiSize(size: EmojiSize) {
        appContext.keyboardDataStore.edit { prefs ->
            prefs[emojiSizeKey] = size.name
        }
    }
}