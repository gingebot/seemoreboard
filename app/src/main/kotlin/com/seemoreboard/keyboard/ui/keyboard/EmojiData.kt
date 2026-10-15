package com.seemoreboard.keyboard.ui.keyboard

data class EmojiEntry(
    val emoji: String,
    val variants: List<String> = emptyList()
)

internal data class EmojiCategory(
    val label: String,
    val icon: String,
    val emoji: List<EmojiEntry>
)

internal fun buildVisibleEmojiCategories(
    recent: List<String>,
    categories: List<EmojiCategory>
): List<EmojiCategory> = listOf(
    EmojiCategory("Recent", "🕘", recent.map(::EmojiEntry))
) + categories

internal fun selectedEmojiCategoryIndex(selected: Int, categoryCount: Int): Int {
    if (categoryCount <= 0) return 0
    return selected.coerceIn(0, categoryCount - 1)
}

internal fun variantOptions(entry: EmojiEntry): List<String> =
    listOf(entry.emoji) + entry.variants

internal fun parseEmojiCsv(lines: Sequence<String>): List<EmojiEntry> = lines
    .map { line -> line.split(',').map(String::trim).filter(String::isNotEmpty).distinct() }
    .filter { it.isNotEmpty() }
    .map { values -> EmojiEntry(values.first(), values.drop(1)) }
    .toList()

internal fun updateRecentEmoji(recent: List<String>, emoji: String, limit: Int = 30): List<String> =
    (listOf(emoji) + recent.filterNot { it == emoji }).take(limit)