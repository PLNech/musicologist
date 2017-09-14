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
import org.json.JSONObject

/**
 * A song object from the data model.
 */
data class Song(val trackName: String,
                val artistName: String,
                val collectionName: String,
                val trackPrice: Double,
                val trackNumber: Int,
                val primaryGenreName: String,
                val trackCount: Int,
                val trackTimeMillis: Int,
                val artworkUrl100: String,
                val trackViewUrl: String,
                val release_timestamp: Int,
                val json: JSONObject) {

    companion object {
        val TITLE = "trackName"
        val ARTIST = "artistName"
        val ALBUM = "collectionName"
        val RELEASE = "release_timestamp"
        val GENRE = "primaryGenreName"
        val HIGHLIGHT_ATTRIBUTES = listOf(TITLE, ARTIST, ALBUM, GENRE)
    }

    fun play(context: Context) {
        val intent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)
        intent.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, MediaStore.Audio.Media.ENTRY_CONTENT_TYPE)
                .putExtra(MediaStore.EXTRA_MEDIA_ARTIST, artistName)
                .putExtra(MediaStore.EXTRA_MEDIA_TITLE, trackName)
                .putExtra(SearchManager.QUERY, "$trackName $artistName")
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
}