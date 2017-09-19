package com.algolia.musicologist.ui

import ai.api.android.AIConfiguration
import ai.api.model.AIError
import ai.api.model.AIResponse
import ai.api.model.ResponseMessage
import ai.api.ui.AIButton
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import android.view.KeyEvent
import com.algolia.instantsearch.helpers.InstantSearch
import com.algolia.instantsearch.helpers.Searcher
import com.algolia.musicologist.Agent
import com.algolia.musicologist.R
import com.algolia.musicologist.ResultsListView
import com.algolia.search.saas.Client
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import org.json.JSONObject


class MainActivity : VoiceActivity(), AnkoLogger {

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var agent: Agent
    private lateinit var instantSearch: InstantSearch
    private lateinit var searcher: Searcher
    private lateinit var hits: ResultsListView
    private lateinit var mediaController: MediaControllerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(find(R.id.toolbar))

        wakeupBackend()
        requestAudioPermission()
        configureApiAI()

        searcher = Searcher.create(Client("TDNMRH8LS3", "ec222292c9b89b658fe00b34ff341194").getIndex("songs"))
        hits = find(R.id.hits)
        instantSearch = InstantSearch(hits, searcher)
        agent = Agent(this, handler, find(R.id.micButton))

        setupMediaButtons()
    }

    override fun onDestroy() {
        super.onDestroy()
        agent.shutDown()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return super.onKeyDown(keyCode, event)
        }
        return when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                mediaController.dispatchMediaButtonEvent(event)
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    private fun setupMediaButtons() {
        // Media setup
        val mediaSession = MediaSessionCompat(this, application.packageName)
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
        mediaSession.setPlaybackState(PlaybackStateCompat.Builder()
                .build()
        )
        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                toast("Media button event")
                val action: KeyEvent? = mediaButtonEvent?.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                if (action?.action?.equals(KeyEvent.ACTION_DOWN) != false) {
                    when (action?.keyCode) {
                        KeyEvent.KEYCODE_MEDIA_PLAY,
                        KeyEvent.KEYCODE_MEDIA_PAUSE,
                        KeyEvent.KEYCODE_MEDIA_NEXT,
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                            toast("listening!")
                            agent.stopTalking()
                            micButton.onListeningCanceled()
                            micButton.performClick()
                            return true
                        }
                    }
                }
                return super.onMediaButtonEvent(mediaButtonEvent)
            }
        })
        mediaController = MediaControllerCompat(this, mediaSession)
        MediaControllerCompat.setMediaController(this, mediaController)
    }


    private fun configureApiAI() {
        micButton.initialize(AIConfiguration("b2a8a05cfbdb4162bbdfb9c2f4e48a4e",
                ai.api.AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System))
        micButton.setPartialResultsListener { partialResults ->
            // If you're still talking, stop it TODO: Do it as soon as the button is pressed
            agent.stopTalking()

            val result = partialResults[0]
            if (!TextUtils.isEmpty(result)) {
                handler.post { partialResultsTextView.text = result }
            }
        }
        micButton.setResultsListener(object : AIButton.AIButtonListener {
            override fun onResult(response: AIResponse) {
                runOnUiThread{
                    handleResponse(response)
                }
            }

            override fun onError(error: AIError) {
                runOnUiThread {
                    error("Error: $error.")
                    agent.say("$error.", Snackbar.LENGTH_LONG)
                }
            }

            override fun onCancelled() {
                runOnUiThread {
                    error("Cancelled.")
                    agent.say("Cancelled.", Snackbar.LENGTH_SHORT)
                }
            }
        })
    }

    private fun handleResponse(response: AIResponse) {
        // Update with resolvedQuery until we have sorted partialResults
        // TODO: Remove once merged https://github.com/api-ai/apiai-android-client/pull/62
        partialResultsTextView.text = response.result.resolvedQuery

        // Say speech response
        val message: String = response.result.fulfillment.messages
                ?.filter { it -> (it as ResponseMessage.ResponseSpeech).speech?.size != 0 }
                ?.joinToString(" ") { it -> (it as ResponseMessage.ResponseSpeech).speech.joinToString("\n") }
                ?: response.result.fulfillment.speech
        agent.say(message, delay = 500)

        when (response.result.metadata.intentName) {
            "Results" -> {
                // Response used the webhook, let's present the results
                response.result.getComplexParameter("data")?.let { it ->
                    searcher.forwardBackendSearchResult(JSONObject(it.toString()))
                }
            }
            "Results - select.number" -> {
                val position = response.result.getIntParameter("position")
                if (position != 0) {
                    hits.playSong(position)
                } else {
                    val rank = response.result.getStringParameter("rank")
                    rank?.let {
                        when (rank) {
                            "first" -> hits.playSong(0)
                            "last" -> hits.playLastSong()
                            else -> hits.playRandomSong()
                        }
                    }
                }
            }
            "Default Fallback Intent", "Default Welcome Intent" -> {
            }
            else -> if (!response.result.action.startsWith("smalltalk")) {
                agent.say("I'm sorry, I didn't understand my backend... Please report this bug \uD83D\uDE25")
            }
        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CODE_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                agent.say("Thanks! Looking forward to hearing your lovely voice.", duration = Snackbar.LENGTH_SHORT)
            } else {
                requestAudioPermission()
            }
        }
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PERMISSION_GRANTED) {
            var delay = 0

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

                agent.say("I can only talk with you if you let me record audio.")
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                delay = 1000
            }
            // Explanation given or no explanation needed, we can request the permission.
            handler.postDelayed({
                ActivityCompat.requestPermissions(this@MainActivity,
                        arrayOf(Manifest.permission.RECORD_AUDIO),
                        CODE_PERMISSION_REQUEST)
            }, delay.toLong())
        }
    }

    private fun wakeupBackend() {
        Volley.newRequestQueue(this).add(StringRequest(Request.Method.GET,
                "http://musicologist-backend.herokuapp.com/wakeup", Response.Listener {}, Response.ErrorListener { error ->
            error("Backend seems down: $error.")
            val speech = "Oh oh... It seems my backend is down... I don't know music anymore..."
            agent.say(speech, "$speech :'(", 500)
        }))
    }

    companion object {
        private val CODE_PERMISSION_REQUEST = 0
    }
}