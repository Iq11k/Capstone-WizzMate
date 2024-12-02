package com.bangkit.wizzmateapp.helper

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

object StringHelper {
    fun makeTextLink(
        textView: TextView,
        str: String,
        underlined: Boolean,
        @ColorRes colorResId: Int?,
        action: (() -> Unit)? = null
    ) {
        val spannableString = SpannableString(textView.text)
        val textColor = colorResId?.let {
            ContextCompat.getColor(textView.context, it)
        } ?: textView.currentTextColor

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                action?.invoke()
            }

            override fun updateDrawState(drawState: TextPaint) {
                super.updateDrawState(drawState)
                drawState.isUnderlineText = underlined
                drawState.color = textColor
            }
        }

        val index = spannableString.indexOf(str)
        if (index >= 0) {
            spannableString.setSpan(
                StyleSpan(Typeface.BOLD),
                index,
                index + str.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannableString.setSpan(
                clickableSpan,
                index,
                index + str.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
    }
}