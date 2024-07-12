package com.xstory.storysnap.app.view.customview

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import androidx.core.content.ContextCompat
import com.dicoding.picodiploma.loginwithanimation.R

class Email @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : androidx.appcompat.widget.AppCompatEditText(context, attrs, defStyleAttr) {

    init {
        setup()
    }

    private fun setup() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                //TODO:NOTHING
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (!isValidEmail(s.toString())) {
                    showError(resources.getString(R.string.error_email))
                } else {
                    hideError()
                }
            }

            override fun afterTextChanged(s: Editable) {
                //TODO:NOTHING
            }
        })
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showError(errorText: String) {
        val errorIcon: Drawable? = ContextCompat.getDrawable(context, R.drawable.baseline_error_24)
        errorIcon?.setBounds(0, 0, errorIcon.intrinsicWidth, errorIcon.intrinsicHeight)
        setError(errorText, errorIcon)
        setCompoundDrawables(null, null, errorIcon, null)
    }

    private fun hideError() {
        error = null
        setCompoundDrawables(null, null, null, null)
    }
}
