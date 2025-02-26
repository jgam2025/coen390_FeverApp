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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

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
    private TextView textViewShowTemp;
    private Button buttonSaveTemp;

    private static final String DEVICE_NAME = "ESP32";
    private static final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private InputStream inputStream;

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
        textViewShowTemp = findViewById(R.id.textViewShowTemp);
        buttonSaveTemp = findViewById(R.id.buttonSaveTemp);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null){
            Toast.makeText(this, "Bluetooth not supposed", Toast.LENGTH_SHORT).show();
            finish();
        }

        buttonSaveTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "Button clicked!");  // Debugging log
                Toast.makeText(ScanMeasurementActivity.this, "Connecting...", Toast.LENGTH_SHORT).show();
                connectToESP32();
            }
        });

    }

    private void connectToESP32() {
        BluetoothDevice device = null;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
            return;
        }


        for (BluetoothDevice bondedDevice : bluetoothAdapter.getBondedDevices()) {
            if (bondedDevice.getName().equals("ESP32")) { // Make sure this matches ESP32 name
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

            // Send the request for temperature
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write("GET_TEMP\n".getBytes()); // Send command to ESP32
            outputStream.flush();

            readTemperature(); // Read the response from ESP32
        } catch (IOException e) {
            Toast.makeText(this, "Failed to connect", Toast.LENGTH_SHORT).show();
        }
    }


    private void readTemperature() {
        new Thread(() -> {
            try {
                byte[] buffer = new byte[256];
                int bytesRead = inputStream.read(buffer);
                String receivedData = new String(buffer, 0, bytesRead).trim();

                runOnUiThread(() -> textViewShowTemp.setText(receivedData + " °C")); // Update UI
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "Error reading data", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}