package com.example.coen390_feverapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.UUID;

public class CalibrationActivity extends AppCompatActivity {

    private static final String DEVICE_NAME = "ESP32";
    private static final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private InputStream inputStream;

    private TextView instructionTextView;
    private Button measureButton;
    private TextView measuredTempTextView;
    private TextView offsetTextView;
    //protected ImageView imageViewArrowScanPage2;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calibration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setUpToolbar();
        // Initialize UI elements

        measureButton = findViewById(R.id.button_measure);
        measuredTempTextView = findViewById(R.id.textView_measuredTemp);
        offsetTextView = findViewById(R.id.textView_offset);
        //imageViewArrowScanPage2 = findViewById(R.id.imageViewArrowScanPage2);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Attempt connection to the ESP32 sensor
        if (!connectToESP32()) {
            Toast.makeText(this, "Unable to connect to sensor.", Toast.LENGTH_LONG).show();
        }

        // Set up button to initiate calibration
        measureButton.setOnClickListener(v -> calibrateSensor());

    }
    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private boolean connectToESP32() {
        BluetoothDevice device = null;
        // Check for the BLUETOOTH_CONNECT permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Request permission from the UI thread
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                runOnUiThread(() -> ActivityCompat.requestPermissions(
                        CalibrationActivity.this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        1));
            }
            return false;
        }
        // Find the bonded device named "ESP32"
        for (BluetoothDevice bondedDevice : bluetoothAdapter.getBondedDevices()) {
            if (bondedDevice.getName().equals(DEVICE_NAME)) {
                device = bondedDevice;
                break;
            }
        }
        if (device == null) {
            runOnUiThread(() -> Toast.makeText(CalibrationActivity.this, "ESP32 not paired", Toast.LENGTH_SHORT).show());
            return false;
        }
        try {
            socket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);
            socket.connect();
            inputStream = socket.getInputStream();
            runOnUiThread(() -> Toast.makeText(CalibrationActivity.this, "Connected Successfully", Toast.LENGTH_SHORT).show());
            return true;
        } catch (IOException e) {
            runOnUiThread(() -> Toast.makeText(CalibrationActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show());
            return false;
        }
    }

    // Handle the permission request result:
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted – try connecting again on a new thread
                new Thread(() -> connectToESP32()).start();
            } else {
                Toast.makeText(this, "Bluetooth permission is required.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Calibrate sensor using the boiling water test
    private void calibrateSensor() {
        String sensorReading = readTemperature();
        if (sensorReading == null) {
            Toast.makeText(CalibrationActivity.this, "Error reading sensor.", Toast.LENGTH_SHORT).show();
            return;
        }
        double measuredTemp;
        try {
            measuredTemp = Double.parseDouble(sensorReading);
        } catch (NumberFormatException e) {
            Toast.makeText(CalibrationActivity.this, "Invalid sensor reading.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Display the measured temperature
        measuredTempTextView.setText(String.format(Locale.getDefault(), "Measured Temp: %.2f °C", measuredTemp));

        // Calculate calibration offset (expected 100°C minus measured temperature)
        double offset = 100.0 - measuredTemp;
        offsetTextView.setText(String.format(Locale.getDefault(), "Calibration Offset: %.2f °C", offset));

        // Save the offset to SharedPreferences for later use (e.g., in ScanMeasurementActivity)
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().putFloat("calibrationOffset", (float) offset).apply();

        Toast.makeText(CalibrationActivity.this, "Calibration saved!", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close Bluetooth connection if it exists
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu from the menu.xml file in the menu directory
        getMenuInflater().inflate(R.menu.toolbar, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.miperson) {
            goToHealth();
            return true;

        } else if (id == R.id.miMore) {
            goToExtra();
            return true;
        } else if (id == R.id.mihome) {
            goToHome();
            return true;

        } else {
            return super.onOptionsItemSelected(item);

        }
    }

    private void goToHealth() {
        Intent intent = new Intent(this, HealthDataActivity.class);
        startActivity(intent);
    }

    private void goToHome() {
        Intent intent = new Intent(this, BaseActivity.class);
        startActivity(intent);
    }

    private void goToExtra() {
        Intent intent = new Intent(this, ExtraPageActivity.class);
        startActivity(intent);
    }

}