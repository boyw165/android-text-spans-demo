package com.example.spans

import android.text.InputFilter
import android.text.Spanned
import android.widget.EditText
import java.math.BigDecimal

/**
 * The [InputFilter] for decimals for [EditText].
 */
internal class DecimalInputFilter : InputFilter {

    private val textBuilder = StringBuilder()

    @Suppress("Detekt.TooGenericExceptionCaught")
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val result = textBuilder.apply {
            setLength(0)
            append(dest, 0, dstart)
            append(source, start, end)
            append(dest, dend, dest.length)
        }.toString()
        // Just let the delete-to-empty case through
        if (result.isEmpty()) return null

        return try {
            // Use the big-decimal class to validate the text.
            val formatted = BigDecimal(result).toString()

            if (formatted == result) {
                // Null for not changing anything so that the subsequent filters can
                // still functioning.
                null
            } else {
                throw NumberFormatException()
            }
        } catch (error: Exception) {
            // The result becomes invalid after adding the source, so we simply
            // drop the entire source.
            ""
        }
    }
}
