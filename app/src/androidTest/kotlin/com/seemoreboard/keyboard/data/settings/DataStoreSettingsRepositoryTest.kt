package com.seemoreboard.keyboard.data.settings

import androidx.test.platform.app.InstrumentationRegistry
import com.seemoreboard.keyboard.domain.settings.SuggestionTextSize
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DataStoreSettingsRepositoryTest {
    @Test
    fun newSettingsPersistIndependentlyAndClampSuggestionSize() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = DataStoreSettingsRepository(context)
        val original = repository.settingsFlow.first()

        try {
            repository.setPredictiveTextEnabled(false)
            repository.setAutocorrectEnabled(true)
            repository.setSuggestionTextSizeSp(SuggestionTextSize.MAX_SP + 20)

            val reloaded = DataStoreSettingsRepository(context).settingsFlow.first()
            assertEquals(false, reloaded.predictiveTextEnabled)
            assertEquals(true, reloaded.autocorrectEnabled)
            assertEquals(SuggestionTextSize.MAX_SP, reloaded.suggestionTextSizeSp)

            repository.setPredictiveTextEnabled(true)
            repository.setAutocorrectEnabled(false)
            repository.setSuggestionTextSizeSp(SuggestionTextSize.MIN_SP - 20)

            val secondReload = DataStoreSettingsRepository(context).settingsFlow.first()
            assertEquals(true, secondReload.predictiveTextEnabled)
            assertEquals(false, secondReload.autocorrectEnabled)
            assertEquals(SuggestionTextSize.MIN_SP, secondReload.suggestionTextSizeSp)
        } finally {
            repository.setPredictiveTextEnabled(original.predictiveTextEnabled)
            repository.setAutocorrectEnabled(original.autocorrectEnabled)
            repository.setSuggestionTextSizeSp(original.suggestionTextSizeSp)
        }
    }
}