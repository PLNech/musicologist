package com.algolia.musicologist.ui

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import com.algolia.instantsearch.ui.views.AlgoliaHitView
import com.algolia.musicologist.model.Song
import org.json.JSONObject
import org.ocpsoft.prettytime.PrettyTime
import java.util.*


class TimestampHitView(context: Context, attrs: AttributeSet) : AppCompatTextView(context, attrs), AlgoliaHitView {
    override fun onUpdateView(result: JSONObject?) {
        setTimestamp(Song.fromJSON(result)!!)
    }

    fun setTimestamp(result: Song) {
        text = PrettyTime().format(Date(result.release_timestamp.toLong() * 1000))
    }
}