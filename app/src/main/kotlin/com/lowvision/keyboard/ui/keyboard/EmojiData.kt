package com.lowvision.keyboard.ui.keyboard

data class EmojiEntry(
    val emoji: String,
    val variants: List<String> = emptyList()
)

internal fun parseEmojiCsv(lines: Sequence<String>): List<EmojiEntry> = lines
    .map { line -> line.split(',').map(String::trim).filter(String::isNotEmpty).distinct() }
    .filter { it.isNotEmpty() }
    .map { values -> EmojiEntry(values.first(), values.drop(1)) }
    .toList()

internal fun updateRecentEmoji(recent: List<String>, emoji: String, limit: Int = 30): List<String> =
    (listOf(emoji) + recent.filterNot { it == emoji }).take(limit)