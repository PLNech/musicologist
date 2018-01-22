package com.algolia.musicologist.ui

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
import com.algolia.musicologist.R
import com.algolia.musicologist.model.Highlight
import com.algolia.musicologist.model.HighlightedSong
import com.algolia.musicologist.model.Song
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer
import kotlinx.android.synthetic.main.cell_song.view.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast
import java.util.regex.Pattern

internal class SongAdapter(context: Context, resource: Int) : ArrayAdapter<HighlightedSong>(context, resource) {

    private val imageLoader: ImageLoader = ImageLoader.getInstance()

    init {
        // Configure Universal Image Loader.
        Thread(Runnable {
            if (!imageLoader.isInited) {
                val placeholder = R.drawable.placeholder_record
                val configuration = ImageLoaderConfiguration.Builder(context)
                        .defaultDisplayImageOptions(DisplayImageOptions.Builder()
                                .cacheOnDisk(true)
                                .resetViewBeforeLoading(true)
                                .displayer(FadeInBitmapDisplayer(300))
                                .showImageOnLoading(placeholder)
                                .showImageForEmptyUri(placeholder)
                                .showImageOnFail(placeholder)
                                .build())
                        .memoryCacheSize(2 * 1024 * 1024)
                        .memoryCacheSizePercentage(13) // default
                        .build()
                imageLoader.init(configuration)
            }
        }).start()

    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val cell: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.cell_song, parent, false)
        val result = getItem(position)
        imageLoader.displayImage(result.song.artworkUrl100, cell.preview)
        cell.title.text = renderHighlights(result[Song.TITLE])
        cell.artist.text = renderHighlights(result[Song.ARTIST])
        cell.genre.text = renderHighlights(result[Song.GENRE])
        cell.album.text = renderHighlights(result[Song.ALBUM])
        cell.track.setTrackCount(result.song)
        cell.release.setTimestamp(result.song)
        cell.onClick {
            this@SongAdapter.context.toast("Playing song %d: %s.".format(position, cell.title.text))
            result.song.play(this@SongAdapter.context)
        }
        return cell
    }

    override fun addAll(items: Collection<HighlightedSong>?) {
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

    private fun renderHighlights(highlight: Highlight?): Spannable {
        val result = SpannableStringBuilder()
        val markupString = highlight?.highlightedValue
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
                val highlightColor = R.color.colorHighlight
                val color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.resources.getColor(highlightColor, context.theme)
                } else {
                    @Suppress("DEPRECATION")
                    context.resources.getColor(highlightColor)
                }
                result.setSpan(BackgroundColorSpan(color), q, q + highlightString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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
