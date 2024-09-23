package com.example.you_tube_wifi_app;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlarmManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_PERMISSIONS = 100;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPP UUID
    private String BASE_URL;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;
    private OutputStream outputStream;
    private OkHttpClient client = new OkHttpClient();

    private Button led_1, led_2, led_3, led_4, led_5, led_6, led_7, led_8, led_9, led_10, led_11, led_12, led_13, led_14, led_15, led_16;
    Button ledButton;

    private Button connectButton, disconnectButton;
    private TextView logTextView;
    private ImageButton ayarlar_btn, bluetooth_open_btn, bluetooth_close_btn, wifi_open_btn, wifi_close_btn;
    private Switch switch1;

    private boolean[] ledStates = new boolean[17];

    private Handler handler = new Handler(); // For handling delayed operations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load base URL from SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("LEDNames", MODE_PRIVATE);
        String baseUrl = sharedPref.getString("wifi_server", "http://192.168.43.44/"); // Default value
        String wifi_ıd = sharedPref.getString("wifi_ad", "cokgizlinet");
        String wifi_pass = sharedPref.getString("wifi_sifre", "gizlisifrebu");

        // Set BASE_URL
        BASE_URL = baseUrl;

        // Initialize UI components
        led_1 = findViewById(R.id.led_1);
        led_2 = findViewById(R.id.led_2);
        led_3 = findViewById(R.id.led_3);
        led_4 = findViewById(R.id.led_4);
        led_5 = findViewById(R.id.led_5);
        led_6 = findViewById(R.id.led_6);
        led_7 = findViewById(R.id.led_7);
        led_8 = findViewById(R.id.led_8);
        led_9 = findViewById(R.id.led_9);
        led_10 = findViewById(R.id.led_10);
        led_11 = findViewById(R.id.led_11);
        led_12 = findViewById(R.id.led_12);
        led_13 = findViewById(R.id.led_13);
        led_14 = findViewById(R.id.led_14);
        led_15 = findViewById(R.id.led_15);
        led_16 = findViewById(R.id.led_16);
        ayarlar_btn = findViewById(R.id.ayarlar_btn);
        bluetooth_open_btn = findViewById(R.id.bluetooth_open_btn);
        bluetooth_close_btn = findViewById(R.id.bluetooth_close_btn);
        wifi_open_btn = findViewById(R.id.wifi_open_btn);
        wifi_close_btn = findViewById(R.id.wifi_close_btn);
        connectButton = findViewById(R.id.connectButton);
        disconnectButton = findViewById(R.id.disconnectButton);
        logTextView = findViewById(R.id.logTextView);
        switch1 = findViewById(R.id.switch1);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        // Request necessary permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermissions()) {
                requestPermissions();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }


        // Setup network connection
        setupNetworkConnection();

        // Update LED names and visibility
        updateLedNames();

        updateBaseUrl();

        updateWifi();

        // Switch functionality
        switch1.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                // Switch to Bluetooth
                handleBluetoothMode();
            } else {
                // Switch to Wi-Fi
                handleWifiMode();
            }
        });

        // LED buttons click listeners
        led_1.setOnClickListener(view -> toggleLed("led_1", "11", "10"));
        led_2.setOnClickListener(view -> toggleLed("led_2", "21", "20"));
        led_3.setOnClickListener(view -> toggleLed("led_3", "31", "30"));
        led_4.setOnClickListener(view -> toggleLed("led_4", "41", "40"));
        led_5.setOnClickListener(view -> toggleLed("led_5", "51", "50"));
        led_6.setOnClickListener(view -> toggleLed("led_6", "61", "60"));
        led_7.setOnClickListener(view -> toggleLed("led_7", "71", "70"));
        led_8.setOnClickListener(view -> toggleLed("led_8", "81", "80"));
        led_9.setOnClickListener(view -> toggleLed("led_9", "91", "90"));
        led_10.setOnClickListener(view -> toggleLed("led_10", "101", "100"));
        led_11.setOnClickListener(view -> toggleLed("led_11", "111", "110"));
        led_12.setOnClickListener(view -> toggleLed("led_12", "121", "120"));
        led_13.setOnClickListener(view -> toggleLed("led_13", "131", "130"));
        led_14.setOnClickListener(view -> toggleLed("led_14", "141", "140"));
        led_15.setOnClickListener(view -> toggleLed("led_15", "151", "150"));
        led_16.setOnClickListener(view -> toggleLed("led_16", "161", "160"));


        // Settings button click listener
        ayarlar_btn.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Connect button click listener
        connectButton.setOnClickListener(v -> {
            connectButton.setEnabled(false); // Butonu devre dışı bırak
            if (checkPermissions()) {
                try {
                    connectToDevice("ESP32_BT"); // ESP32 Bluetooth cihaz adını değiştir
                } catch (SecurityException se) {
                    Log.e(TAG, "Security exception: Missing permissions.", se);
                    appendLog("Security exception: Missing permissions.");
                    Toast.makeText(MainActivity.this, "Security exception: Missing permissions.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Permissions not granted", Toast.LENGTH_SHORT).show();
                appendLog("Permissions not granted");
                requestPermissions();
            }

            // 3 saniye sonra butonu tekrar etkinleştir
            new Handler().postDelayed(() -> connectButton.setEnabled(true), 3000);
        });


        // Disconnect button click listener
        disconnectButton.setOnClickListener(v -> disconnectDevice());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLedNames();
        updateBaseUrl();
        updateWifi();
    }

    private void updateBaseUrl() {
        // Load base URL from SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("LEDNames", MODE_PRIVATE);
        String baseUrl = sharedPref.getString("wifi_server", "http://192.168.43.44/"); // Default value

        // Set BASE_URL
        BASE_URL = baseUrl;
    }

    private void updateWifi() {
        // Eğer switch açık ise (Bluetooth modunda)
        if (switch1.isChecked()) {
            // Bluetooth bağlantısının aktif olup olmadığını kontrol et
            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                SharedPreferences sharedPref = getSharedPreferences("LEDNames", MODE_PRIVATE);
                String wifi_ıd = sharedPref.getString("wifi_ad", "cokgizlinet");    // SSID
                String wifi_pass = sharedPref.getString("wifi_sifre", "gizlisifrebu"); // PASS


                // SSID'yi gönder
                sendCommand("SSID:" + wifi_ıd);

                // Kısa bir gecikme, isteğe bağlı olarak eklenebilir
                try {
                    Thread.sleep(1000);  // 1000ms gecikme
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // PASS'ı gönder
                sendCommand("PASS:" + wifi_pass);

                try {
                    Thread.sleep(1000);  // 1000ms gecikme
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Toast.makeText(this, "SSID ve PASS gönderildi", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bluetooth bağlantısı yok", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Eğer switch kapalıysa (Bluetooth kapalı)
            Toast.makeText(this, "Bluetooth kapalı", Toast.LENGTH_SHORT).show();
        }
    }



    private void updateLedNames() {
        SharedPreferences sharedPref = getSharedPreferences("LEDNames", MODE_PRIVATE);
        int visibleCount = sharedPref.getInt("VisibleCount", 3); // Default to 3 if not found

        // Update the visibility of LED buttons based on the visible count
        led_1.setVisibility(visibleCount >= 1 ? View.VISIBLE : View.GONE);
        led_2.setVisibility(visibleCount >= 2 ? View.VISIBLE : View.GONE);
        led_3.setVisibility(visibleCount >= 3 ? View.VISIBLE : View.GONE);
        led_4.setVisibility(visibleCount >= 4 ? View.VISIBLE : View.GONE);
        led_5.setVisibility(visibleCount >= 5 ? View.VISIBLE : View.GONE);
        led_6.setVisibility(visibleCount >= 6 ? View.VISIBLE : View.GONE);
        led_7.setVisibility(visibleCount >= 7 ? View.VISIBLE : View.GONE);
        led_8.setVisibility(visibleCount >= 8 ? View.VISIBLE : View.GONE);
        led_9.setVisibility(visibleCount >= 9 ? View.VISIBLE : View.GONE);
        led_10.setVisibility(visibleCount >= 10 ? View.VISIBLE : View.GONE);
        led_11.setVisibility(visibleCount >= 11 ? View.VISIBLE : View.GONE);
        led_12.setVisibility(visibleCount >= 12 ? View.VISIBLE : View.GONE);
        led_13.setVisibility(visibleCount >= 13 ? View.VISIBLE : View.GONE);
        led_14.setVisibility(visibleCount >= 14 ? View.VISIBLE : View.GONE);
        led_15.setVisibility(visibleCount >= 15 ? View.VISIBLE : View.GONE);
        led_16.setVisibility(visibleCount >= 16 ? View.VISIBLE : View.GONE);
        // Update LED names
        for (int i = 1; i <= visibleCount; i++) {
            String ledName = sharedPref.getString("LED" + i, "LED " + i);
            switch (i) {
                case 1: led_1.setText(ledName); break;
                case 2: led_2.setText(ledName); break;
                case 3: led_3.setText(ledName); break;
                case 4: led_4.setText(ledName); break;
                case 5: led_5.setText(ledName); break;
                case 6: led_6.setText(ledName); break;
                case 7: led_7.setText(ledName); break;
                case 8: led_8.setText(ledName); break;
                case 9: led_9.setText(ledName); break;
                case 10: led_10.setText(ledName); break;
                case 11: led_11.setText(ledName); break;
                case 12: led_12.setText(ledName); break;
                case 13: led_13.setText(ledName); break;
                case 14: led_14.setText(ledName); break;
                case 15: led_15.setText(ledName); break;
                case 16: led_16.setText(ledName); break;
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e("neden", "intent içi");
        // Intent'ten LED ID'si ve aksiyon bilgisini al
        int ledId = intent.getIntExtra("LED_ID", -1);
        String action = intent.getStringExtra("ACTION");

        // LED ID'si ve aksiyon geçerliyse işlemi gerçekleştir
        if (ledId != -1 && action != null) {
            Button ledButton;
            boolean ledState = false;

            // LED ID'sine göre uygun butonu seç
            switch (ledId) {
                case 1:
                    ledButton = findViewById(R.id.led_1);
                    ledState = ledStates[1];
                    break;
                case 2:
                    ledButton = findViewById(R.id.led_2);
                    ledState = ledStates[2];
                    break;
                case 3:
                    ledButton = findViewById(R.id.led_3);
                    ledState = ledStates[3];
                    break;
                case 4:
                    ledButton = findViewById(R.id.led_4);
                    ledState = ledStates[4];
                    break;
                case 5:
                    ledButton = findViewById(R.id.led_5);
                    ledState = ledStates[5];
                    break;
                case 6:
                    ledButton = findViewById(R.id.led_6);
                    ledState = ledStates[6];
                    break;
                case 7:
                    ledButton = findViewById(R.id.led_7);
                    ledState = ledStates[7];
                    break;
                case 8:
                    ledButton = findViewById(R.id.led_8);
                    ledState = ledStates[8];
                    break;
                case 9:
                    ledButton = findViewById(R.id.led_9);
                    ledState = ledStates[9];
                    break;
                case 10:
                    ledButton = findViewById(R.id.led_10);
                    ledState = ledStates[10];
                    break;
                case 11:
                    ledButton = findViewById(R.id.led_11);
                    ledState = ledStates[11];
                    break;
                case 12:
                    ledButton = findViewById(R.id.led_12);
                    ledState = ledStates[12];
                    break;
                case 13:
                    ledButton = findViewById(R.id.led_13);
                    ledState = ledStates[13];
                    break;
                case 14:
                    ledButton = findViewById(R.id.led_14);
                    ledState = ledStates[14];
                    break;
                case 15:
                    ledButton = findViewById(R.id.led_15);
                    ledState = ledStates[15];
                    break;
                case 16:
                    ledButton = findViewById(R.id.led_16);
                    ledState = ledStates[16];
                    break;
                default:
                    // Geçersiz LED ID'si durumunda butonu null olarak ayarla
                    Log.e("neden", "id yok");
                    ledButton = null;
                    break;
            }

            // LED butonu geçerliyse aksiyonu gerçekleştir
            if (ledButton != null) {
                if (ledButton.getVisibility() == View.VISIBLE && ledButton.isClickable()) {
                    String onCommand = ledId + "1"; // Örneğin "11" için
                    String offCommand = ledId + "0"; // Örneğin "10" için
                    if (action.equals("ON") && !ledState) {
                        // LED'i aç
                        toggleLed("led_" + ledId, onCommand, offCommand);
                    } else if (action.equals("OFF") && ledState) {
                        // LED'i kapa
                        toggleLed("led_" + ledId, onCommand, offCommand);
                    }
                } else {
                    // Buton görünür değilse veya tıklanabilir değilse hata mesajı yazdır
                    Log.e("neden", "LED button is not visible or clickable");
                }
            }
        }
    }



    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_CODE_PERMISSIONS);
    }

    private void setupNetworkConnection() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager != null) {
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
            builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            NetworkRequest request = builder.build();
            connManager.requestNetwork(request, new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    connManager.bindProcessToNetwork(network);
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    // Handle network loss if necessary
                }
            });
        }
    }

    private void toggleLed(String led, String onCommand, String offCommand) {
        boolean currentState = false;
        int buttonBackground = R.drawable.led_btn_bg_closed; // Default background when LED is off
        Button ledButton = null;

        // LED ID'sine göre uygun butonu ve durumu belirleyin
        switch (led) {
            case "led_1":
                currentState = ledStates[1];
                ledStates[1] = !ledStates[1]; // Durumu tersine çevir
                buttonBackground = ledStates[1] ? R.drawable.led_btn_bg : R.drawable.led_btn_bg_closed;
                ledButton = led_1;
                Log.e("serverdeneme", "wifi_server: " + BASE_URL);
                break;
            case "led_2":
                currentState = ledStates[2];
                ledStates[2] = !ledStates[2]; // Durumu tersine çevir
                buttonBackground = ledStates[2] ? R.drawable.led_btn_bg : R.drawable.led_btn_bg_closed;
                ledButton = led_2;
                break;
            case "led_3":
                currentState = ledStates[3];
                ledStates[3] = !ledStates[3]; // Durumu tersine çevir
                buttonBackground = ledStates[3] ? R.drawable.led_btn_bg : R.drawable.led_btn_bg_closed;
                ledButton = led_3;
                break;
            case "led_4":
                currentState = ledStates[4];
                ledStates[4] = !ledStates[4]; // Durumu tersine çevir
                buttonBackground = ledStates[4] ? R.drawable.led_btn_bg : R.drawable.led_btn_bg_closed;
                ledButton = led_4;
                break;
            case "led_5":
                currentState = ledStates[5];
                ledStates[5] = !ledStates[5]; // Durumu tersine çevir
                buttonBackground = ledStates[5] ? R.drawable.led_btn_bg : R.drawable.led_btn_bg_closed;
                ledButton = led_5;
                break;
            case "led_6":
                currentState = ledStates[6];
                ledStates[6] = !ledStates[6]; // Durumu tersine çevir
                buttonBackground = ledStates[6] ? R.drawable.led_btn_bg : R.drawable.led_btn_bg_closed;
                ledButton = led_6;
                break;
            case "led_7":
                currentState = ledStates[7];
                ledStates[7] = !ledStates[7]; // Durumu tersine çevir
                buttonBackground = ledStates[7] ? R.drawable.led_btn_bg : R.drawable.led_btn_bg_closed;
                ledButton = led_7;
                break;
            case "led_8":
                currentState = ledStates[8];
                ledStates[8] = !ledStates[8]; // Durumu tersine çevir
                buttonBackground = ledStates[8] ? R.drawable.led_btn_bg : R.drawable.led_btn_bg_closed;
                ledButton = led_8;
                break;
            case "led_9":
                currentState = ledStates[9];
                ledStates[9] = !ledStates[9]; // Durumu tersine çevir
                buttonBackground = ledStates[9] ? R.drawable.led_btn_bg : R.drawable.led_btn_bg_closed;
                ledButton = led_9;
                break;
            case "led_10":
                currentState = ledStates[10];
                ledStates[10] = !ledStates[10]; // Durumu tersine çevir
                buttonBackground = ledStates[10] ? R.drawable.led_btn_bg : R.drawable.led_btn_bg_closed;
                ledButton = led_10;
                break;
            case "led_11":
                currentState = ledStates[11];
                ledStates[11] = !ledStates[11]; // Durumu tersine çevir
                buttonBackground = ledStates[11] ? R.drawable.led_btn_bg : R.drawable.led_btn_bg_closed;
                ledButton = led_11;
                break;
            case "led_12":
                currentState = ledStates[12];
                ledStates[12] = !ledStates[12]; // Durumu tersine çevir
                buttonBackground = ledStates[12] ? R.drawable.led_btn_bg : R.drawable.led_btn_bg_closed;
                ledButton = led_12;
                break;
            case "led_13":
                currentState = ledStates[13];
                ledStates[13] = !ledStates[13]; // Durumu tersine çevir
                buttonBackground = ledStates[13] ? R.drawable.led_btn_bg : R.drawable.led_btn_bg_closed;
                ledButton = led_13;
                break;
            case "led_14":
                currentState = ledStates[14];
                ledStates[14] = !ledStates[14]; // Durumu tersine çevir
                buttonBackground = ledStates[14] ? R.drawable.led_btn_bg : R.drawable.led_btn_bg_closed;
                ledButton = led_14;
                break;
            case "led_15":
                currentState = ledStates[15];
                ledStates[15] = !ledStates[15]; // Durumu tersine çevir
                buttonBackground = ledStates[15] ? R.drawable.led_btn_bg : R.drawable.led_btn_bg_closed;
                ledButton = led_15;
                break;
            case "led_16":
                currentState = ledStates[16];
                ledStates[16] = !ledStates[16]; // Durumu tersine çevir
                buttonBackground = ledStates[16] ? R.drawable.led_btn_bg : R.drawable.led_btn_bg_closed;
                ledButton = led_16;
                break;
        }

        if (ledButton != null) {
            // LED butonunun arka planını güncelleyin
            ledButton.setBackgroundResource(buttonBackground);

            // Komutu belirleyin
            String command = currentState ? offCommand : onCommand;

            // Bluetooth veya Wi-Fi moduna göre komut gönderin
            if (switch1.isChecked()) {
                // Bluetooth mode
                if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                    sendCommand(command);
                } else {
                    Toast.makeText(this, "Bluetooth not connected", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Wi-Fi mode
                new Thread(() -> {
                    String url = BASE_URL + command;
                    Log.d("Command", url);
                    Request request = new Request.Builder().url(url).build();
                    try {
                        Response response = client.newCall(request).execute();
                        if (!response.isSuccessful()) {
                            Log.e("HTTP Error", "Response not successful: " + response.code());
                        }
                        String myResponse = response.body().string();
                        Log.d("Response", myResponse);
                    } catch (IOException e) {
                        Log.e("IO Error", "Error while sending command", e);
                    }
                }).start();
            }
        }
    }



    private void sendCommand(String cmd) {
        if (switch1.isChecked()) {
            // Bluetooth mode
            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                try {
                    outputStream.write(cmd.getBytes());
                } catch (IOException e) {
                    Log.e(TAG, "Error while sending command via Bluetooth", e);
                }
            } else {
                Toast.makeText(this, "Bluetooth not connected", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Wi-Fi mode
            new Thread(() -> {
                String command = BASE_URL + cmd;
                Log.d("Command", command);
                Request request = new Request.Builder().url(command).build();
                try {
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        Log.e("HTTP Error", "Response not successful: " + response.code());
                    }
                    String myResponse = response.body().string();
                    Log.d("Response", myResponse);
                } catch (IOException e) {
                    Log.e("IO Error", "Error while sending command", e);
                }
            }).start();
        }
    }

    private void connectToDevice(String deviceName) throws SecurityException {
        // Eğer halihazırda bir cihaza bağlıysak ve bu cihaz "ESP32_BT" ise, yeni bağlantı deneme
        if (bluetoothSocket != null && bluetoothSocket.isConnected() && bluetoothDevice != null && bluetoothDevice.getName().equals(deviceName)) {
            Toast.makeText(this, "Already connected to " + deviceName, Toast.LENGTH_SHORT).show();
            appendLog("Already connected to " + deviceName);
            return;
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(deviceName)) {
                bluetoothDevice = device;
                break;
            }
        }

        if (bluetoothDevice == null) {
            Toast.makeText(this, "Device not found", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            Toast.makeText(this, "Connected to device", Toast.LENGTH_SHORT).show();
            appendLog("Connected to device");

            // Veri okuma işlemini yeni bir thread içinde başlat
            new Thread(new Runnable() {
                public void run() {
                    readDataFromBluetooth();
                }
            }).start();
        } catch (IOException e) {
            Log.e(TAG, "Connection failed", e);
            appendLog("Connection failed");
        }
    }

    private void readDataFromBluetooth() {
        InputStream inputStream = null;
        try {
            inputStream = bluetoothSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        byte[] buffer = new byte[1024];  // Gelen veriyi depolamak için bir buffer
        int bytes;  // Okunan byte sayısı
        while (true) {
            try {
                bytes = inputStream.read(buffer);  // Veri geldiğinde okuma
                String incomingMessage = new String(buffer, 0, bytes);  // String'e çevir
                if (incomingMessage.startsWith("IP:")) {
                    String ipAddress = incomingMessage.substring(3).trim();
                    String formattedAddress = "http://" + ipAddress + "/";
                    // SharedPreferences'ı güncelle
                    SharedPreferences sharedPref = getSharedPreferences("LEDNames", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("wifi_server", formattedAddress);  // Yeni IP adresini kaydet
                    editor.apply();  // Değişiklikleri uygula
                    updateBaseUrl();

                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), ipAddress, Toast.LENGTH_SHORT).show();
                    });
                } else {
                    // Başka türde gelen veri
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "Gelen veri: " + incomingMessage, Toast.LENGTH_SHORT).show();
                    });
                }

            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }



    private void disconnectDevice() {
        // "ESP32_BT" cihazına zaten bağlı mı kontrol et
        if (bluetoothSocket != null && bluetoothSocket.isConnected() && bluetoothDevice != null && "ESP32_BT".equals(bluetoothDevice.getName())) {
            new Thread(() -> {
                try {
                    // LED'leri kapatmak için komutlar gönder
                    sendCommand("10"); // LED 1'i kapat
                    Thread.sleep(500); // 500 ms bekle
                    sendCommand("20"); // LED 2'yi kapat
                    Thread.sleep(500); // 500 ms bekle
                    sendCommand("30"); // LED 3'ü kapat
                    Thread.sleep(500); // 500 ms bekle

                    // Bluetooth bağlantısını kapat
                    bluetoothSocket.close();
                    bluetoothSocket = null;
                    outputStream = null;
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
                        appendLog("Disconnected");
                    });
                } catch (IOException e) {
                    Log.e(TAG, "Disconnection failed", e);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Disconnection failed", Toast.LENGTH_SHORT).show();
                        appendLog("Disconnection failed");
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Log.e(TAG, "Thread interrupted", e);
                }
            }).start();
        } else {
            // Zaten "ESP32_BT" cihazına bağlı değilse bir şey yapma
            runOnUiThread(() -> {
                Toast.makeText(this, "No connection to disconnect", Toast.LENGTH_SHORT).show();
                appendLog("No connection to disconnect");
            });
        }
    }


    private void appendLog(String message) {
        runOnUiThread(() -> logTextView.append(message + "\n"));
    }

    private void handleBluetoothMode() {
        bluetooth_open_btn.setVisibility(View.VISIBLE);
        bluetooth_close_btn.setVisibility(View.GONE);
        wifi_open_btn.setVisibility(View.GONE);
        wifi_close_btn.setVisibility(View.VISIBLE);
        connectButton.setVisibility(View.VISIBLE);
        disconnectButton.setVisibility(View.VISIBLE);
        logTextView.setVisibility(View.VISIBLE);

        enableBluetooth();
        disableWifi();
    }

    private void handleWifiMode() {
        // Make the relevant UI changes
        bluetooth_open_btn.setVisibility(View.GONE);
        bluetooth_close_btn.setVisibility(View.VISIBLE);
        wifi_open_btn.setVisibility(View.VISIBLE);
        wifi_close_btn.setVisibility(View.GONE);
        connectButton.setVisibility(View.GONE);
        disconnectButton.setVisibility(View.GONE);
        logTextView.setVisibility(View.GONE);

        // Only disconnect the "ESP32_BT" device if connected
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (bluetoothSocket != null && bluetoothDevice != null && "ESP32_BT".equals(bluetoothDevice.getName())) {
            disconnectDevice(); // Only disconnects the ESP32_BT device
        }

        // Add a delay before enabling Wi-Fi
        handler.postDelayed(this::enableWifi, 1000);
    }

    private void enableBluetooth() {
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            bluetoothAdapter.enable();
        }
    }

    private void disableBluetooth() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            bluetoothAdapter.disable();
        }
    }

    private void disableWifi() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager != null) {
            connManager.bindProcessToNetwork(null); // Unbind from current network
        }
    }

    private void enableWifi() {
        setupNetworkConnection(); // Re-setup Wi-Fi connection
    }
}
