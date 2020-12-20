package com.example.spans

import android.text.Editable
import android.text.Spanned
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.disposables.Disposable
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.Currency
import java.util.Locale
import timber.log.Timber

/**
 * Text watch that uses [CharAnnotationSpan] for TextView or EditText to turn their
 * numeric text to monetary text without changing the raw text for your business
 * logics.
 *
 * The alternative is to change the text directly with a [TextWatcher]. However,
 * there're at least 3 drawbacks that could bother you a lot:
 * 1. The selection would be off and recovering the cursor position could be tricky.
 * 2. You need to prevent the dead-loop that the text-changed callback is triggered
 * when you change the text in the callback.
 * 3. You need to stripe out the annotation from the data for your business logics.
 *
 * This class is created for solving all the above problems and keep your business
 * logics as clean as possible.
 */
internal object MonetaryTextWatcher {

    /**
     * The rx-wrapper for setting the monetary text watcher to [EditText] and
     * will automatically unset the watcher when you dispose the subscription.
     */
    fun applyTextView(
        textView: TextView,
        locale: Locale,
        currencyCode: String,
        isAnnotatingCharByChar: Boolean
    ): Disposable {
        val watcher = if (isAnnotatingCharByChar) {
            MonetaryCharWatcherImpl(locale, currencyCode)
        } else {
            MonetaryTextWatcherImpl(locale, currencyCode)
        }
        return Completable
            .create { emitter ->
                emitter.setCancellable {
                    textView.removeTextChangedListener(watcher)
                }
                textView.addTextChangedListener(watcher)
            }
            .subscribe()
    }

    /**
     * The actual class that enforces the annotation for char-by-char.
     */
    private class MonetaryCharWatcherImpl(
        private val locale: Locale,
        private val currencyCode: String,
    ) : TextWatcher {

        private val formatter by lazy {
            val currency = Currency.getInstance(currencyCode)
            DecimalFormat.getCurrencyInstance(locale).apply {
                // Set the currency of the transaction on the formatter so that
                // currency symbol is displayed
                setCurrency(currency)
                minimumFractionDigits = currency.defaultFractionDigits
                maximumFractionDigits = currency.defaultFractionDigits
            }
        }

        override fun beforeTextChanged(
            s: CharSequence,
            start: Int,
            count: Int,
            after: Int
        ) {
            // No-op
        }

        override fun onTextChanged(
            s: CharSequence,
            start: Int,
            before: Int,
            count: Int
        ) {
            // No-op
        }

        override fun afterTextChanged(
            s: Editable
        ) {
            // Remove the spans that are added by us for the monetary formatting.
            // However, Android UI components like TextView or EditText usually
            // create new span list so the removal doesn't really remove anything.
            // For absolute safe, we still reinforce the removal.
            val toRemoveSpans = s.getSpans(0, s.length, CharAnnotationSpan::class.java)
            for (span in toRemoveSpans) {
                s.removeSpan(span)
            }
            // Then add the formatting spans.
            try {
                val rawText = s.toString()
                val decimalText = BigDecimal(rawText)
                // Format the raw value.
                val formattedDecimal = formatter.format(decimalText)
                var offset = 0
                for (i in formattedDecimal.indices) {
                    val c = formattedDecimal[i]
                    if (!Character.isDigit(c)) {
                        val spanStart = i - offset
                        val spanEnd = i - offset + 1
                        if (spanStart >= rawText.length) break

                        s.setSpan(
                            CharAnnotationSpan(c, isPrepend = true),
                            spanStart, spanEnd,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        ++offset
                    }
                }
            } catch (error: Exception) {
                Timber.w(error)
            }
        }
    }

    /**
     * The actual class that enforces the annotation for the entire text.
     */
    private class MonetaryTextWatcherImpl(
        private val locale: Locale,
        private val currencyCode: String,
    ) : TextWatcher {

        private val formatter by lazy {
            val currency = Currency.getInstance(currencyCode)
            DecimalFormat.getCurrencyInstance(locale).apply {
                // Set the currency of the transaction on the formatter so that
                // currency symbol is displayed
                setCurrency(currency)
                minimumFractionDigits = currency.defaultFractionDigits
                maximumFractionDigits = currency.defaultFractionDigits
            }
        }

        override fun beforeTextChanged(
            s: CharSequence,
            start: Int,
            count: Int,
            after: Int
        ) {
            // No-op
        }

        override fun onTextChanged(
            s: CharSequence,
            start: Int,
            before: Int,
            count: Int
        ) {
            // No-op
        }

        override fun afterTextChanged(
            s: Editable
        ) {
            // Remove the spans that are added by us for the monetary formatting.
            // However, Android UI components like TextView or EditText usually
            // create new span list so the removal doesn't really remove anything.
            // For absolute safe, we still reinforce the removal.
            val toRemoveSpans = s.getSpans(0, s.length, TextReplacementSpan::class.java)
            for (span in toRemoveSpans) {
                s.removeSpan(span)
            }
            // Then add the formatting spans.
            try {
                val rawText = s.toString()
                if (rawText.isBlank()) return

                val decimalText = BigDecimal(rawText)
                val formattedDecimal = formatter.format(decimalText)
                s.setSpan(
                    TextReplacementSpan(formattedDecimal),
                    0, s.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } catch (error: Throwable) {
                Timber.w(error)
            }
        }
    }
}
