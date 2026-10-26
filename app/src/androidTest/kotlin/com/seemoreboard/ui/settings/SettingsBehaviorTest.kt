package com.seemoreboard.keyboard.ui.settings

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.seemoreboard.keyboard.domain.settings.EmojiSize
import com.seemoreboard.keyboard.domain.settings.KeyboardSettings
import org.junit.Rule
import org.junit.Test

class SettingsBehaviorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settingsShowsPreviewThemeAndEmojiSize() {
        setSettingsContent()

        composeRule.onNodeWithContentDescription("Keyboard preview").assertExists()
        composeRule.onNodeWithText("Active Theme: Black & White").assertExists()
        composeRule.onNodeWithText("Emoji Size").assertExists()
        composeRule.onNodeWithContentDescription("Emoji Size Large").assertExists()
    }

    @Test
    fun emojiSizeDropdownShowsAllPresets() {
        setSettingsContent()

        composeRule.onNodeWithText("Emoji Size").performScrollTo()
        composeRule.onNodeWithContentDescription("Emoji Size Large").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Standard").assertExists()
        composeRule.onAllNodesWithText("Large").assertCountEquals(2)
        composeRule.onNodeWithText("Extra large").assertExists()
    }

    @Test
    fun inactiveKeyboardShowsEnableButton() {
        setSettingsContent(isSeeMoreBoardDefault = false)

        composeRule.onNodeWithContentDescription("Tap to enable")
            .assertExists()
    }

    @Test
    fun enabledButInactiveKeyboardShowsDefaultButton() {
        setSettingsContent(isSeeMoreBoardEnabled = true, isSeeMoreBoardDefault = false)

        composeRule.onNodeWithContentDescription("Make SeeMoreBoard default")
            .assertExists()
    }

    @Test
    fun selectingEmojiSizeUpdatesDisplayedValue() {
        setSettingsContent()

        composeRule.onNodeWithText("Emoji Size").performScrollTo()
        composeRule.onNodeWithContentDescription("Emoji Size Large").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Extra large").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Emoji Size Extra large").assertExists()
    }

    @Test
    fun predictiveTextAndAutocorrectAreIndependentAndAccessible() {
        setSettingsContent()

        composeRule.onNodeWithContentDescription("Predictive text").performScrollTo()
        composeRule.onNodeWithContentDescription("Predictive text").assertIsOn().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Predictive text").assertIsOff()
        composeRule.onNodeWithContentDescription("Autocorrect").assertIsOn()
        composeRule.onNodeWithContentDescription("Autocorrect").performScrollTo()
        composeRule.onNodeWithContentDescription("Autocorrect").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Autocorrect").assertIsOff()
        composeRule.onNodeWithContentDescription("Predictive text").assertIsOff()
    }

    @Test
    fun suggestionTextSizeShowsItsCurrentValueAndSliderSemantics() {
        setSettingsContent()

        composeRule.onNodeWithText("Suggestion text size: 22 sp").assertExists()
        composeRule.onNodeWithContentDescription("Suggestion text size").assertExists()
    }

    private fun setSettingsContent(
        isSeeMoreBoardEnabled: Boolean = false,
        isSeeMoreBoardDefault: Boolean = true
    ) {
        composeRule.setContent {
            val settingsState = remember {
                mutableStateOf(KeyboardSettings(emojiSize = EmojiSize.LARGE))
            }
            SettingsContent(
                settings = settingsState.value,
                isSeeMoreBoardEnabled = isSeeMoreBoardEnabled,
                isSeeMoreBoardDefault = isSeeMoreBoardDefault,
                zoomValue = settingsState.value.zoomLevel,
                heightValue = settingsState.value.keyboardHeightDp,
                onZoomChanged = {},
                onHeightChanged = {},
                onThemeChanged = {},
                onHapticsChanged = {},
                onEmojiSizeChanged = { settingsState.value = settingsState.value.copy(emojiSize = it) },
                onPredictiveTextChanged = {
                    settingsState.value = settingsState.value.copy(predictiveTextEnabled = it)
                },
                onAutocorrectChanged = {
                    settingsState.value = settingsState.value.copy(autocorrectEnabled = it)
                },
                onSuggestionTextSizeChanged = {
                    settingsState.value = settingsState.value.copy(suggestionTextSizeSp = it)
                },
                onClose = {}
            )
        }
    }
}