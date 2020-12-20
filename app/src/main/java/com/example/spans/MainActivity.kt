package com.example.spans

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.spans.databinding.ActivityMainBinding
import com.jakewharton.rxbinding4.widget.textChangeEvents
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // Note: If you're browsing code with AndroidStudio, command+clicks on the
    // ActivityMainBinding could lead you to the actual layout XML!
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initMonetaryTextInput()
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    private fun initMonetaryTextInput() {
        // Reflect the raw text of the input text
        binding.monetaryTextInput.textChangeEvents()
            .subscribe { event ->
                // Note: Only the view's editable gives you the entire RAW text!
                binding.rawText.text = event.view.editableText.toString()
            }
            .addTo(disposables)

        // Customize behavior for the input text
        binding.monetaryTextInput.addFilter(DecimalInputFilter())
        MonetaryTextWatcher
            .applyTextView(
                textView = binding.monetaryTextInput,
                locale = Locale.getDefault(),
                currencyCode = "USD"
            )
            .addTo(disposables)
    }
}
