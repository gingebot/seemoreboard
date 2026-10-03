package com.lowvision.keyboard.ui.components

import android.content.Context
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.lowvision.keyboard.ui.gestures.ZoomPanHandlers
import com.lowvision.keyboard.ui.keyboard.KeyboardViewModel

fun QwertyKeyboardView(
    context: Context,
    viewModel: KeyboardViewModel
): FrameLayout {
    val rows = listOf(
        "QWERTYUIOP",
        "ASDFGHJKL",
        "ZXCVBNM"
    )

    val innerContainer = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }

        for (row in rows) {
            addView(createKeyRow(context, viewModel, row))
        }
    }

    lateinit var root: FrameLayout

    val handlers = ZoomPanHandlers(
        onZoomChanged = { scaleFactor ->
            val current = viewModel.state.value.zoomLevel
            viewModel.updateZoom(current * scaleFactor)
            val state = viewModel.state.value
            innerContainer.scaleX = state.zoomLevel
            innerContainer.scaleY = state.zoomLevel
            clampPan(innerContainer, root, state)
        },
        onPanChanged = { dx, dy ->
            viewModel.updatePan(dx, dy)
            val state = viewModel.state.value
            innerContainer.translationX = state.offsetX
            innerContainer.translationY = state.offsetY
            clampPan(innerContainer, root, state)
        }
    )
    handlers.createDetector(context)

    root = object : FrameLayout(context) {
        override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
            handlers.handleTouchEvent(ev)
            // When a pinch or drag gesture is active, consume the event so
            // child buttons don't receive it — prevents accidental typing.
            if (handlers.isGestureInProgress()) {
                return true
            }
            return super.dispatchTouchEvent(ev)
        }
    }.apply {
        setBackgroundColor(0xFF000000.toInt())
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        clipChildren = false
        addView(innerContainer)
    }

    return root
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
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                viewModel.onKeyPress(label)
            })
        }
    }
}

private fun clampPan(
    inner: LinearLayout,
    root: FrameLayout,
    state: com.lowvision.keyboard.ui.keyboard.KeyboardState
) {
    val innerW = inner.width * state.zoomLevel
    val innerH = inner.height * state.zoomLevel
    val rootW = root.width
    val rootH = root.height

    if (rootW > 0 && innerW > rootW) {
        val maxTx = (innerW - rootW) / 2f
        val minTx = -(innerW - rootW) / 2f
        val clampedX = state.offsetX.coerceIn(minTx, maxTx)
        if (clampedX != state.offsetX) {
            inner.translationX = clampedX
        }
    }
    if (rootH > 0 && innerH > rootH) {
        val maxTy = (innerH - rootH) / 2f
        val minTy = -(innerH - rootH) / 2f
        val clampedY = state.offsetY.coerceIn(minTy, maxTy)
        if (clampedY != state.offsetY) {
            inner.translationY = clampedY
        }
    }
}