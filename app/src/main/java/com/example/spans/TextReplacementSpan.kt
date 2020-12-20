package com.example.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan
import kotlin.math.ceil

/**
 * Spans that replaces the text for the TextView or EditText.
 */
internal class TextReplacementSpan(
    private val replacement: CharSequence
) : ReplacementSpan() {

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return ceil(paint.measureText(replacement, 0, replacement.length)).toInt()
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
        canvas.drawText(replacement, 0, replacement.length, x, y.toFloat(), paint)
    }
}
