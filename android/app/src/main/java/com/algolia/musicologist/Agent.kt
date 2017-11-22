package com.algolia.musicologist

import ai.api.ui.AIButton
import android.content.Context
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.support.design.widget.Snackbar
import com.vdurmont.emoji.EmojiParser

class Agent(context: Context, private var handler: Handler, private var button: AIButton) {
    private val textToSpeech = TextToSpeech(context, null)

    fun say(speech: String, duration: Int = Snackbar.LENGTH_INDEFINITE) {
        say(speech, speech, duration, 0)
    }

    fun say(speech: String, text: String? = null, duration: Int = Snackbar.LENGTH_INDEFINITE, delay: Long = 0) {
        handler.postDelayed({
            textToSpeech.speak(EmojiParser.removeAllEmojis(speech), TextToSpeech.QUEUE_FLUSH, null, null)
            Snackbar.make(button, text ?: speech, duration).show()
        }, delay)
    }

    fun stopTalking() {
        textToSpeech.stop()
    }

    fun shutDown() {
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}