package com.seemoreboard.keyboard.domain.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class SuggestionTextSizeTest {
    @Test
    fun clampKeepsValuesWithinSupportedRange() {
        assertEquals(SuggestionTextSize.MIN_SP, SuggestionTextSize.clamp(Int.MIN_VALUE))
        assertEquals(SuggestionTextSize.DEFAULT_SP, SuggestionTextSize.clamp(SuggestionTextSize.DEFAULT_SP))
        assertEquals(SuggestionTextSize.MAX_SP, SuggestionTextSize.clamp(Int.MAX_VALUE))
    }

    @Test
    fun predictiveSettingsDefaultEnabledAndTextSizeDefaultsTo22Sp() {
        val settings = KeyboardSettings()

        assertEquals(true, settings.predictiveTextEnabled)
        assertEquals(true, settings.autocorrectEnabled)
        assertEquals(SuggestionTextSize.DEFAULT_SP, settings.suggestionTextSizeSp)
    }
}