package com.algolia.musicologist;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ai.api.PartialResultsListener;
import ai.api.android.AIConfiguration;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.ui.AIButton;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {
    private static final int CODE_PERMISSION_REQUEST = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        requestAudioPermission();

        configureApiAI();

        if (textToSpeech == null) {
            textToSpeech = new TextToSpeech(this, null);
        }
    }

    private void configureApiAI() {
        final AIConfiguration config = new AIConfiguration("***REMOVED***",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        final AIButton aiButton = (AIButton) findViewById(R.id.micButton);
        final TextView partialResultsTextView = (TextView) findViewById(R.id.partialResultsTextView);

        aiButton.initialize(config);
        aiButton.setPartialResultsListener(new PartialResultsListener() {
            @Override
            public void onPartialResults(final List<String> partialResults) {
                final String result = partialResults.get(0);
                if (!TextUtils.isEmpty(result)) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (partialResultsTextView != null) {
                                partialResultsTextView.setText(result);
                            }
                        }
                    });
                }
            }
        });
        aiButton.setResultsListener(new AIButton.AIButtonListener() {
            @Override
            public void onResult(final AIResponse response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final String speech = response.getResult().getFulfillment().getSpeech();
                        textToSpeech.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
                        Snackbar.make(aiButton, speech, Snackbar.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(final AIError error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("ApiAi", "onError");
                        // TODO process error here
                    }
                });
            }

            @Override public void onCancelled() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("ApiAi", "onCancel");
                        // TODO process error here
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                Toast.makeText(this, "Thanks! Looking forward to hearing your lovely voice.", Toast.LENGTH_LONG).show();
            } else {
                requestAudioPermission();
            }
        }
    }

    private void requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PERMISSION_GRANTED) {
            int delay = 0;

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

                Toast.makeText(this, "This app only works if you allow it to record audio.", Toast.LENGTH_SHORT).show();
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                delay = 1000;
            }
            // Explanation given or no explanation needed, we can request the permission.
            handler.postDelayed(new Runnable() {
                @Override public void run() {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            CODE_PERMISSION_REQUEST);
                }
            }, delay);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }
}
