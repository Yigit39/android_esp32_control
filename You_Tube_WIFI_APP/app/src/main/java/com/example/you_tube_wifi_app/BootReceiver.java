package com.example.you_tube_wifi_app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Cihaz yeniden başlatıldı, alarmları yeniden kur
            SharedPreferences sharedPref = context.getSharedPreferences("LEDNames", Context.MODE_PRIVATE);
            int visibleCount = sharedPref.getInt("VisibleCount", 3);

            for (int i = 0; i < visibleCount; i++) {
                // LED açma (ON) alarmlarını kur
                boolean isChecked = sharedPref.getBoolean("CheckBoxChecked" + (i + 1), false);
                if (isChecked) {
                    String time = sharedPref.getString("CheckBoxTime" + (i + 1), "");
                    if (!time.isEmpty()) {
                        setAlarm(context, i + 1, time, "ON");
                    }
                }

                // LED kapama (OFF) alarmlarını kur
                boolean isKapaChecked = sharedPref.getBoolean("CheckKapaChecked" + (i + 1), false);
                if (isKapaChecked) {
                    String time = sharedPref.getString("CheckKapaTime" + (i + 1), "");
                    if (!time.isEmpty()) {
                        setAlarm(context, i + 1, time, "OFF");
                    }
                }
            }

            Toast.makeText(context, "Alarmlar yeniden kuruldu", Toast.LENGTH_SHORT).show();
        }
    }

    private void setAlarm(Context context, int id, String time, String action) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        String[] timeParts = time.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            // Eğer zaman geçmişse, alarmı bir sonraki gün kur
            calendar.add(Calendar.DATE, 1);
        }

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra("LED_ID", id);
        alarmIntent.putExtra("ACTION", action);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                id * (action.equals("ON") ? 1 : 100),
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Check for permission to schedule exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Handle the case where exact alarms cannot be scheduled
                Toast.makeText(context, "Uygulamanın kesin alarm kurma izni yok.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        try {
            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        } catch (SecurityException e) {
            // Handle the exception if the permission is not granted
            Toast.makeText(context, "Alarm kurma izni verilmedi.", Toast.LENGTH_LONG).show();
        }
    }
}
