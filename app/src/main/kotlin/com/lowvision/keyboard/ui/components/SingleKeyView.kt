package com.lowvision.keyboard.ui.components

import android.content.Context
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.ViewGroup
import android.widget.TextView
import com.lowvision.keyboard.ui.keyboard.KeyboardViewModel

fun SingleKeyView(
    context: Context,
    viewModel: KeyboardViewModel
): TextView {
    return TextView(context).apply {
        text = "A"
        textSize = 72f
        gravity = Gravity.CENTER
        setBackgroundColor(0xFF000000.toInt())
        setTextColor(0xFFFFFFFF.toInt())
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setOnClickListener {
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            viewModel.onKeyPress("A")
        }
    }
}