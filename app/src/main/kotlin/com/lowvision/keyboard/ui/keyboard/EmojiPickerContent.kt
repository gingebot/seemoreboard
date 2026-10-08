package com.lowvision.keyboard.ui.keyboard

import android.view.HapticFeedbackConstants
import androidx.emoji2.text.EmojiCompat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.lowvision.keyboard.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EmojiPickerContent(viewModel: KeyboardViewModel) {
    val context = LocalContext.current
    val view = LocalView.current
    val state by viewModel.state.collectAsState()
    val categories = remember(context) { loadEmojiCategories(context) }
    var selectedCategory by remember { mutableStateOf(0) }
    var selectedVariants by remember { mutableStateOf<List<String>?>(null) }
    val categoryScrollState = rememberLazyListState()
    val visibleCategories = listOf(
        EmojiCategory("Recent", "🕘", state.recentEmoji.map { EmojiEntry(it) })
    ) + categories

    LaunchedEffect(selectedCategory) {
        categoryScrollState.animateScrollToItem(selectedCategory)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Back to keyboard",
            color = Color.White,
            fontSize = 24.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF333333))
                .clickable { viewModel.closeEmojiPicker() }
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .semantics { contentDescription = "Close emoji picker" }
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF222222))
                .padding(8.dp),
            state = categoryScrollState,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(visibleCategories, key = { _, category -> category.label }) { index, category ->
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(if (isPressed) 1.06f else 1f)
                Text(
                    text = category.icon,
                    color = if (index == selectedCategory) Color.Black else Color.White,
                    fontSize = state.emojiSize.textSizeSp.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier
                        .scale(scale)
                        .size(state.emojiSize.cellHeightDp.dp)
                        .background(
                            if (index == selectedCategory) Color.White else Color(0xFF444444),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            if (state.hapticEnabled) {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            }
                            selectedCategory = index
                        }
                        .padding(4.dp)
                        .semantics { contentDescription = "Emoji category ${category.label}" }
                )
            }
        }
        if (visibleCategories[selectedCategory].emoji.isEmpty()) {
            Text(
                text = "No recent emoji yet",
                color = Color.White,
                fontSize = 24.sp,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp)
                    .semantics { contentDescription = "No recent emoji yet" }
            )
        } else LazyVerticalGrid(
            columns = GridCells.Fixed(state.emojiSize.columns),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A)),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(visibleCategories[selectedCategory].emoji, key = { it.emoji }) { entry ->
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(state.emojiSize.cellHeightDp.dp)
                        .background(Color(0xFF333333), RoundedCornerShape(8.dp))
                        .semantics { contentDescription = "Emoji ${entry.emoji}" },
                    factory = {
                        android.widget.TextView(context).apply {
                            gravity = android.view.Gravity.CENTER
                            setTextColor(android.graphics.Color.WHITE)
                            setTextSize(
                                android.util.TypedValue.COMPLEX_UNIT_SP,
                                state.emojiSize.textSizeSp
                            )
                            isFocusable = false
                            isClickable = true
                            isLongClickable = true
                            configureEmojiCellInteraction(
                                view = this,
                                hapticEnabled = state.hapticEnabled,
                                onClick = { viewModel.commitEmoji(entry.emoji) },
                                onLongClick = {
                                if (entry.variants.isNotEmpty()) {
                                    selectedVariants = listOf(entry.emoji) + entry.variants
                                    true
                                } else {
                                    false
                                }
                                }
                            )
                        }
                    },
                    update = { cell ->
                        cell.setTextSize(
                            android.util.TypedValue.COMPLEX_UNIT_SP,
                            state.emojiSize.textSizeSp
                        )
                        cell.text = processEmojiForDisplay(entry.emoji)
                    }
                )
            }
        }
        }
        selectedVariants?.let { variants ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x99000000))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { selectedVariants = null }
                    .semantics { contentDescription = "Close emoji variant chooser" }
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .align(Alignment.Center)
                    .background(Color(0xFF1A1A1A))
                    .padding(16.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Choose emoji variant",
                        color = Color.White,
                        fontSize = 24.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Cancel",
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .background(Color(0xFF444444), RoundedCornerShape(8.dp))
                            .clickable { selectedVariants = null }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .semantics { contentDescription = "Cancel emoji variant chooser" }
                    )
                }
                LazyVerticalGrid(columns = GridCells.Fixed(state.emojiSize.columns)) {
                    items(variants, key = { it }) { variant ->
                        AndroidView(
                            modifier = Modifier
                                .height(state.emojiSize.cellHeightDp.dp)
                                .fillMaxWidth()
                                .semantics { contentDescription = "Emoji variant $variant" }
                            ,
                            factory = {
                                android.widget.TextView(context).apply {
                                    gravity = android.view.Gravity.CENTER
                                    setTextColor(android.graphics.Color.WHITE)
                                    setTextSize(
                                        android.util.TypedValue.COMPLEX_UNIT_SP,
                                        state.emojiSize.textSizeSp
                                    )
                                    isClickable = true
                                    configureEmojiCellInteraction(
                                        view = this,
                                        hapticEnabled = state.hapticEnabled,
                                        onClick = {
                                            selectedVariants = null
                                            viewModel.commitEmoji(variant)
                                        },
                                        onLongClick = { false }
                                    )
                                }
                            },
                            update = { cell ->
                                cell.setTextSize(
                                    android.util.TypedValue.COMPLEX_UNIT_SP,
                                    state.emojiSize.textSizeSp
                                )
                                cell.text = processEmojiForDisplay(variant)
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun configureEmojiCellInteraction(
    view: android.widget.TextView,
    hapticEnabled: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Boolean
) {
    val normalBackground = android.graphics.drawable.GradientDrawable().apply {
        setColor(android.graphics.Color.TRANSPARENT)
        cornerRadius = 8f
    }
    val pressedBackground = android.graphics.drawable.GradientDrawable().apply {
        setColor(android.graphics.Color.rgb(102, 102, 102))
        cornerRadius = 8f
    }
    view.background = normalBackground
    view.setOnClickListener { onClick() }
    view.setOnLongClickListener { onLongClick() }
    view.setOnTouchListener { touchedView, event ->
        when (event.actionMasked) {
            android.view.MotionEvent.ACTION_DOWN -> {
                touchedView.background = pressedBackground
                if (hapticEnabled) {
                    touchedView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                touchedView.animate()
                    .scaleX(1.08f)
                    .scaleY(1.08f)
                    .setDuration(70L)
                    .start()
            }
            android.view.MotionEvent.ACTION_UP,
            android.view.MotionEvent.ACTION_CANCEL -> {
                touchedView.background = normalBackground
                touchedView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(90L)
                    .start()
            }
        }
        false
    }
}

private data class EmojiCategory(
    val label: String,
    val icon: String,
    var emoji: List<EmojiEntry>
)

private fun loadEmojiCategories(context: android.content.Context): List<EmojiCategory> {
    val definitions = listOf(
        "Smileys" to R.raw.emoji_category_emotions,
        "People" to R.raw.emoji_category_people,
        "Animals" to R.raw.emoji_category_animals_nature,
        "Food" to R.raw.emoji_category_food_drink,
        "Travel" to R.raw.emoji_category_travel_places,
        "Activities" to R.raw.emoji_category_activity,
        "Objects" to R.raw.emoji_category_objects,
        "Symbols" to R.raw.emoji_category_symbols,
        "Flags" to R.raw.emoji_category_flags
    )
    return definitions.map { (label, resourceId) ->
        EmojiCategory(label, iconForCategory(label), context.resources.openRawResource(resourceId).bufferedReader().useLines { lines ->
            parseEmojiCsv(lines).toList()
        })
    }
}

private fun iconForCategory(label: String): String = when (label) {
    "Smileys" -> "😀"
    "People" -> "🙋"
    "Animals" -> "🐶"
    "Food" -> "🍎"
    "Travel" -> "🚗"
    "Activities" -> "🎉"
    "Objects" -> "💡"
    "Symbols" -> "❤️"
    "Flags" -> "🏳️"
    else -> "🙂"
}

private fun processEmojiForDisplay(emoji: String): CharSequence {
    return if (EmojiCompat.isConfigured()) {
        EmojiCompat.get().process(emoji) ?: emoji
    } else {
        emoji
    }
}

