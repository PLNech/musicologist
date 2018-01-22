package com.algolia.musicologist.ui

import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.algolia.musicologist.VoiceApplication

abstract class VoiceActivity : AppCompatActivity() {
    companion object {
        const val PAUSE_CALLBACK_DELAY: Long = 500
    }

    private val handler = Handler()
    private val app: VoiceApplication by lazy { application as VoiceApplication }

    override fun onResume() {
        super.onResume()
        app.onActivityResume()
    }

    override fun onPause() {
        super.onPause()
        handler.postDelayed({ app.onActivityPaused() }, PAUSE_CALLBACK_DELAY)
    }
}