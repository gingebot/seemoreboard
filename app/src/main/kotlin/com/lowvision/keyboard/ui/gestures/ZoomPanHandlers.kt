package com.lowvision.keyboard.ui.gestures

import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import kotlin.math.abs

class ZoomPanHandlers(
    private val onZoomChanged: (Float) -> Unit,
    private val onPanChanged: (Float, Float) -> Unit
) {
    companion object {
        const val MIN_ZOOM = 1.0f
        const val MAX_ZOOM = 2.5f
    }

    private var scaleDetector: ScaleGestureDetector? = null
    private var isScaling = false

    private var dragStartX = 0f
    private var dragStartY = 0f
    private var hasMoved = false

    fun createDetector(context: Context) {
        scaleDetector = ScaleGestureDetector(context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                    isScaling = true
                    hasMoved = true
                    return true
                }

                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    onZoomChanged(detector.scaleFactor)
                    return true
                }

                override fun onScaleEnd(detector: ScaleGestureDetector) {
                    isScaling = false
                }
            })
    }

    fun handleTouchEvent(event: MotionEvent): Boolean {
        scaleDetector?.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isScaling = false
                hasMoved = false
                dragStartX = event.x
                dragStartY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isScaling) {
                    val dx = event.x - dragStartX
                    val dy = event.y - dragStartY
                    dragStartX = event.x
                    dragStartY = event.y
                    if (abs(dx) > 2f || abs(dy) > 2f) {
                        hasMoved = true
                        onPanChanged(dx, dy)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // hasMoved intentionally not reset — isGestureInProgress()
                // must stay true through ACTION_UP to block accidental clicks.
            }
        }
        return false
    }

    fun isGestureInProgress(): Boolean = isScaling || hasMoved
}