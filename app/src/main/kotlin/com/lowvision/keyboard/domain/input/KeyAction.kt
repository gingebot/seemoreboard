package com.lowvision.keyboard.domain.input

sealed class KeyAction {
    data class TypeCharacter(val char: Char) : KeyAction()
    object Delete : KeyAction()
    object Submit : KeyAction()
    object EmojiKeyPressed : KeyAction()
}