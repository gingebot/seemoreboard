package com.lowvision.keyboard.ui.components

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout

fun KeyButtonView(
    context: Context,
    label: String,
    highContrast: Boolean = true,
    onClick: () -> Unit
): Button {
    val bgColor = if (highContrast) 0xFF000000.toInt() else 0xFF333333.toInt()
    return Button(context).apply {
        text = label
        textSize = 24f
        gravity = Gravity.CENTER
        setBackgroundColor(bgColor)
        setTextColor(0xFFFFFFFF.toInt())
        setOnClickListener { onClick() }

        val minSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 48f, context.resources.displayMetrics
        ).toInt()

        layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            1f
        ).apply {
            width = 0
        }
        minimumWidth = minSize
        minimumHeight = minSize
        setAllCaps(false)
        setPadding(0, 0, 0, 0)
    }
}