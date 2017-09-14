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

import org.json.JSONArray
import org.json.JSONObject
import java.util.*

/**
 * Parses the JSON output of a search query.
 */
class SearchResultsParser {
    operator fun JSONArray.iterator():
            Iterator<JSONObject> = (0 until length()).asSequence()
            .map { get(it) as JSONObject }.iterator()

    /**
     * Parse the root result JSON object into a list of results.

     * @param jsonObject The result's root object.
     * *
     * @return A list of results (potentially empty), or null in case of error.
     */
    fun parseResults(jsonObject: JSONObject?): List<HighlightedSong>? {
        jsonObject ?: return null

        val results = ArrayList<HighlightedSong>()
        val hits = jsonObject.optJSONArray("hits")

        for (hit in hits) {
            val parsed = parse(hit)
            parsed?.let {
                val highlightedResult = HighlightedSong(parsed)
                for (attribute in Song.HIGHLIGHT_ATTRIBUTES) {
                    val value = hit.optJSONObject("_highlightResult")?.
                            optJSONObject(attribute)?.optString("value")
                    value?.let {
                        highlightedResult.addHighlight(attribute, value)
                    }
                }
                results.add(highlightedResult)
            }
        }
        return results
    }
}

fun parse(jsonObject: JSONObject?): Song? {
    var movie: Song? = null
    jsonObject?.let {
        val trackName = jsonObject.optString(Song.TITLE)
        val artistName = jsonObject.optString(Song.ARTIST)
        val collectionName = jsonObject.optString(Song.ALBUM)
        val trackPrice = jsonObject.optDouble("trackPrice")
        val trackNumber = jsonObject.optInt("trackNumber")
        val primaryGenreName = jsonObject.optString(Song.GENRE)
        val trackCount = jsonObject.optInt("trackCount")
        val trackTimeMillis = jsonObject.optInt("trackTimeMillis")
        val artworkUrl100 = jsonObject.optString("artworkUrl100")
        val trackViewUrl = jsonObject.optString("trackViewUrl")
        val release_timestamp = jsonObject.optInt(Song.RELEASE)
        movie = Song(trackName, artistName, collectionName, trackPrice,
                trackNumber, primaryGenreName, trackCount, trackTimeMillis,
                artworkUrl100, trackViewUrl, release_timestamp, jsonObject)
    }
    return movie
}