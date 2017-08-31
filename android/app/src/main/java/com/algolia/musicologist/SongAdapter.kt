package com.algolia.musicologist

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.algolia.musicologist.model.HighlightedResult
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import kotlinx.android.synthetic.main.cell_song.view.*

internal class SongAdapter(context: Context, resource: Int) : ArrayAdapter<HighlightedResult<Song>>(context, resource) {

    private val highlightRenderer: HighlightRenderer
    private val imageLoader: ImageLoader = ImageLoader.getInstance()
    private val displayImageOptions: DisplayImageOptions = DisplayImageOptions.Builder()
            .cacheOnDisk(true)
            .resetViewBeforeLoading(true)
            .displayer(FadeInBitmapDisplayer(300))
            .build()

    init {

        // Configure Universal Image Loader.
        Thread(Runnable {
            if (!imageLoader.isInited) {
                val configuration = ImageLoaderConfiguration.Builder(context)
                        .memoryCacheSize(2 * 1024 * 1024)
                        .memoryCacheSizePercentage(13) // default
                        .build()
                imageLoader.init(configuration)
            }
        }).start()

        highlightRenderer = HighlightRenderer(context)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val cell : View = convertView ?: LayoutInflater.from(context).inflate(R.layout.cell_song, parent, false)
        val result = getItem(position)

        imageLoader.displayImage(result!!.result.trackViewUrl, cell.preview, displayImageOptions)
        cell.title.text = highlightRenderer.renderHighlights(result[Song.TITLE]?.highlightedValue)
        cell.release.text = String.format("%d", result.result.release_timestamp)

        return cell
    }

    override fun addAll(items: Collection<HighlightedResult<Song>>?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            super.addAll(items)
        } else {
            items?.isNotEmpty()?.let {
                for (item in items) {
                    add(item)
                }
                notifyDataSetChanged()
            }
        }
    }
}
