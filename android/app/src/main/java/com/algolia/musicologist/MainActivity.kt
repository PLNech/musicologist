package com.algolia.musicologist

import ai.api.android.AIConfiguration
import ai.api.model.AIError
import ai.api.model.AIResponse
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
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var textToSpeech: TextToSpeech

    private lateinit var aiButton: AIButton
    private lateinit var partialResultsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar) as Toolbar)

        requestAudioPermission()
        configureApiAI()

        textToSpeech = TextToSpeech(this, null)

        aiButton = findViewById(R.id.micButton) as AIButton
        partialResultsTextView = findViewById(R.id.partialResultsTextView) as TextView
    }

    private fun configureApiAI() {
        aiButton.initialize(AIConfiguration("***REMOVED***",
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
                    val speech = response.result.fulfillment.speech
                    textToSpeech.speak(speech, TextToSpeech.QUEUE_FLUSH, null, null)
                    Snackbar.make(aiButton, speech, Snackbar.LENGTH_LONG).show()
                }
            }

            override fun onError(error: AIError) {
                runOnUiThread {
                    Log.d("ApiAi", "onError")
                    Snackbar.make(aiButton, "Error: " + error, Snackbar.LENGTH_LONG).show()
                }
            }

            override fun onCancelled() {
                runOnUiThread {
                    Snackbar.make(aiButton, "Cancelled.", Snackbar.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CODE_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                Snackbar.make(aiButton, "Thanks! Looking forward to hearing your lovely voice.", Snackbar.LENGTH_SHORT).show()
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

                Toast.makeText(this, "This app only works if you allow it to record audio.", Toast.LENGTH_SHORT).show()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        val id = item.itemId
//        when (id) {
//            R.id.action_settings -> return true
//        }
//        return super.onOptionsItemSelected(item)
//    }

    companion object {
        private val CODE_PERMISSION_REQUEST = 0
    }
}