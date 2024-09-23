package com.example.you_tube_wifi_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int ledId = intent.getIntExtra("LED_ID", -1);
        String action = intent.getStringExtra("ACTION");

        if (ledId != -1) {


            Toast.makeText(context, "LED " + ledId + " " + action, Toast.LENGTH_SHORT).show();
            Log.e("neden", "reciver");

            Intent mainIntent = new Intent(context, MainActivity.class);
            mainIntent.putExtra("LED_ID", ledId);
            mainIntent.putExtra("ACTION", action);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainIntent);
        }
    }
}
