package com.example.coen390_feverapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ScanMeasurementActivity extends AppCompatActivity {

    private static final String DEVICE_NAME = "ESP32";
    private static final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private InputStream inputStream;

    protected TextView temperatureTextView;
    protected ProgressBar measurementProgressBar;
    protected LinearLayout cancelAndSaveButtonLayout;
    protected Button startButton, cancelButton, saveButton;
    protected ConstraintLayout dialogLayout;
    protected FloatingActionButton closeDialogButton;
    protected ImageView imageViewArrowScanPage;

    protected volatile boolean measurementCanceled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan_measurement);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupUI();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Attempt connection on a background thread
        new Thread(() -> connectToESP32()).start();
        dialogLayout.setVisibility(android.view.View.VISIBLE);
    }

    private void setupUI() {
        temperatureTextView = findViewById(R.id.textViewTemperature);
        startButton = findViewById(R.id.buttonStart);
        measurementProgressBar = findViewById(R.id.measurementProgressBar);
        cancelAndSaveButtonLayout = findViewById(R.id.linearLayoutCancelAndSave);
        cancelButton = findViewById(R.id.buttonCancel);
        saveButton = findViewById(R.id.buttonProgressAndSave);
        dialogLayout = findViewById(R.id.scanInstructionDialog);
        closeDialogButton = findViewById(R.id.closeScanDialogButton);
        imageViewArrowScanPage = findViewById(R.id.imageViewArrowScanPage);

        measurementProgressBar.setProgress(0);
        cancelAndSaveButtonLayout.setVisibility(android.view.View.GONE);
        startButton.setVisibility(android.view.View.VISIBLE);

        startButton.setOnClickListener(v -> startMeasurement());
        cancelButton.setOnClickListener(v -> cancelMeasurement());
        saveButton.setOnClickListener(v -> saveMeasurement());
        closeDialogButton.setOnClickListener(v -> dialogLayout.setVisibility(android.view.View.GONE));

        imageViewArrowScanPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ScanMeasurementActivity.this, TemperatureMeasurementPage.class);
                startActivity(intent);
            }
        });
    }

    // Attempt to connect to the ESP32 on a background thread
    private boolean connectToESP32() {
        BluetoothDevice device = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            return false;
        }
        for (BluetoothDevice bondedDevice : bluetoothAdapter.getBondedDevices()) {
            if (bondedDevice.getName().equals(DEVICE_NAME)) {
                device = bondedDevice;
                break;
            }
        }
        if (device == null) {
            runOnUiThread(() -> Toast.makeText(ScanMeasurementActivity.this, "ESP32 not paired", Toast.LENGTH_SHORT).show());
            return false;
        }
        try {
            socket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);
            socket.connect();
            inputStream = socket.getInputStream();
            runOnUiThread(() -> Toast.makeText(ScanMeasurementActivity.this, "Connected Successfully", Toast.LENGTH_SHORT).show());
            return true;
        } catch (IOException e) {
            runOnUiThread(() -> Toast.makeText(ScanMeasurementActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show());
            return false;
        }
    }

    // Starts the measurement on a background thread
    private void startMeasurement() {
        measurementCanceled = false;
        runOnUiThread(() -> {
            startButton.setVisibility(android.view.View.GONE);
            cancelAndSaveButtonLayout.setVisibility(android.view.View.VISIBLE);
            saveButton.setClickable(false);
            measurementProgressBar.setProgress(0);
            temperatureTextView.setText(" Measuring... ");
        });

        new Thread(() -> {
            if (socket == null || !socket.isConnected()) {
                if (!connectToESP32()) {
                    runOnUiThread(() -> {
                        Toast.makeText(ScanMeasurementActivity.this, "Unable to connect", Toast.LENGTH_SHORT).show();
                        startButton.setVisibility(android.view.View.VISIBLE);
                        cancelAndSaveButtonLayout.setVisibility(android.view.View.GONE);
                    });
                    return;
                }
            }

            long startTime = System.currentTimeMillis();
            int duration = 3000; // 8 seconds measurement period
            while (System.currentTimeMillis() - startTime < duration && !measurementCanceled) {
                String temp = readTemperature();
                if (temp == null) {
                    temp = "Error";
                }
                long elapsed = System.currentTimeMillis() - startTime;
                int progress = (int) ((elapsed / (float) duration) * 100);
                String finalTemp = temp;
                runOnUiThread(() -> {
                    temperatureTextView.setText(" " + finalTemp + " °C ");
                    measurementProgressBar.setProgress(progress);
                    saveButton.setText(progress + "%");
                });
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }
            runOnUiThread(() -> {
                saveButton.setText("SAVE");
                saveButton.setClickable(true);
            });
        }).start();
    }

    private void cancelMeasurement() {
        measurementCanceled = true;
        closeBluetoothConnection();
        runOnUiThread(() -> {
            cancelAndSaveButtonLayout.setVisibility(android.view.View.GONE);
            startButton.setVisibility(android.view.View.VISIBLE);
            temperatureTextView.setText(" 0 °C ");
        });
    }

    private void saveMeasurement() {

        String temperatureText = temperatureTextView.getText().toString().trim();

        temperatureText = temperatureText.replace("°C", "").trim();

        String measurementTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Retrieve current profile name from SharedPreferences.
        // (Ensure that you save the profile name there when the user selects it in TemperatureMeasurementPage.)
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentProfile = sharedPrefs.getString("current_profile", "default");

        // Save the measurement to the temperature table using the profile name
        DBHelper dbHelper = new DBHelper(this);
        boolean inserted = dbHelper.insertTemperature(currentProfile, measurementTime, temperatureText);

        if (inserted) {
            Toast.makeText(ScanMeasurementActivity.this, "Temperature has been saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ScanMeasurementActivity.this, "Error saving temperature", Toast.LENGTH_SHORT).show();
        }

        closeBluetoothConnection();
        Intent intent = new Intent(ScanMeasurementActivity.this, TemperatureMeasurementPage.class);
        startActivity(intent);
        finish();
    }

    private String readTemperature() {
        if (socket == null || !socket.isConnected()) {
            return null;
        }
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write("GET_TEMP\n".getBytes());
            outputStream.flush();

            byte[] buffer = new byte[256];
            int bytesRead = inputStream.read(buffer);
            return new String(buffer, 0, bytesRead).trim();
        } catch (IOException e) {
            return null;
        }
    }

    private void closeBluetoothConnection() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket = null;
        inputStream = null;
    }


}