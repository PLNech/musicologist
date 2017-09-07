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

package com.algolia.musicologist.model

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import java.util.*

/**
 * An highlighted results holds a data model object along with any number of highlights for this
 * object's attributes.

 * @param <T> The data model type.
</T> */
class HighlightedSong(val song: Song) {
    private val highlights = HashMap<String, Highlight>()

    fun addHighlight(attributeName: String, value: String): HighlightedSong {
        highlights[attributeName] = Highlight(attributeName, value)
        return this
    }

    operator fun get(attribute: String): Highlight = highlights[attribute] ?: Highlight(attribute, song.json.getString(attribute))

    fun play(context: Context) {
        //FIXME Artist not sent to Spotify
        val intent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)
        intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Media.ENTRY_CONTENT_TYPE)
                .putExtra(MediaStore.EXTRA_MEDIA_ARTIST, this[Song.ARTIST].highlightedValue)
                .putExtra(MediaStore.EXTRA_MEDIA_TITLE, this[Song.TITLE].highlightedValue)
                .putExtra(SearchManager.QUERY, this[Song.TITLE].highlightedValue)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
}
