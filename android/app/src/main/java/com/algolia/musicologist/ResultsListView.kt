package com.algolia.musicologist

import android.content.Context
import android.util.AttributeSet
import android.widget.ListView
import com.algolia.instantsearch.model.AlgoliaErrorListener
import com.algolia.instantsearch.model.AlgoliaResultsListener
import com.algolia.instantsearch.model.SearchResults
import com.algolia.musicologist.ui.SongAdapter
import com.algolia.search.saas.AlgoliaException
import com.algolia.search.saas.Query

class ResultsListView(context: Context, attrs: AttributeSet) : ListView(context, attrs),
        AlgoliaResultsListener, AlgoliaErrorListener {
    private val adapter: SongAdapter = SongAdapter(context, R.layout.cell_song)
    private val resultsParser = SearchResultsParser()

    init {
        setAdapter(adapter)
    }

    override fun onResults(results: SearchResults, isLoadingMore: Boolean) {
        if (!isLoadingMore) {
            val resultList = resultsParser.parseResults(results.content)
            adapter.clear()
            adapter.addAll(resultList)
            // Scroll the list back to the top.
            smoothScrollToPosition(0)
        } else {
            val resultList = resultsParser.parseResults(results.content)
            adapter.addAll(resultList)
        }
    }

    override fun onError(query: Query, error: AlgoliaException) {}
}
