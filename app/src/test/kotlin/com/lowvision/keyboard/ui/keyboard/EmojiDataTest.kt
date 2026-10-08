package com.lowvision.keyboard.ui.keyboard

import com.lowvision.keyboard.ime.integration.InputConnectionBridge
import org.junit.Assert.assertEquals
import org.junit.Test

class EmojiDataTest {
    @Test
    fun parsesBaseEmojiAndVariants() {
        val entries = parseEmojiCsv(sequenceOf("🙋,🙋‍♀️,🙋🏻‍♀️", "😀"))

        assertEquals("🙋", entries[0].emoji)
        assertEquals(listOf("🙋‍♀️", "🙋🏻‍♀️"), entries[0].variants)
        assertEquals("😀", entries[1].emoji)
    }

    @Test
    fun updatesRecentEmojiMostRecentFirstWithoutDuplicates() {
        assertEquals(
            listOf("😀", "😂", "😍"),
            updateRecentEmoji(listOf("😂", "😀", "😍"), "😀")
        )
    }

    @Test
    fun limitsRecentEmoji() {
        val recent = (0 until 4).map { it.toString() }
        assertEquals(listOf("new", "0", "1"), updateRecentEmoji(recent, "new", limit = 3))
    }

    @Test
    fun countsSurrogatePairAsOneEmoji() {
        assertEquals(1, InputConnectionBridge.precedingGraphemeCodePointCount("a😀"))
    }

    @Test
    fun countsZwjSequenceAsOneGrapheme() {
        assertEquals(
            7,
            InputConnectionBridge.precedingGraphemeCodePointCount("x👨‍👩‍👧‍👦")
        )
    }
}