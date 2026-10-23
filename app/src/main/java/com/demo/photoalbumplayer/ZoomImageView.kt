package com.demo.photoalbumplayer

import android.content.Context
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView

class ZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    private val matrix = Matrix()
    private var scaleDetector: ScaleGestureDetector
    private var lastX = 0f
    private var lastY = 0f
    private var isDrag = false
    private var scale = 1f
    private val minScale = 1f
    private val maxScale = 5f

    init {
        scaleType = ScaleType.MATRIX
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
                isDrag = true
            }
            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress && isDrag) {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    matrix.postTranslate(dx, dy)
                    imageMatrix = matrix
                    lastX = event.x
                    lastY = event.y
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDrag = false
            }
        }
        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scale *= detector.scaleFactor
            scale = scale.coerceIn(minScale, maxScale)
            matrix.setScale(scale, scale, detector.focusX, detector.focusY)
            imageMatrix = matrix
            return true
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        resetZoom()
    

    fun resetZoom() {
        matrix.reset()
        scale = 1f
        imageMatrix = matrix
    }
}

