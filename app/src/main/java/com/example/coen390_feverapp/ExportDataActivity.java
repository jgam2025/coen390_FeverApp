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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.annotation.SuppressLint;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import android.content.Context;
import android.app.DatePickerDialog;




import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class ExportDataActivity extends AppCompatActivity {

    Button btnExportAll, btnSaveUserInfo;

    EditText etFirstName, etLastName, etBirthDate, etHealthCardNumber, etAddress, etDoctorName;
    SharedPreferences sharedPreferences;
    String currentProfile;
    EditText etStartDate, etEndDate;


    private final Context context = this;



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
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);



        etFirstName.setText(sharedPreferences.getString("first_name", ""));
        etLastName.setText(sharedPreferences.getString("last_name", ""));
        etBirthDate.setText(sharedPreferences.getString("birth_date", ""));
        etHealthCardNumber.setText(sharedPreferences.getString("health_card", ""));
        etAddress.setText(sharedPreferences.getString("address", ""));
        etDoctorName.setText(sharedPreferences.getString("doctor", ""));
        etStartDate.setOnClickListener(v -> showDatePickerDialog(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePickerDialog(etEndDate));

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
        btnExportAll.setOnClickListener(v -> exportAllDataWithDateRange());

        // Export all
     //   btnExportAll.setOnClickListener(v -> exportAllTemperatureData());

    }
    public List<String> getTemperatureHistoryList(String profile, String startDate, String endDate) {
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        List<String> temps = new ArrayList<>();
        Cursor cursor = null;

        try {
            if (endDate == null) {
                endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Calendar.getInstance().getTime());
            }

            cursor = db.rawQuery("SELECT measurement_time, temperature_value FROM temperature WHERE profile_name = ? AND measurement_time BETWEEN ? AND ? ORDER BY measurement_time ASC",
                    new String[]{profile, startDate, endDate});

            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("measurement_time"));
                    @SuppressLint("Range") String temp = cursor.getString(cursor.getColumnIndex("temperature_value"));
                    temps.add(time + "," + temp);
                } while (cursor.moveToNext());
            }

            cursor.close();
        } catch (Exception e) {
            Toast.makeText(context, "Temp error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return temps;
    }


    private void writeExportToFile(String filename, List<String> temps, List<String> meds, List<String> symptoms, String profileLabel)
    {

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
        data.append("\n---- Symptom Records ----\n");
        data.append("Date & Time, Symptoms\n");
        for (String symptom : symptoms) {
            data.append(symptom).append("\n");
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

    private void exportAllDataWithDateRange() {
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();

        if (startDate.isEmpty()) {
            Toast.makeText(this, "Please select a start date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (endDate.isEmpty()) {
            endDate = null;
        } else {
            endDate += " 23:59";
        }

        DBHelper dbHelper = new DBHelper(this);
        List<String> temps = dbHelper.getTemperatureHistoryList(currentProfile, startDate, endDate);
        List<String> meds = dbHelper.getMedicationHistoryList(currentProfile, startDate, endDate);
        List<String> symptoms = dbHelper.getSymptomHistory(currentProfile, startDate, endDate);


        if (temps.isEmpty() && meds.isEmpty()) {
            Toast.makeText(this, "No data in that range", Toast.LENGTH_SHORT).show();
            return;
        }

        String filename = currentProfile + "_export_" + startDate + ".csv";
        writeExportToFile(filename, temps, meds, symptoms, currentProfile);
    }

    private void showDatePickerDialog(EditText target) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String dateFormatted = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    target.setText(dateFormatted);
                },
                year, month, day
        );

        datePickerDialog.show();
    }


}
