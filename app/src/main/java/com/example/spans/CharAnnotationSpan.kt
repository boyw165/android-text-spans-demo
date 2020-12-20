package com.example.spans

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan
import kotlin.math.ceil

/**
 * Spans for annotating the TextView or EditText with a character.
 */
internal class CharAnnotationSpan(
    private val symbol: Char,
    private val isPrepend: Boolean = true
) : ReplacementSpan() {

    private val textBuilder = StringBuilder()

    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        textBuilder.apply {
            // Keep the buffer but change the index so that the string is rebuilt
            // without reallocation.
            setLength(0)
            if (isPrepend) {
                append(symbol)
                append(text, start, end)
            } else {
                append(text, start, end)
                append(symbol)
            }
        }
        return ceil(paint.measureText(textBuilder.toString(), 0, textBuilder.length)).toInt()
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
        canvas.drawText(textBuilder.toString(), 0, textBuilder.length, x, y.toFloat(), paint)
    }
}
