package com.example.coen390_feverapp;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Calendar;
import java.util.List;

public class TemperatureStoragePage extends AppCompatActivity {

    private TextView textViewLastTemperature;
    private Spinner spinnerMonth, spinnerDay,spinnerYear;
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.checkBoxLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textViewLastTemperature = findViewById(R.id.textViewLastTemperature);
        spinnerYear = findViewById(R.id.spinnerYear);
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

    @SuppressLint("ResourceAsColor")
    private void loadLastTemperature() {
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentProfile = sharedPrefs.getString("current_profile", "default");
        Cursor cursor = dbHelper.getLastTemperatureByProfile(currentProfile);

        TextView lastTempTv = findViewById(R.id.textViewLastTemperature);
        TextView alertTv = findViewById(R.id.textView5);

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String saved = cursor.getString(cursor.getColumnIndex("temperature_value"));
            @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("measurement_time"));
            lastTempTv.setText("Last Temperature: " + saved + "\n" + "    " + time);

            // Parse value + unit
            String[] parts = saved.split(" ");
            double value = Double.parseDouble(parts[0]);
            boolean isF = saved.contains("°F");

            // Convert to °C if needed
            double celsius = isF ? (value - 32) * 5/9 : value;

            // Determine category + colour
            String category;
            int colorRes;
            if (celsius < 37.5) {
                category = " No Fever ";
                colorRes = android.R.color.transparent;
            } else if (celsius < 38.0) {
                category = " Mild Fever ";
                colorRes = getResources().getColor(R.color.mild_fever_bg);
            } else if (celsius < 39.0) {
                category = " Fever ";
                colorRes = getResources().getColor(R.color.fever_bg);
            } else {
                category = " High Fever ";
                colorRes = getResources().getColor(R.color.high_fever_bg);
            }

            alertTv.setText(category);
            alertTv.setBackgroundColor(colorRes);
            cursor.close();
        } else {
            textViewLastTemperature.setText("Last Temperature: None");
        }
    }

    private void setupSpinners() {

        // Populate year spinner (for example, from current year back to 5 years ago)
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int y = currentYear; y >= currentYear - 10; y--) {
            years.add(String.valueOf(y));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(yearAdapter);
        spinnerYear.setSelection(0);
        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTemperatureHistory();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        // Use month names for the month spinner
        final String[] monthNames = new String[] {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, monthNames);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);

        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        spinnerMonth.setSelection(currentMonth);

        // Populate the day spinner with values "01" to "31"
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) {
            days[i] = String.format("%02d", i + 1);
        }
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(dayAdapter);

        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        spinnerDay.setSelection(currentDay - 1);

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
        // Get current profile from SharedPreferences
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentProfile = sharedPrefs.getString("current_profile", "default");

        String selectedYear = spinnerYear.getSelectedItem().toString();

        // Convert selected month name to a two-digit month number
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

        // Query the measurements by date and current profile
        Cursor cursor = dbHelper.getMeasurementsByFullDateAndProfile(selectedYear,monthDay, currentProfile);
        ArrayList<String> measurements = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("measurement_time"));
                @SuppressLint("Range") String temp = cursor.getString(cursor.getColumnIndex("temperature_value"));
                measurements.add("Date & Time: " + time + "\nTemperature: " + temp);

            } while (cursor.moveToNext());
            cursor.close();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, measurements);
        listViewTemperatureHistory.setAdapter(adapter);
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
        }else if(id == R.id.miMedication){
            goToMedicationPage();
            return true;
        }
        else if (id == R.id.miTemperature) {
            goToTemperatureMeasurementPage();
            return true;
        }
        else if (id==R.id.miSymptoms){
            goToSymptomLogActivity();
            return true;
        }
        else if (id==R.id.miGraph) {
            Graph();
            return true;
        }
        else if (id==R.id.miLogOut) {
            goToLoginPage();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void goToExtraPage(){
        Intent intent = new Intent(this, ExtraPageActivity.class);
        startActivity(intent);
    }

    private void goToLoginPage(){
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

    private void goToMedicationPage(){
        Intent intent = new Intent(this, MedicationActivity.class);
        startActivity(intent);
    }

    private void goToSymptomLogActivity(){
        Intent intent = new Intent(this,SymptomLogActivity.class);
        startActivity(intent);
    }

    private void Graph(){
        GraphFragment graphDialog = new GraphFragment();
        graphDialog.show(getSupportFragmentManager(), "GraphDialog");
    }

}
