package com.example.you_tube_wifi_app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {
    private EditText wifi_server,wifi_ad,wifi_sifre;
    private EditText[] editTexts;
    private CheckBox[] checkBoxes;
    private CheckBox[] checkKapaBoxes;
    private Button buttonSave;
    private ImageButton addBtn, removeBtn;
    private int currentVisibleCount = 3; // Başlangıçta 3 eleman görünür
    private SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);



        // EditText dizisi tanımlanıyor
        editTexts = new EditText[] {
                findViewById(R.id.editTextLed1),
                findViewById(R.id.editTextLed2),
                findViewById(R.id.editTextLed3),
                findViewById(R.id.editTextLed4),
                findViewById(R.id.editTextLed5),
                findViewById(R.id.editTextLed6),
                findViewById(R.id.editTextLed7),
                findViewById(R.id.editTextLed8),
                findViewById(R.id.editTextLed9),
                findViewById(R.id.editTextLed10),
                findViewById(R.id.editTextLed11),
                findViewById(R.id.editTextLed12),
                findViewById(R.id.editTextLed13),
                findViewById(R.id.editTextLed14),
                findViewById(R.id.editTextLed15),
                findViewById(R.id.editTextLed16)
        };

        // CheckBox dizisi tanımlanıyor
        checkBoxes = new CheckBox[] {
                findViewById(R.id.checkBox1),
                findViewById(R.id.checkBox2),
                findViewById(R.id.checkBox3),
                findViewById(R.id.checkBox4),
                findViewById(R.id.checkBox5),
                findViewById(R.id.checkBox6),
                findViewById(R.id.checkBox7),
                findViewById(R.id.checkBox8),
                findViewById(R.id.checkBox9),
                findViewById(R.id.checkBox10),
                findViewById(R.id.checkBox11),
                findViewById(R.id.checkBox12),
                findViewById(R.id.checkBox13),
                findViewById(R.id.checkBox14),
                findViewById(R.id.checkBox15),
                findViewById(R.id.checkBox16)
        };

        // Yeni CheckBox dizisi tanımlanıyor
        checkKapaBoxes = new CheckBox[] {
                findViewById(R.id.check_Kapa_1),
                findViewById(R.id.check_Kapa_2),
                findViewById(R.id.check_Kapa_3),
                findViewById(R.id.check_Kapa_4),
                findViewById(R.id.check_Kapa_5),
                findViewById(R.id.check_Kapa_6),
                findViewById(R.id.check_Kapa_7),
                findViewById(R.id.check_Kapa_8),
                findViewById(R.id.check_Kapa_9),
                findViewById(R.id.check_Kapa_10),
                findViewById(R.id.check_Kapa_11),
                findViewById(R.id.check_Kapa_12),
                findViewById(R.id.check_Kapa_13),
                findViewById(R.id.check_Kapa_14),
                findViewById(R.id.check_Kapa_15),
                findViewById(R.id.check_Kapa_16)
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                // Request the permission by opening the settings screen
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        buttonSave = findViewById(R.id.buttonSave);
        addBtn = findViewById(R.id.add_btn);
        removeBtn = findViewById(R.id.remove_btn);

        // SharedPreferences'dan değerleri yükle
        SharedPreferences sharedPref = getSharedPreferences("LEDNames", MODE_PRIVATE);
        currentVisibleCount = sharedPref.getInt("VisibleCount", 3); // Bulunmazsa varsayılan olarak 3

        // wifi_server EditText'i tanımla
        wifi_server = findViewById(R.id.wifi_server);

        // SharedPreferences'dan wifi_server değerini yükle
        String savedWifiServer = sharedPref.getString("wifi_server", "http://192.168.43.44/"); // Varsayılan olarak boş string
        wifi_server.setText(savedWifiServer);

        wifi_ad = findViewById(R.id.wifi_ad);
        wifi_sifre = findViewById(R.id.wifi_sifre);
        String savedWifiAd = sharedPref.getString("wifi_ad", "cokgizlinet"); // Varsayılan olarak cokgizlinet
        String savedWifiSifre = sharedPref.getString("wifi_sifre", "gizlisifrebu"); // Varsayılan olarak gizlisifrebu
        wifi_ad.setText(savedWifiAd);
        wifi_sifre.setText(savedWifiSifre);


        // Görünürlükleri ayarla ve zaman değerlerini yükle
        for (int i = 0; i < 16; i++) {
            if (i < currentVisibleCount) {
                editTexts[i].setVisibility(View.VISIBLE);
                checkBoxes[i].setVisibility(View.VISIBLE);
                checkKapaBoxes[i].setVisibility(View.VISIBLE);

                // EditText içeriğini yükle
                editTexts[i].setText(sharedPref.getString("LED" + (i + 1), ""));

                // CheckBox durumu ve zamanı yükle
                boolean isChecked = sharedPref.getBoolean("CheckBoxChecked" + (i + 1), false);
                checkBoxes[i].setChecked(isChecked);
                if (isChecked) {
                    checkBoxes[i].setText(sharedPref.getString("CheckBoxTime" + (i + 1), ""));
                } else {
                    checkBoxes[i].setText(""); // Seçim kaldırıldığında CheckBox metnini temizle
                }

                // Yeni CheckBox'lar için aynı işlemleri yap
                boolean isKapaChecked = sharedPref.getBoolean("CheckKapaChecked" + (i + 1), false);
                checkKapaBoxes[i].setChecked(isKapaChecked);
                if (isKapaChecked) {
                    checkKapaBoxes[i].setText(sharedPref.getString("CheckKapaTime" + (i + 1), ""));
                } else {
                    checkKapaBoxes[i].setText(""); // Seçim kaldırıldığında CheckBox metnini temizle
                }
            } else {
                editTexts[i].setVisibility(View.GONE);
                checkBoxes[i].setVisibility(View.GONE);
                checkKapaBoxes[i].setVisibility(View.GONE);
            }
        }

        buttonSave.setOnClickListener(view -> saveSettings());

        addBtn.setOnClickListener(view -> {
            if (currentVisibleCount < 16) {
                editTexts[currentVisibleCount].setVisibility(View.VISIBLE);
                checkBoxes[currentVisibleCount].setVisibility(View.VISIBLE);
                checkKapaBoxes[currentVisibleCount].setVisibility(View.VISIBLE);
                currentVisibleCount++;
            }
        });

        removeBtn.setOnClickListener(view -> {
            if (currentVisibleCount > 1) {
                currentVisibleCount--;
                editTexts[currentVisibleCount].setVisibility(View.GONE);
                checkBoxes[currentVisibleCount].setVisibility(View.GONE);
                checkKapaBoxes[currentVisibleCount].setVisibility(View.GONE);
            }
        });

        // Zaman seçici diyalog için ayarlamalar
        for (int i = 0; i < 16; i++) {
            final int index = i;
            checkBoxes[i].setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    showTimePickerDialog(index, checkBoxes);
                } else {
                    checkBoxes[index].setText(""); // Seçim kaldırıldığında CheckBox metnini temizle
                }
            });

            checkKapaBoxes[i].setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    showTimePickerDialog(index, checkKapaBoxes);
                } else {
                    checkKapaBoxes[index].setText(""); // Seçim kaldırıldığında CheckBox metnini temizle
                }
            });
        }
    }

    private void showTimePickerDialog(int index, CheckBox[] checkBoxArray) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String time = String.format("%02d:%02d", hourOfDay, minuteOfHour);
            // Seçilen zamanı sadece ilgili CheckBox'un metnine yaz
            checkBoxArray[index].setText(time);
        }, hour, minute, true);

        // Kullanıcı "Cancel" tuşuna basarsa, CheckBox'u onaysız yap
        timePickerDialog.setOnCancelListener(dialog -> checkBoxArray[index].setChecked(false));

        timePickerDialog.show();
    }

    private void saveSettings() {
        SharedPreferences sharedPref = getSharedPreferences("LEDNames", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        for (int i = 0; i < currentVisibleCount; i++) {
            // EditText verilerini kaydet
            editor.putString("LED" + (i + 1), editTexts[i].getText().toString());

            // CheckBox durumu ve zamanı kaydet
            editor.putBoolean("CheckBoxChecked" + (i + 1), checkBoxes[i].isChecked());
            if (checkBoxes[i].isChecked()) {
                String time = checkBoxes[i].getText().toString();
                editor.putString("CheckBoxTime" + (i + 1), time);
                setAlarm(alarmManager, i + 1, time, "ON");
            } else {
                editor.putString("CheckBoxTime" + (i + 1), "");
                cancelAlarm(alarmManager, i + 1, "ON");
            }

            // Yeni CheckBox'lar için aynı işlemleri yap
            editor.putBoolean("CheckKapaChecked" + (i + 1), checkKapaBoxes[i].isChecked());
            if (checkKapaBoxes[i].isChecked()) {
                String time = checkKapaBoxes[i].getText().toString();
                editor.putString("CheckKapaTime" + (i + 1), time);
                setAlarm(alarmManager, i + 1, time, "OFF");
            } else {
                editor.putString("CheckKapaTime" + (i + 1), "");
                cancelAlarm(alarmManager, i + 1, "OFF");
            }
        }
        // wifi_server değerini kaydet
        String wifiServerValue = wifi_server.getText().toString();
        editor.putString("wifi_server", wifiServerValue);

        // wifi_ad değerini kaydet
        String wifiAdValue = wifi_ad.getText().toString();
        editor.putString("wifi_ad", wifiAdValue);

        // wifi_sifre değerini kaydet
        String wifiSifreValue = wifi_sifre.getText().toString();
        editor.putString("wifi_sifre", wifiSifreValue);

        editor.putInt("VisibleCount", currentVisibleCount);
        editor.apply();
        finish(); // Ayarlar kaydedildikten sonra aktiviteyi kapat
    }

    private void setAlarm(AlarmManager alarmManager, int id, String time, String action) {
        try {
            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.before(Calendar.getInstance())) {
                // If the time is in the past, set it for the next day
                calendar.add(Calendar.DATE, 1);
            }

            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("LED_ID", id);
            intent.putExtra("ACTION", action);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    id * (action.equals("ON") ? 1 : 100), // Different requestCode for each LED
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } catch (SecurityException e) {
            // Handle the security exception if the permission is not granted
            e.printStackTrace();
            // You could show a dialog or take other actions
        }
    }

    private void cancelAlarm(AlarmManager alarmManager, int id, String action) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                id * (action.equals("ON") ? 1 : 100),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }
}