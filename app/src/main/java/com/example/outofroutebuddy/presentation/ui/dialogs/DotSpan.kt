package com.example.outofroutebuddy.presentation.ui.dialogs

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan

/**
 * Draws a dot centered below the day label. Replaces MaterialCalendarView's DotSpan
 * when the library version doesn't export it.
 *
 * @param size Dot radius in pixels
 * @param color Dot color (e.g. 0xFF2196F3 for blue)
 */
class DotSpan(private val size: Int, private val color: Int) : ReplacementSpan() {
    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: android.graphics.Paint.FontMetricsInt?
    ): Int {
        return paint.measureText(text, start, end).toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val textWidth = paint.measureText(text, start, end)
        val centerX = x + textWidth / 2f
        val dotTop = bottom.toFloat()
        val dotBottom = dotTop + size * 2
        val dotCenterY = (dotTop + dotBottom) / 2f

        paint.color = color
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, dotCenterY, size.toFloat(), paint)
    }
}
