package com.example.coen390_feverapp;
import android.content.SharedPreferences;
import android.view.Menu;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;


import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class TemperatureMeasurementPage extends AppCompatActivity {

    Button ScanButton;
    Spinner userSpinner;
    FloatingActionButton infoFAB;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        insertTestTemperatureData();
        setContentView(R.layout.activity_temperature_measurement_page);
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
        setupUI();
        dbHelper = new DBHelper(this);
        showUsersOnSpinner();
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
            goToTemperatureStoragePage();
            return true;
        } else if (id == R.id.miMore) {
            goToExtraPage();
            return true;
        } else if (id == R.id.miadd) {
            addProfile();
            return true;
        } else if(id == R.id.miMedication){
            goToMedicationPage();
            return true;
        } else if(id ==R.id.miLogOut) {
            goToLoginPage();
            return true;

        }else if (id==R.id.miSymptoms){
            goToSymptomLogActivity();
            return true;
        }
        else if(id ==R.id.miTemperature) {
            goToTemperatureStoragePage();
            return true;

        }else if (id==R.id.miGraph){
            Graph();
            return true;

        }else {
            return super.onOptionsItemSelected(item);
        }
    }


    void setupUI(){

        ScanButton = findViewById(R.id.ScanButton);
        ScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToScanMeasurementPage();
            }
        });

        infoFAB = findViewById(R.id.infoFAB);
        infoFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InsertInfoFragment infoDialog = new InsertInfoFragment();
                infoDialog.show(getSupportFragmentManager(),"InfoPage");
            }
        });

        userSpinner = findViewById(R.id.userSpinner);
    }

    private void goToScanMeasurementPage(){
        Intent intent = new Intent(this, ScanMeasurementActivity.class);
        startActivity(intent);
    }
    private void goToTemperatureStoragePage(){
        Intent intent = new Intent(this, TemperatureStoragePage.class);
        startActivity(intent);
    }

    private void addProfile(){
        NewProfileFragment newProfile = new NewProfileFragment();
        newProfile.show(getFragmentManager(), "InsertProfile");
    }

    private void Graph(){
        GraphFragment graphDialog = new GraphFragment();
        graphDialog.show(getSupportFragmentManager(), "GraphDialog");
    }

    private void insertTestTemperatureData() {
        DBHelper dbHelper = new DBHelper(this);

        // Insert 10 test values with different dates and times
        for (int i = 1; i <= 10; i++) {
            String profileName = "amira";  // Change if needed
            String measurementTime = "2025-03-1" + i + " 12:0" + i + ":00"; // YYYY-MM-DD HH:MM:SS
            String temperatureValue = String.valueOf(36.5 + (i * 0.1)); // Example: 36.5, 36.6, ...

            boolean result = dbHelper.insertTemperature(profileName, measurementTime, temperatureValue);
            if (result) {
                System.out.println("Inserted: " + measurementTime + " - " + temperatureValue);
            } else {
                System.out.println("Failed to insert data.");
            }
        }
    }


    private void goToLoginPage(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void goToMedicationPage(){
        Intent intent = new Intent(this, MedicationActivity.class);
        startActivity(intent);
    }


    private void goToExtraPage(){
        Intent intent = new Intent(this, ExtraPage.class);
        startActivity(intent);
    }

    private void goToSymptomLogActivity(){
        Intent intent = new Intent(this,SymptomLogActivity.class);
        startActivity(intent);
    }

    public void showUsersOnSpinner(){
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUser = sharedPrefs.getString("current_user",null);
        List<String> profileList = dbHelper.getProfiles(currentUser);
        if(profileList.isEmpty()){

        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,profileList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(adapter);

        // Check if a profile was previously selected and set it.
        String savedProfile = sharedPrefs.getString("current_profile", null);
        if (savedProfile != null) {
            int index = profileList.indexOf(savedProfile);
            if (index >= 0) {
                userSpinner.setSelection(index);
            }
        }

        // Save the selected profile into SharedPreferences when the user changes it.
        userSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedProfile = (String) parent.getItemAtPosition(position);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString("current_profile", selectedProfile);
                editor.apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

    }

}