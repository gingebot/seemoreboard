package com.lowvision.keyboard.ui.components

import android.content.Context
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.lowvision.keyboard.ui.gestures.ZoomPanHandlers
import com.lowvision.keyboard.ui.keyboard.KeyboardViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun QwertyKeyboardView(
    context: Context,
    viewModel: KeyboardViewModel
): FrameLayout {
    val normalRows = listOf("QWERTYUIOP", "ASDFGHJKL")
    val bottomRow = "ZXCVBNM\u2699" // ZXCVBNM⚙

    val allKeyButtons = mutableListOf<Button>()

    val innerContainer = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }

        for (row in normalRows) {
            addView(createKeyRow(context, viewModel, row, allKeyButtons))
        }
        addView(createKeyRow(context, viewModel, bottomRow, allKeyButtons))
    }

    lateinit var root: FrameLayout

    val settingsPanelHolder = SettingsPanelHolder(context, viewModel)
    val settingsPanel = settingsPanelHolder.view.apply { visibility = View.GONE }

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
        addView(settingsPanel)
    }

    // Coroutine scope to react to state changes
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    scope.launch {
        viewModel.state.collectLatest { state ->
            val bgColor = if (state.highContrastEnabled) 0xFF000000.toInt() else 0xFF333333.toInt()
            allKeyButtons.forEach { it.setBackgroundColor(bgColor) }
            settingsPanelHolder.updateFromState(state)
        }
    }
    root.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {}
        override fun onViewDetachedFromWindow(v: View) { scope.cancel() }
    })

    return root
}

private fun createKeyRow(
    context: Context,
    viewModel: KeyboardViewModel,
    keys: String,
    keyButtons: MutableList<Button>
): LinearLayout {
    return LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        )

        for (char in keys) {
            val label = char.toString()
            val isSettings = label == "\u2699"
            val btn = KeyButtonView(context, label, highContrast = true) {
                if (isSettings) {
                    toggleSettingsPanel(this@apply)
                } else {
                    if (viewModel.state.value.hapticEnabled) {
                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                    viewModel.onKeyPress(label)
                }
            }
            addView(btn)
            if (!isSettings) keyButtons.add(btn)
        }
    }
}

private fun toggleSettingsPanel(keyRow: LinearLayout) {
    var parent: View? = keyRow.parent as? View
    while (parent != null && parent !is FrameLayout) {
        parent = parent.parent as? View
    }
    val root = parent as? FrameLayout ?: return
    for (i in (root.childCount - 1) downTo 0) {
        val child = root.getChildAt(i)
        if (child is LinearLayout && child !== root.getChildAt(0)) {
            child.visibility = if (child.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            return
        }
    }
}

private class SettingsPanelHolder(
    context: Context,
    viewModel: KeyboardViewModel
) {
    var updating = false

    lateinit var view: LinearLayout
    private val hapticsSwitch: android.widget.Switch
    private val contrastSwitch: android.widget.Switch

    init {
        val state = viewModel.state.value

        hapticsSwitch = android.widget.Switch(context).apply {
            text = "Haptics Enabled"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 22f
            isChecked = state.hapticEnabled
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 16, 0, 16) }
            setOnCheckedChangeListener { _, _ ->
                if (!updating) viewModel.toggleHaptics()
            }
        }

        contrastSwitch = android.widget.Switch(context).apply {
            text = "High-Contrast Mode"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 22f
            isChecked = state.highContrastEnabled
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 16, 0, 16) }
            setOnCheckedChangeListener { _, _ ->
                if (!updating) viewModel.toggleHighContrast()
            }
        }

        view = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(0xFF000000.toInt())
            setPadding(32, 48, 32, 48)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            addView(hapticsSwitch)
            addView(contrastSwitch)

            addView(Button(context).apply {
                text = "Close"
                textSize = 24f
                setBackgroundColor(0xFF333333.toInt())
                setTextColor(0xFFFFFFFF.toInt())
                setAllCaps(false)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 32, 0, 0) }
                setOnClickListener { view.visibility = View.GONE }
            })
        }
    }

    fun updateFromState(state: com.lowvision.keyboard.ui.keyboard.KeyboardState) {
        updating = true
        hapticsSwitch.isChecked = state.hapticEnabled
        contrastSwitch.isChecked = state.highContrastEnabled
        updating = false
    }
}