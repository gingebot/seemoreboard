package com.lowvision.keyboard.ui.settings

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.lowvision.keyboard.domain.settings.EmojiSize
import com.lowvision.keyboard.domain.settings.KeyboardSettings
import com.lowvision.keyboard.ui.theme.KeyboardTheme
import org.junit.Rule
import org.junit.Test

class SettingsBehaviorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settingsShowsPreviewThemeAndEmojiSize() {
        setSettingsContent()

        composeRule.onNodeWithContentDescription("Keyboard preview").assertTextContains("Keyboard preview")
        composeRule.onNodeWithText("Active Theme: Black & White").assertTextContains("Active Theme")
        composeRule.onNodeWithText("Emoji Size").assertTextContains("Emoji Size")
        composeRule.onNodeWithContentDescription("Emoji Size Large").assertTextContains("Large")
    }

    @Test
    fun emojiSizeDropdownShowsAllPresets() {
        setSettingsContent()

        composeRule.onNodeWithContentDescription("Emoji Size Large").performClick()
        composeRule.onNodeWithText("Standard").assertTextContains("Standard")
        composeRule.onNodeWithText("Large").assertTextContains("Large")
        composeRule.onNodeWithText("Extra large").assertTextContains("Extra large")
    }

    @Test
    fun inactiveKeyboardShowsEnableButton() {
        setSettingsContent(isSeeMoreBoardDefault = false)

        composeRule.onNodeWithContentDescription("Tap to enable SeeMoreBoard keyboard")
            .assertTextContains("Tap to enable")
    }

    @Test
    fun selectingEmojiSizeUpdatesDisplayedValue() {
        setSettingsContent()

        composeRule.onNodeWithContentDescription("Emoji Size Large").performClick()
        composeRule.onNodeWithText("Extra large").performClick()
        composeRule.onNodeWithContentDescription("Emoji Size Extra large").assertTextContains("Extra large")
    }

    private fun setSettingsContent(isSeeMoreBoardDefault: Boolean = true) {
        composeRule.setContent {
            val settingsState = remember {
                mutableStateOf(KeyboardSettings(emojiSize = EmojiSize.LARGE))
            }
            SettingsContent(
                settings = settingsState.value,
                isSeeMoreBoardDefault = isSeeMoreBoardDefault,
                zoomValue = settingsState.value.zoomLevel,
                heightValue = settingsState.value.keyboardHeightDp,
                onZoomChanged = {},
                onHeightChanged = {},
                onThemeChanged = {},
                onHapticsChanged = {},
                onEmojiSizeChanged = { settingsState.value = settingsState.value.copy(emojiSize = it) },
                onClose = {}
            )
        }
    }
}