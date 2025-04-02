package com.example.coen390_feverapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class ExportDataActivity extends AppCompatActivity {

    Button btnExportAll, btnSaveUserInfo;

    EditText etFirstName, etLastName, etBirthDate, etHealthCardNumber, etAddress, etDoctorName;
    SharedPreferences sharedPreferences;
    String currentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_data);

        // User info SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentProfile = prefs.getString("current_profile", "default");
        sharedPreferences = getSharedPreferences("user_info_" + currentProfile, MODE_PRIVATE);

        // Connect views
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etBirthDate = findViewById(R.id.etBirthDate);
        etHealthCardNumber = findViewById(R.id.etHealthCardNumber);
        etAddress = findViewById(R.id.etAddress);
        etDoctorName = findViewById(R.id.etDoctorName);
        btnSaveUserInfo = findViewById(R.id.btnSaveUserInfo);
        btnExportAll = findViewById(R.id.btnExportAll);


        etFirstName.setText(sharedPreferences.getString("first_name", ""));
        etLastName.setText(sharedPreferences.getString("last_name", ""));
        etBirthDate.setText(sharedPreferences.getString("birth_date", ""));
        etHealthCardNumber.setText(sharedPreferences.getString("health_card", ""));
        etAddress.setText(sharedPreferences.getString("address", ""));
        etDoctorName.setText(sharedPreferences.getString("doctor", ""));

        //save infos
        btnSaveUserInfo.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("first_name", etFirstName.getText().toString());
            editor.putString("last_name", etLastName.getText().toString());
            editor.putString("birth_date", etBirthDate.getText().toString());
            editor.putString("health_card", etHealthCardNumber.getText().toString());
            editor.putString("address", etAddress.getText().toString());
            editor.putString("doctor", etDoctorName.getText().toString());
            editor.apply();
            Toast.makeText(this, "User info saved ", Toast.LENGTH_SHORT).show();
        });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }

        // Export all
        btnExportAll.setOnClickListener(v -> exportAllTemperatureData());

    }

    private void exportAllTemperatureData() {
        DBHelper dbHelper = new DBHelper(this);
        List<String> temps = dbHelper.getAllTemperatures(currentProfile);
        List<String> meds = dbHelper.getAllMedications(currentProfile);

        if (temps.isEmpty() && meds.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }

        writeExportToFile(currentProfile + "_export.csv", temps, meds, currentProfile);

    }



    private void writeExportToFile(String filename, List<String> temps, List<String> meds, String profileLabel) {

        String firstName = sharedPreferences.getString("first_name", "");
        String lastName = sharedPreferences.getString("last_name", "");
        String birthDate = sharedPreferences.getString("birth_date", "");
        String healthCard = sharedPreferences.getString("health_card", "");
        String address = sharedPreferences.getString("address", "");
        String doctor = sharedPreferences.getString("doctor", "");

        StringBuilder data = new StringBuilder();
        data.append("---- Health Information ----\n");
        data.append("Name: ").append(firstName).append(" ").append(lastName).append("\n");
        data.append("Date of Birth: ").append(birthDate).append("\n");
        data.append("Health Card #: ").append(healthCard).append("\n");
        data.append("Address: ").append(address).append("\n");
        data.append("Family Doctor: ").append(doctor).append("\n\n");

        data.append("---- Temperature Records: ").append(" ----\n");
        data.append("Timestamp,Temperature\n");
        for (String temp : temps) {
            data.append(temp).append("\n");
        }

        data.append("\n---- Medication Records for: ").append(" ----\n");
        data.append("Date,Medication,Dose\n");
        for (String med : meds) {
            data.append(med).append("\n");
        }

        try {
            File dir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS);

            File file = new File(dir, filename);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data.toString().getBytes());
            fos.close();

            Toast.makeText(this, "Exported to:\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



}
