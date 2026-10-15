package com.seemoreboard.keyboard.domain.settings

enum class EmojiSize(
    val displayName: String,
    val textSizeSp: Float,
    val cellHeightDp: Int,
    val columns: Int
) {
    STANDARD("Standard", 40f, 68, 5),
    LARGE("Large", 56f, 88, 4),
    EXTRA_LARGE("Extra large", 64f, 104, 3)
}