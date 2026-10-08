package com.lowvision.keyboard.ime.integration

import android.view.KeyEvent
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection

class InputConnectionBridge {

    private var inputConnection: InputConnection? = null

    fun setInputConnection(ic: InputConnection?) {
        inputConnection = ic
    }

    fun clearInputConnection() {
        inputConnection = null
    }

    fun typeCharacter(text: String) {
        inputConnection?.commitText(text, 1)
    }

    fun deleteCharacter() {
        val connection = inputConnection ?: return
        val beforeCursor = connection.getTextBeforeCursor(MAX_GRAPHEME_LOOKBACK, 0)?.toString()
        val codePointCount = beforeCursor?.let(::precedingGraphemeCodePointCount) ?: 1
        connection.deleteSurroundingTextInCodePoints(codePointCount.coerceAtLeast(1), 0)
    }

    fun submit() {
        inputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        inputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }

    fun commitText(text: String) {
        inputConnection?.let { connection ->
            connection.commitText(text, 1)
        }
    }

    fun refreshCursor() {
        inputConnection?.let(::refreshCursor)
    }

    fun sendKeyEvent(keyEvent: KeyEvent) {
        inputConnection?.sendKeyEvent(keyEvent)
    }

    private fun refreshCursor(connection: InputConnection) {
        val extracted = connection.getExtractedText(ExtractedTextRequest(), 0) ?: return
        val selectionStart = extracted.selectionStart
        val selectionEnd = extracted.selectionEnd
        if (selectionStart >= 0 && selectionEnd >= 0) {
            connection.setSelection(selectionStart, selectionEnd)
        }
    }

    companion object {
        private const val MAX_GRAPHEME_LOOKBACK = 32

        internal fun precedingGraphemeCodePointCount(text: String): Int {
            if (text.isEmpty()) return 1
            var index = text.length
            var count = 0
            var regionalIndicators = 0

            while (index > 0) {
                val codePoint = text.codePointBefore(index)
                val charCount = Character.charCount(codePoint)
                index -= charCount
                count++

                val isJoiner = codePoint == ZERO_WIDTH_JOINER
                val isModifier = codePoint in SKIN_TONE_START..SKIN_TONE_END
                val isVariationSelector = codePoint in VARIATION_SELECTOR_START..VARIATION_SELECTOR_END
                val isRegionalIndicator = codePoint in REGIONAL_INDICATOR_START..REGIONAL_INDICATOR_END

                if (isRegionalIndicator) {
                    regionalIndicators++
                }
                if (isJoiner || isModifier || isVariationSelector) continue
                if (isRegionalIndicator && regionalIndicators % 2 == 1) continue
                if (index > 0 && text.codePointBefore(index) == ZERO_WIDTH_JOINER) continue
                break
            }
            return count
        }

        private const val ZERO_WIDTH_JOINER = 0x200D
        private const val SKIN_TONE_START = 0x1F3FB
        private const val SKIN_TONE_END = 0x1F3FF
        private const val VARIATION_SELECTOR_START = 0xFE00
        private const val VARIATION_SELECTOR_END = 0xFE0F
        private const val REGIONAL_INDICATOR_START = 0x1F1E6
        private const val REGIONAL_INDICATOR_END = 0x1F1FF
    }
}