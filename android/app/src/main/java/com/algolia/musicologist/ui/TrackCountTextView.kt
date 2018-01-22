package com.algolia.musicologist.ui

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import com.algolia.instantsearch.ui.views.AlgoliaHitView
import com.algolia.musicologist.model.Song
import org.json.JSONObject


class TrackCountTextView(context: Context, attrs: AttributeSet) : AppCompatTextView(context, attrs), AlgoliaHitView {
    override fun onUpdateView(result: JSONObject?) {
        setTrackCount(Song.fromJSON(result)!!)
    }

    fun setTrackCount(result: Song) {
        text = "%d/%d".format(result.trackNumber, result.trackCount)
    }
}