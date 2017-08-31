/*
 * Copyright (c) 2015 Algolia
 * http://www.algolia.com/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.algolia.musicologist

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import java.util.regex.Pattern
/**
 * Renders HTML-like attributed strings into `Spannable` instances suitable for display.
 */
class HighlightRenderer(private val context: Context) {

    fun renderHighlights(markupString: String?): Spannable {
        val result = SpannableStringBuilder()
        markupString?.let {
            val matcher = HIGHLIGHT_PATTERN.matcher(markupString)
            var p = 0 // current position in input string
            var q = 0 // current position in output string
            // For each highlight...
            while (matcher.find()) {
                // Append text before.
                result.append(markupString.substring(p, matcher.start()))
                q += matcher.start() - p

                // Append highlighted text.
                val highlightString = matcher.group(1)
                result.append(highlightString)
                result.setSpan(BackgroundColorSpan(context.resources.getColor(R.color.colorAccent)), q, q + highlightString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                q += highlightString.length
                p = matcher.end()
            }
            // Append text after.
            result.append(markupString.substring(p))
        }
        return result
    }

    companion object {

        // NOTE: This pattern is not bullet-proof (most notably against nested tags), but it is
        // sufficient for our purposes.
        internal val HIGHLIGHT_PATTERN = Pattern.compile("<em>([^<]*)</em>")
    }
}
