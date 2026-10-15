package com.seemoreboard.keyboard.domain.settings

import com.seemoreboard.keyboard.data.settings.DataStoreSettingsRepository
import com.seemoreboard.keyboard.ui.theme.KeyboardTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SettingsUseCases(
    private val repository: DataStoreSettingsRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    val settingsFlow: Flow<KeyboardSettings> = repository.settingsFlow
    val recentEmojiFlow: Flow<List<String>> = repository.recentEmojiFlow

    fun addRecentEmoji(emoji: String) {
        scope.launch { repository.addRecentEmoji(emoji) }
    }

    fun setHapticEnabled(enabled: Boolean) {
        scope.launch { repository.setHapticEnabled(enabled) }
    }

    fun setZoomLevel(level: Float) {
        scope.launch { repository.setZoomLevel(level) }
    }

    fun setKeyboardHeightDp(dp: Int) {
        scope.launch { repository.setKeyboardHeightDp(dp) }
    }

    fun cycleTheme(current: KeyboardTheme): KeyboardTheme {
        val themes = KeyboardTheme.entries
        return themes[(themes.indexOf(current) + 1) % themes.size]
    }

    fun setTheme(theme: KeyboardTheme) {
        scope.launch { repository.setTheme(theme) }
    }

    fun setEmojiSize(size: EmojiSize) {
        scope.launch { repository.setEmojiSize(size) }
    }
}