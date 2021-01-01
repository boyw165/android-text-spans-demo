package com.example.spans

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.spans.databinding.LMainActivityBinding
import com.jakewharton.rxbinding4.widget.itemSelections
import com.jakewharton.rxbinding4.widget.textChangeEvents
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import java.util.Locale

private const val DEFAULT_BIG_DECIMAL = "9999999999"

class MainActivity : AppCompatActivity() {

    // Note: If you're browsing code with AndroidStudio, command+clicks on the
    // ActivityMainBinding could lead you to the actual layout XML!
    private val binding by lazy { LMainActivityBinding.inflate(layoutInflater) }

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initSpanPicker()
        initMonetaryTextInput()
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    private fun initSpanPicker() {
        val typeAdapter = ArrayAdapter(
            this,
            R.layout.i_span_type_in_spinner,
            listOf(
                resources.getString(R.string.item_of_full_replacement_span),
                resources.getString(R.string.item_of_character_replacement_span)
            )
        ).apply {
            setDropDownViewResource(R.layout.i_span_type_in_spinner_menu)
        }
        binding.spanTypePicker.adapter = typeAdapter
        binding.spanTypePicker.setSelection(0)
    }

    private fun initMonetaryTextInput() {
        // Reflect the raw text of the input text
        binding.monetaryTextInput.textChangeEvents()
            .subscribe { event ->
                // Note: Only the view's editable gives you the entire RAW text!
                binding.rawText.text = event.view.editableText.toString()
            }
            .addTo(disposables)

        binding.spanTypePicker.itemSelections()
            .switchMap { selPos ->
                MonetaryTextWatcher
                    .applyTextView(
                        textView = binding.monetaryTextInput,
                        locale = Locale.getDefault(),
                        currencyCode = "USD", // or JPY
                        // In the spinner, [0] is the full replacement span and [1] is
                        // character replacement.
                        isAnnotatingCharByChar = (selPos > 0)
                    )
                    // The upstream, which is a Completable, never completes.
                    // To let downstream know that the upstream has subscribed,
                    // we convert the Completable to an Observable that emits
                    // Unit on subscribe.
                    .toObservable<Unit>()
                    .startWith(Observable.defer { Observable.just(Unit) })
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                binding.monetaryTextInput.setText(DEFAULT_BIG_DECIMAL)
            }
            .addTo(disposables)

        // Customize behavior for the input text
        binding.monetaryTextInput.addFilter(DecimalInputFilter())
    }
}
