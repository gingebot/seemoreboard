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
            innerContainer.pivotX = innerContainer.width / 2f
            innerContainer.pivotY = innerContainer.height / 2f
            innerContainer.scaleX = state.zoomLevel
            innerContainer.scaleY = state.zoomLevel

            // Re-clamp pan after zoom change.  With centered pivot,
            // higher zoom pushes edges equally outward; bounds allow
            // positive translation to see the left/top and negative
            // to see the right/bottom.
            val innerW = innerContainer.width * state.zoomLevel
            val innerH = innerContainer.height * state.zoomLevel
            val rw = root.width.toFloat()
            val rh = root.height.toFloat()

            if (innerW <= rw && innerH <= rh) {
                if (state.offsetX != 0f || state.offsetY != 0f) {
                    viewModel.updatePan(-state.offsetX, -state.offsetY)
                }
                innerContainer.translationX = 0f
                innerContainer.translationY = 0f
            } else {
                val maxTx = rw * (state.zoomLevel - 1f) / 2f
                val minTx = rw * (1f - state.zoomLevel) / 2f
                val maxTy = rh * (state.zoomLevel - 1f) / 2f
                val minTy = rh * (1f - state.zoomLevel) / 2f

                val clampedX = state.offsetX.coerceIn(minTx, maxTx)
                val clampedY = state.offsetY.coerceIn(minTy, maxTy)
                if (clampedX != state.offsetX || clampedY != state.offsetY) {
                    viewModel.updatePan(clampedX - state.offsetX, clampedY - state.offsetY)
                }
                innerContainer.translationX = clampedX
                innerContainer.translationY = clampedY
            }
        },
        onPanChanged = { dx, dy ->
            val state = viewModel.state.value
            val innerW = innerContainer.width * state.zoomLevel
            val innerH = innerContainer.height * state.zoomLevel
            val rw = root.width.toFloat()
            val rh = root.height.toFloat()

            if (innerW <= rw && innerH <= rh) {
                // Content fits — reset any stale offsets
                if (state.offsetX != 0f || state.offsetY != 0f) {
                    viewModel.updatePan(-state.offsetX, -state.offsetY)
                }
                innerContainer.translationX = 0f
                innerContainer.translationY = 0f
            } else {
                // Clamp the proposed new offset BEFORE applying, so
                // ViewModel and view stay in sync.  Bounds account for
                // centered pivot: positive translation sees left/top,
                // negative sees right/bottom.
                val maxTx = rw * (state.zoomLevel - 1f) / 2f
                val minTx = rw * (1f - state.zoomLevel) / 2f
                val maxTy = rh * (state.zoomLevel - 1f) / 2f
                val minTy = rh * (1f - state.zoomLevel) / 2f

                val newX = (state.offsetX + dx).coerceIn(minTx, maxTx)
                val newY = (state.offsetY + dy).coerceIn(minTy, maxTy)

                if (newX != state.offsetX || newY != state.offsetY) {
                    viewModel.updatePan(newX - state.offsetX, newY - state.offsetY)
                    innerContainer.translationX = newX
                    innerContainer.translationY = newY
                }
            }
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