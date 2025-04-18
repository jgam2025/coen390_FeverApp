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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;


public class ScanMeasurementActivity extends AppCompatActivity {
    private static final boolean TEST_MODE = false;
    //name of device = ESP32
    private static final String DEVICE_NAME = "ESP32";
    //serial uuid of esp32
    private static final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private InputStream inputStream;
    protected TextView temperatureTextView;
    protected ProgressBar measurementProgressBar;
    protected LinearLayout cancelAndSaveButtonLayout;
    protected Button startButton, cancelButton, saveButton;
    protected ConstraintLayout instructionDialogLayout, feverAlertDialogLayout;
    protected FloatingActionButton closeInstructionDialogButton, closeAlertDialogButton;
    protected ImageView imageViewArrowScanPage;
    private String selectedProfile;
    protected volatile boolean measurementCanceled = false;
    private double calibrationOffset = 0.0;
    private Spinner scaleSpinner, selectProfileSpinner;
    private String temperatureScaleText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scan_measurement);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.checkBoxLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupUI();

        temperatureScaleText = getSharedPreferences("user_prefs", MODE_PRIVATE).getString("temperatureScaleText", "°C");
        calibrationOffset = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getFloat("calibrationOffset", 0.0f);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //sends message to user if their device does not support bluetooth
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }
        new Thread(() -> connectToESP32()).start();
        instructionDialogLayout.setVisibility(android.view.View.VISIBLE);
    }

    private void setupUI() {
        temperatureTextView = findViewById(R.id.textViewTemperature);
        startButton = findViewById(R.id.buttonStart);
        measurementProgressBar = findViewById(R.id.measurementProgressBar);
        cancelAndSaveButtonLayout = findViewById(R.id.linearLayoutCancelAndSave);
        cancelButton = findViewById(R.id.buttonCancel);
        saveButton = findViewById(R.id.buttonProgressAndSave);
        instructionDialogLayout = findViewById(R.id.scanInstructionDialog);
        closeInstructionDialogButton = findViewById(R.id.closeScanDialogButton);
        feverAlertDialogLayout = findViewById(R.id.feverAlertDialog);
        closeAlertDialogButton = findViewById(R.id.closeFeverAlertDialogButton);
        imageViewArrowScanPage = findViewById(R.id.imageViewArrowScanPage);

        //progress bar
        measurementProgressBar.setProgress(0);
        cancelAndSaveButtonLayout.setVisibility(android.view.View.GONE);
        startButton.setVisibility(android.view.View.VISIBLE);

        startButton.setOnClickListener(v -> startMeasurement());
        cancelButton.setOnClickListener(v -> cancelMeasurement());
        saveButton.setOnClickListener(v -> saveMeasurement());
        closeInstructionDialogButton.setOnClickListener(v -> instructionDialogLayout.setVisibility(android.view.View.GONE));
        closeAlertDialogButton.setOnClickListener(v -> feverAlertDialogLayout.setVisibility(android.view.View.GONE));

        selectProfileSpinner = findViewById(R.id.selectProfileSpinner);
        showProfilesOnSpinner();

        //allows user to choose unit of measurement
        scaleSpinner = findViewById(R.id.scaleSpinner);
        String[] scales = {"°C", "°F"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, scales);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scaleSpinner.setAdapter(adapter);

        temperatureScaleText = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("temperatureScaleText", "°C");
        scaleSpinner.setSelection(temperatureScaleText.equals("°F") ? 1 : 0);

        scaleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                temperatureScaleText = parent.getItemAtPosition(pos).toString();
                getSharedPreferences("user_prefs", MODE_PRIVATE)
                        .edit().putString("temperatureScaleText", temperatureScaleText).apply();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });

        imageViewArrowScanPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ScanMeasurementActivity.this, BaseActivity.class);
                startActivity(intent);
            }
        });

    }

    //function to connect to ESP32 with bluetooth permissions
    private boolean connectToESP32() {

        //test mode for team members that wanted to test their code but did not have the physical sensor with them
        if (TEST_MODE) {
            runOnUiThread(() -> Toast.makeText(this, "In Test Mode, connection simulated", Toast.LENGTH_SHORT).show());
            return true;
        }

        BluetoothDevice device = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                runOnUiThread(() -> ActivityCompat.requestPermissions(
                        ScanMeasurementActivity.this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        1));
            }
            return false;
        }
        for (BluetoothDevice bondedDevice : bluetoothAdapter.getBondedDevices()) {
            if (bondedDevice.getName().equals(DEVICE_NAME)) {
                device = bondedDevice;
                break;
            }
        }
        //sends a message to user is esp32 is not paired
        if (device == null) {
            runOnUiThread(() -> Toast.makeText(ScanMeasurementActivity.this, "ESP32 not paired", Toast.LENGTH_SHORT).show());
            return false;
        }
        try {
            socket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);
            socket.connect();
            inputStream = socket.getInputStream();
            //sends a message to user when device is properly connected
            runOnUiThread(() -> Toast.makeText(ScanMeasurementActivity.this, "Connected Successfully", Toast.LENGTH_SHORT).show());
            return true;
        } catch (IOException e) {
            //otherwise, lets user know that device has failed to connect
            runOnUiThread(() -> Toast.makeText(ScanMeasurementActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show());
            return false;
        }
    }

    //function to request bluetooth permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new Thread(() -> connectToESP32()).start();
            } else {
                Toast.makeText(this, "Bluetooth permission is required.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //function to begin taking measurements
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

            //takes measurements over a 3 second interval, last measurement value is displayed to user
            long startTime = System.currentTimeMillis();
            int duration = 3000;

            while (System.currentTimeMillis() - startTime < duration && !measurementCanceled) {
                String temp = readTemperature();
                if (temp == null) temp = "Error";

                long elapsed = System.currentTimeMillis() - startTime;
                int progress = (int) ((elapsed / (float) duration) * 100);
                String finalTemp = temp;

                //implements calibration offset if there is one
                runOnUiThread(() -> {
                    String display;
                    try {
                        double c = Double.parseDouble(finalTemp);
                        c = c - calibrationOffset;

                        if (temperatureScaleText.equals("°F")) {
                            display = String.format(Locale.getDefault(), " %.2f °F ", (c * 9/5) + 32);
                        } else {
                            display = String.format(Locale.getDefault(), " %.2f °C ", c);
                        }
                    } catch (NumberFormatException e) {
                        display = finalTemp + " " + temperatureScaleText;
                    }

                    temperatureTextView.setText(" " + display);
                    measurementProgressBar.setProgress(progress);
                    saveButton.setText(progress + "%");
                });

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            runOnUiThread(() -> {
                saveButton.setText("SAVE");
                saveButton.setClickable(true);
            });
        }).start();
    }

    //allows user to cancel measurement taken and retake
    private void cancelMeasurement() {
        measurementCanceled = true;
        closeBluetoothConnection();
        runOnUiThread(() -> {
            cancelAndSaveButtonLayout.setVisibility(android.view.View.GONE);
            startButton.setVisibility(android.view.View.VISIBLE);
            temperatureTextView.setText(" Press Start ");
        });
    }

    //allows user to save measurement to database based on a specific user profile
    private void saveMeasurement() {
        String measurementValueStr = temperatureTextView.getText().toString().trim();

        String measurementTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentProfile = sharedPrefs.getString("current_profile", "default");

        if(selectedProfile != "Select profile") {
            DBHelper dbHelper = new DBHelper(this);
            boolean inserted = dbHelper.insertTemperature(currentProfile, measurementTime, measurementValueStr);

            if (inserted) {
                Toast.makeText(ScanMeasurementActivity.this, "Temperature has been saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ScanMeasurementActivity.this, "Error saving temperature", Toast.LENGTH_SHORT).show();
            }
        } else if (selectedProfile == "Select profile"){
            Toast.makeText(ScanMeasurementActivity.this,"Please select a profile", Toast.LENGTH_LONG).show();
        }
        double measurementValue = 0.0;
        try {
            String[] parts = measurementValueStr.split(" ");
            measurementValue = Double.parseDouble(parts[0]);
            if (measurementValueStr.contains("°F")) {
                measurementValue = (measurementValue - 32) * 5.0/9.0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
            closeBluetoothConnection();
            Intent intent = new Intent(ScanMeasurementActivity.this, BaseActivity.class);
            intent.putExtra("measurement", measurementValue);
            startActivity(intent);
            finish();
    }

    private String readTemperature() {

        if (TEST_MODE) {

            double test = 36.0 + Math.random() * 3.5;
            return String.format(Locale.getDefault(), "%.2f", test);
        }
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

    //close connection
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

    private void showProfilesOnSpinner(){
        DBHelper dbHelper = new DBHelper(this);
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUser = sharedPrefs.getString("current_user",null);
        List<String> profileList = dbHelper.getProfiles(currentUser);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,profileList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectProfileSpinner.setAdapter(adapter);

        selectProfileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedProfile = (String) parent.getItemAtPosition(position);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString("current_profile", selectedProfile);
                editor.apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
}
