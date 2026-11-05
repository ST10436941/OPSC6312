package com.vetqueue.app.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Displays raw digits as "YYYY-MM-DD" while the underlying TextField value
 * stays plain digits. Using a VisualTransformation (instead of re-formatting
 * the string in onValueChange) keeps the cursor in the right place while
 * typing/editing in the middle of the field, so typing "20260325" always
 * lands as 2,0,2,6,-,0,3,-,2,5 in order instead of jumping to the end.
 */
class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(8)
        val formatted = InputFormatting.formatDate(digits)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = when {
                offset <= 4 -> offset
                offset <= 6 -> offset + 1
                else -> offset + 2
            }

            override fun transformedToOriginal(offset: Int): Int = when {
                offset <= 4 -> offset
                offset <= 7 -> offset - 1
                else -> offset - 2
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

/** Same idea as [DateVisualTransformation] but for "HH:MM". */
class TimeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(4)
        val formatted = InputFormatting.formatTime(digits)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (offset <= 2) offset else offset + 1

            override fun transformedToOriginal(offset: Int): Int =
                if (offset <= 2) offset else offset - 1
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}