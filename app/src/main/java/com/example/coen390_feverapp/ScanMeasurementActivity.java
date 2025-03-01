package com.example.coen390_feverapp;
import android.Manifest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ScanMeasurementActivity extends AppCompatActivity {


    // Bluetooth Variables
    private static final String DEVICE_NAME = "ESP32";
    private static final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private InputStream inputStream;

    // UI Variables
    protected TextView textViewTemperature;
    protected ProgressBar measurementProgressBar;
    protected LinearLayout cancelAndSaveButtonLayout;
    protected Button buttonStart, buttonCancel, buttonSave;

    // Other Variables
    protected boolean measurementCanceled = false;
    protected String temperature;



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
    }

    private void setupUI(){
        textViewTemperature = findViewById(R.id.textViewTemperature);
        buttonStart = findViewById(R.id.buttonStart);
        measurementProgressBar = findViewById(R.id.measurementProgressBar);
        cancelAndSaveButtonLayout = findViewById(R.id.linearLayoutCancelAndSave);
        buttonStart = findViewById(R.id.buttonStart);
        buttonCancel = findViewById(R.id.buttonCancel);
        buttonSave = findViewById(R.id.buttonProgressAndSave);

        cancelAndSaveButtonLayout.setVisibility(View.GONE);
        buttonStart.setVisibility(View.VISIBLE);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null){
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonStart.setOnClickListener(v -> startMeasurement());
        buttonCancel.setOnClickListener(v -> cancelMeasurement());
        buttonSave.setOnClickListener(v -> saveMeasurement());

    }
    private void startMeasurement(){
        buttonStart.setVisibility(View.GONE);
        cancelAndSaveButtonLayout.setVisibility(View.VISIBLE);
        buttonSave.setClickable(false);
        measurementProgressBar.setProgress(0);

        Log.d("DEBUG", "Button clicked!");
        Toast.makeText(ScanMeasurementActivity.this, "Connecting...", Toast.LENGTH_SHORT).show();
        connectToESP32();

        //start measuring for 8 seconds
        long a, b;
        int duration = 8000; // time in milliseconds Ex:8000ms=8s
        a = System.currentTimeMillis();
        while(true) {
            if (measurementCanceled) break;

            // for 8 seconds
            b = System.currentTimeMillis();
            if (b - a >= duration) break;

            // Bluetooth measure
            temperature = readTemperature();
            if (temperature == null) {
                Toast.makeText(this, "Error reading data", Toast.LENGTH_SHORT).show();
                temperature = "Null";
            }

            // display measure
            textViewTemperature.setText(temperature + " °C");

            // display progression
            measurementProgressBar.setProgress((int)((b - a)/duration)*100,true);
            buttonSave.setText(((int)((b - a)/duration)*100)+"%");
        }

        //* activate save button and display "save" instead of percentage
        buttonSave.setText("save");
        buttonSave.setClickable(true);
    }

    private void cancelMeasurement(){
        cancelAndSaveButtonLayout.setVisibility(View.GONE);
        buttonStart.setVisibility(View.VISIBLE);
        measurementCanceled = true;
        textViewTemperature.setText("0 °C");
    }
    private void saveMeasurement(){
        //TODO: create a way to save the data on a database
    }

    /*
    *
    * All Bluetooth methods are below this comment
    *
    */

    private void connectToESP32() {
        BluetoothDevice device = null;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            return;
        }


        for (BluetoothDevice bondedDevice : bluetoothAdapter.getBondedDevices()) {
            if (bondedDevice.getName().equals("ESP32")) { 
                device = bondedDevice;
                break;
            }
        }

        if (device == null) {
            Toast.makeText(this, "ESP32 not paired", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            socket.connect();
            inputStream = socket.getInputStream();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to connect", Toast.LENGTH_SHORT).show();
        }
    }

    private String readTemperature() {
        try {
            // Send the request for temperature
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write("GET_TEMP\n".getBytes()); // Send command to ESP32
            outputStream.flush();

            // Read incoming temperature
            byte[] buffer = new byte[256];
            int bytesRead = inputStream.read(buffer);
            String receivedData = new String(buffer, 0, bytesRead).trim();

            return receivedData;

        }catch (IOException e) {
            return null;
        }
    }
}