package com.example.spans

import android.text.InputFilter
import android.widget.TextView

/**
 * Add [filter] (and set it back to the text view)
 */
internal fun TextView.addFilter(
    filter: InputFilter
) {
    val newFilters = filters.toMutableList()
    newFilters.add(filter)
    filters = newFilters.toTypedArray()
}