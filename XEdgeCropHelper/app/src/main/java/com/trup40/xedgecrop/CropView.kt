package com.trup40.xedgecrop

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CropView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f
    private var isDrawing = false

    // Semi-transparent black brush to slightly darken the screen
    private val bgPaint = Paint().apply {
        color = Color.parseColor("#80000000") // 50% black
    }

    // Brush to "punch a hole" in the selected area and show the screen behind it
    private val clearPaint = Paint().apply {
        color = Color.TRANSPARENT
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    // White border around the selection area
    private val borderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    // Callback event to send the coordinates to MainActivity when selection is finished
    var onSelectionFinished: ((RectF) -> Unit)? = null

    init {
        // Disable hardware acceleration at the View level for the "punch hole" (CLEAR) operation to work
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Make the entire screen semi-transparent black
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        if (isDrawing) {
            // The user might draw from right to left or bottom to top, so fix the bounds
            val left = minOf(startX, endX)
            val top = minOf(startY, endY)
            val right = maxOf(startX, endX)
            val bottom = maxOf(startY, endY)
            val rect = RectF(left, top, right, bottom)

            // 2. Clear the selected area (reveal the screen)
            canvas.drawRect(rect, clearPaint)

            // 3. Draw a white border around it
            canvas.drawRect(rect, borderPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> { // When the finger touches the screen
                startX = event.x
                startY = event.y
                endX = event.x
                endY = event.y
                isDrawing = true
                invalidate() // Trigger onDraw
                return true
            }
            MotionEvent.ACTION_MOVE -> { // While dragging the finger
                endX = event.x
                endY = event.y
                invalidate()
            }
            MotionEvent.ACTION_UP -> { // When the finger is lifted from the screen
                endX = event.x
                endY = event.y
                invalidate()

                val left = minOf(startX, endX)
                val top = minOf(startY, endY)
                val right = maxOf(startX, endX)
                val bottom = maxOf(startY, endY)
                val cropRect = RectF(left, top, right, bottom)

                // Ignore very small accidental touches (clicks)
                if (cropRect.width() > 20 && cropRect.height() > 20) {
                    onSelectionFinished?.invoke(cropRect)
                }
            }
        }
        return super.onTouchEvent(event)
    }
}