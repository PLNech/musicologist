package com.algolia.musicologist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class RemoteControlReceiver extends BroadcastReceiver {

    public static final int CODE_INTENT_LISTEN = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

            if (event == null) {
                return;
            }

            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                context.startActivity(new Intent(context, MainActivity.class)
                        .putExtra("code", CODE_INTENT_LISTEN)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }
    }

}