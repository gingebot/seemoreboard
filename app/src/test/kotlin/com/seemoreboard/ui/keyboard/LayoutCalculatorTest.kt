package com.seemoreboard.keyboard.ui.keyboard

import com.seemoreboard.keyboard.domain.layout.LayoutCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class LayoutCalculatorTest {

    @Test
    fun numericSymbolsLayoutMatchesSpecification() {
        assertEquals(
            listOf(
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
                listOf("@", "#", "£", "_", "&", "-", "+", "(", ")", "/"),
                listOf("▶=<", "*", "'", "\"", ":", ";", "!", "?", "▶⌫"),
                listOf("▶ABC", ",", "▶Space", ".", "▶⏎")
            ),
            LayoutCalculator.getLayout(KeyboardLayer.NUMERIC_SYMBOLS)
        )
    }

    @Test
    fun extendedSymbolsLayoutMatchesSpecification() {
        assertEquals(
            listOf(
                listOf("~", "|", "√", "π", "×", "§", "Δ"),
                listOf("€", "¥", "$", "¢", "^", "{", "}", "\\"),
                listOf("▶?123", "%", "©", "™", "✓", "[", "]", "▶⌫"),
                listOf("▶ABC", "<", "▶Space", ">", "▶⏎")
            ),
            LayoutCalculator.getLayout(KeyboardLayer.EXTENDED_SYMBOLS)
        )
    }

    @Test
    fun alphabetLayoutRemainsTheDefaultLayer() {
        assertEquals("q", LayoutCalculator.getLayout(KeyboardLayer.ALPHA).first().first())
        assertEquals("▶123?", LayoutCalculator.getLayout(KeyboardLayer.ALPHA).last().first())
    }
}