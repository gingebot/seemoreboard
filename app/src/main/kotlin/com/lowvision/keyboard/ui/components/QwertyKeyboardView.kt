package com.lowvision.keyboard.ui.components

import android.content.Context
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.widget.LinearLayout
import com.lowvision.keyboard.ui.keyboard.KeyboardViewModel

fun QwertyKeyboardView(
    context: Context,
    viewModel: KeyboardViewModel
): LinearLayout {
    val rows = listOf(
        "QWERTYUIOP",
        "ASDFGHJKL",
        "ZXCVBNM"
    )

    return LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setBackgroundColor(0xFF000000.toInt())
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        for (row in rows) {
            addView(createKeyRow(context, viewModel, row))
        }
    }
}

private fun createKeyRow(
    context: Context,
    viewModel: KeyboardViewModel,
    keys: String
): LinearLayout {
    return LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        )

        for (char in keys) {
            val label = char.toString()
            addView(KeyButtonView(context, label) {
                this.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                viewModel.onKeyPress(label)
            })
        }
    }
}