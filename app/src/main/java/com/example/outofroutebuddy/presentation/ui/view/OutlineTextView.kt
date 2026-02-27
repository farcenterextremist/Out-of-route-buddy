package com.example.outofroutebuddy.presentation.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * TextView that draws text with a black outline/stroke for better visibility on textured backgrounds.
 * Draws the stroke first, then the fill on top.
 */
class OutlineTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH
        color = 0xFF000000.toInt()
        textAlign = Paint.Align.CENTER
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        val text = text?.toString() ?: return
        val layout = layout ?: return

        strokePaint.textSize = textSize
        strokePaint.typeface = typeface
        strokePaint.isFakeBoldText = (typeface?.isBold == true)
        fillPaint.textSize = textSize
        fillPaint.typeface = typeface
        fillPaint.color = currentTextColor
        fillPaint.isFakeBoldText = (typeface?.isBold == true)

        val line = 0
        val lineWidth = layout.getLineWidth(line)
        val x = paddingLeft + layout.getLineLeft(line) + lineWidth / 2f
        val y = paddingTop + layout.getLineBaseline(line).toFloat()

        // Draw stroke (black outline) first
        canvas.drawText(text, x, y, strokePaint)
        // Draw fill (white text) on top
        canvas.drawText(text, x, y, fillPaint)
    }

    companion object {
        private const val STROKE_WIDTH = 3.5f
    }
}
