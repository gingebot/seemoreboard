package com.lowvision.keyboard.ime.integration

import android.view.KeyEvent
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
        inputConnection?.deleteSurroundingText(1, 0)
    }

    fun submit() {
        inputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        inputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }

    fun commitText(text: String) {
        inputConnection?.commitText(text, 1)
    }

    fun sendKeyEvent(keyEvent: KeyEvent) {
        inputConnection?.sendKeyEvent(keyEvent)
    }
}