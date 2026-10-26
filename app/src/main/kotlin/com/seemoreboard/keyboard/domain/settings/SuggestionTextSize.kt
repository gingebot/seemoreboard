package com.seemoreboard.keyboard.domain.settings

object SuggestionTextSize {
    const val MIN_SP = 16
    const val DEFAULT_SP = 22
    const val MAX_SP = 32
    const val STEP_SP = 2

    fun clamp(sizeSp: Int): Int = sizeSp.coerceIn(MIN_SP, MAX_SP)
}