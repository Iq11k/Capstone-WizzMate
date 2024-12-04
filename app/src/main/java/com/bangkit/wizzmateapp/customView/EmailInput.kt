package com.bangkit.wizzmateapp

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class EmailInput @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : TextInputEditText(context, attrs) {
    private lateinit var textInputLayout: TextInputLayout

    init {
        addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                textInputLayout = parent.parent as TextInputLayout
                textInputLayout.errorIconDrawable = null
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(p0.toString()).matches()) {
                    textInputLayout.error = "Format Email salah"
                } else {
                    textInputLayout.isErrorEnabled = false
                    textInputLayout.error = null
                }

            }

            override fun afterTextChanged(p0: Editable?) {
                if (p0.toString().isEmpty()) {
                    textInputLayout.error = "Email tidak boleh kosong"
                }
            }

        })
    }
}