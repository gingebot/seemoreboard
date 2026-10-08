package com.lowvision.keyboard.ui.keyboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmojiPickerBehaviorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun selectingCategoryChangesVisibleEmoji() {
        composeRule.setContent {
            CategoryHarness()
        }

        composeRule.onNodeWithContentDescription("Emoji category Smileys").performClick()

        composeRule.onNodeWithContentDescription("Visible emoji 😀")
            .assertTextContains("😀")
    }

    @Test
    fun emptyRecentShowsAccessibleEmptyState() {
        composeRule.setContent {
            CategoryHarness()
        }

        composeRule.onNodeWithText("No recent emoji yet").assertTextContains("No recent emoji yet")
    }

    @Test
    fun selectingVariantUpdatesSelection() {
        composeRule.setContent {
            VariantHarness()
        }

        composeRule.onNodeWithContentDescription("Emoji variant 🙋‍♀️").performClick()
        composeRule.onNodeWithText("Selected 🙋‍♀️").assertTextContains("Selected 🙋‍♀️")
    }
}

@androidx.compose.runtime.Composable
private fun CategoryHarness() {
    var selected by androidx.compose.runtime.remember { mutableStateOf(0) }
    val categories = buildVisibleEmojiCategories(
        emptyList(),
        listOf(EmojiCategory("Smileys", "😀", listOf(EmojiEntry("😀"))))
    )
    Column {
        categories.forEachIndexed { index, category ->
            Text(
                text = category.icon,
                modifier = Modifier
                    .clickable { selected = index }
                    .semantics { contentDescription = "Emoji category ${category.label}" }
            )
        }
        if (categories[selected].emoji.isEmpty()) {
            Text("No recent emoji yet")
        } else {
            Text(
                categories[selected].emoji.first().emoji,
                modifier = Modifier.semantics {
                    contentDescription = "Visible emoji ${categories[selected].emoji.first().emoji}"
                }
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun VariantHarness() {
    var selected by androidx.compose.runtime.remember { mutableStateOf("🙋") }
    val variants = variantOptions(EmojiEntry("🙋", listOf("🙋‍♀️", "🙋‍♂️")))
    Column {
        variants.forEach { variant ->
            Text(
                text = variant,
                modifier = Modifier.semantics {
                    contentDescription = "Emoji variant $variant"
                }.clickable { selected = variant }
            )
        }
        Text("Selected $selected")
    }
}