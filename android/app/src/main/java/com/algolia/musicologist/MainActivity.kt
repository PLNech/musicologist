package com.algolia.musicologist

import ai.api.android.AIConfiguration
import ai.api.model.AIError
import ai.api.model.AIResponse
import ai.api.model.ResponseMessage
import ai.api.ui.AIButton
import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class MainActivity : VoiceActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var textToSpeech: TextToSpeech

    private lateinit var aiButton: AIButton
    private lateinit var partialResultsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar)

        textToSpeech = TextToSpeech(this, null)

        aiButton = findViewById(R.id.micButton) as AIButton
        partialResultsTextView = findViewById(R.id.partialResultsTextView) as TextView

        wakeupBackend()
        requestAudioPermission()
        configureApiAI()
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        val id = item.itemId
//        when (id) {
//            R.id.action_settings -> return true
//        }
//        return super.onOptionsItemSelected(item)
//    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun configureApiAI() {
        aiButton.initialize(AIConfiguration("b2a8a05cfbdb4162bbdfb9c2f4e48a4e",
                ai.api.AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System))
        aiButton.setPartialResultsListener { partialResults ->
            val result = partialResults[0]
            if (!TextUtils.isEmpty(result)) {
                handler.post { partialResultsTextView.text = result }
            }
        }
        aiButton.setResultsListener(object : AIButton.AIButtonListener {
            override fun onResult(response: AIResponse) {
                runOnUiThread {
                    // Update with resolvedQuery until we have sorted partialResults
                    // TODO: Remove once merged https://github.com/api-ai/apiai-android-client/pull/62
                    partialResultsTextView.text = response.result.resolvedQuery

                    say(response.result.fulfillment.speech, delay = 500)
                }
            }

            override fun onError(error: AIError) {
                runOnUiThread {
                    Log.e("ApiAi", "Error: " + error)
                    say("Error: " + error, Snackbar.LENGTH_LONG)
                }
            }

            override fun onCancelled() {
                runOnUiThread {
                    Log.d("MainActivity", "Cancelled.")
                    say("Cancelled.", Snackbar.LENGTH_SHORT)
                }
            }
        })
    }

    private fun wakeupBackend() {
        Volley.newRequestQueue(this).add(StringRequest(Request.Method.GET,
                "http://musicologist-backend.herokuapp.com/wakeup", Response.Listener {}, Response.ErrorListener { error ->
            Log.e("MainActivity", "Backend seems down: " + error)
            say("Oh oh... It seems my backend is down... I don't know music anymore...",
                    text = "Oh oh... It seems my backend is down... I don't know music anymore... :'(", delay = 500)
        }));
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CODE_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                say("Thanks! Looking forward to hearing your lovely voice.", duration = Snackbar.LENGTH_SHORT)
            } else {
                requestAudioPermission()
            }
        }
    }

    private fun say(speech: String, duration: Int = Snackbar.LENGTH_INDEFINITE) {
        say(speech, speech, duration, 0)
    }

    private fun say(speech: String, text: String? = null, duration: Int = Snackbar.LENGTH_INDEFINITE, delay: Long = 0) {
        handler.postDelayed({
            textToSpeech.speak(text ?: speech, TextToSpeech.QUEUE_FLUSH, null, null)
            Snackbar.make(aiButton, speech, duration).show()
        }, delay)
    }

    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PERMISSION_GRANTED) {
            var delay = 0

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

                say("I can only talk with you if you let me record audio.")
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

    companion object {
        private val CODE_PERMISSION_REQUEST = 0
    }
}