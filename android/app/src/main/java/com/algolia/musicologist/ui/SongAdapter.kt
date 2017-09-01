package com.algolia.musicologist

import android.content.Context
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
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
import java.util.regex.Pattern

internal class SongAdapter(context: Context, resource: Int) : ArrayAdapter<HighlightedResult<Song>>(context, resource) {

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

    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val cell : View = convertView ?: LayoutInflater.from(context).inflate(R.layout.cell_song, parent, false)
        val result = getItem(position)

        imageLoader.displayImage(result!!.result.trackViewUrl, cell.preview, displayImageOptions)
        cell.title.text = renderHighlights(result[Song.TITLE]?.highlightedValue)
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

    private fun renderHighlights(markupString: String?): Spannable {
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
        internal val HIGHLIGHT_PATTERN = Pattern.compile("<em>([^<]*)</em>")
    }

}
