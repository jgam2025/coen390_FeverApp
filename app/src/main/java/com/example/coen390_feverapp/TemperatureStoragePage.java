package com.example.coen390_feverapp;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class TemperatureStoragePage extends AppCompatActivity {

    private TextView textViewLastTemperature;
    private Spinner spinnerMonth, spinnerDay;
    private ListView listViewTemperatureHistory;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_temperature_storage_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide the title
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textViewLastTemperature = findViewById(R.id.textViewLastTemperature);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerDay = findViewById(R.id.spinnerDay);
        listViewTemperatureHistory = findViewById(R.id.listViewTemperatureHistory);

        dbHelper = new DBHelper(this);

        // Load and display the last temperature measurement
        loadLastTemperature();

        // Setup the month and day spinners
        setupSpinners();

        // Initial population of the history list based on default spinner selections
        updateTemperatureHistory();
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

        if (id == R.id.miMore) {
            goToExtraPage();
            return true;
        } else if (id == R.id.miadd) {
            addProfile();
            return true;

        }else if (id == R.id.miThermometer) {
            goToTemperatureMeasurementPage();
            return true;

        } else if (id==R.id.miWeather) {

            return true;


        } else if (id==R.id.miLogOut) {
            goToLogin();
            return true;

        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void goToExtraPage(){
        Intent intent = new Intent(this, ExtraPage.class);
        startActivity(intent);
    }

    private void goToLogin(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void goToTemperatureMeasurementPage(){
        Intent intent = new Intent(this, TemperatureMeasurementPage.class);
        startActivity(intent);
    }
    private void addProfile(){
        NewProfileFragment newProfile = new NewProfileFragment();
        newProfile.show(getFragmentManager(), "InsertProfile");
    }

    private void loadLastTemperature() {
        Cursor cursor = dbHelper.getLastTemperature();
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String lastTemp = cursor.getString(cursor.getColumnIndex("temperature_value"));
            @SuppressLint("Range") String lastTime = cursor.getString(cursor.getColumnIndex("measurement_time"));
            textViewLastTemperature.setText("Last Temperature: " + lastTemp + " °C\n" + lastTime);
            cursor.close();
        } else {
            textViewLastTemperature.setText("No temperature measurement available");
        }
    }

    private void setupSpinners() {
        // Populate the month spinner with values "01" to "12"
        final String[] monthNames = new String[] {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthNames);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        // Populate the day spinner with values "01" to "31"
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) {
            days[i] = String.format("%02d", i + 1);
        }
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(dayAdapter);

        // Set listeners to update the list when a new month or day is selected
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTemperatureHistory();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        spinnerDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTemperatureHistory();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void updateTemperatureHistory() {
        // Construct the date filter in "MM-dd" format
        String selectedMonthName = spinnerMonth.getSelectedItem().toString();

        String[] monthNames = new String[] {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        int monthNumber = 0;
        for (int i = 0; i < monthNames.length; i++) {
            if (monthNames[i].equals(selectedMonthName)) {
                monthNumber = i + 1;
                break;
            }
        }
        String monthNumberStr = String.format("%02d", monthNumber);

        String selectedDay = spinnerDay.getSelectedItem().toString();
        String monthDay = monthNumberStr + "-" + selectedDay; // e.g., "03-18"

        Cursor cursor = dbHelper.getMeasurementsByDate(monthDay);
        ArrayList<String> measurements = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("measurement_time"));
                @SuppressLint("Range") String temp = cursor.getString(cursor.getColumnIndex("temperature_value"));
                measurements.add("Time: " + time + "\nTemperature: " + temp + " °C");
            } while (cursor.moveToNext());
            cursor.close();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, measurements);
        listViewTemperatureHistory.setAdapter(adapter);
    }

}
