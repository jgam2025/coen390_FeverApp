package com.example.coen390_feverapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;

public class MedicationActivity extends AppCompatActivity {
    private EditText etMedicationName, etMedicationDose;
    private Button btnSaveMedication;
    private ListView listViewMedication;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide the title
        }

        dbHelper = new DBHelper(this);

        etMedicationName = findViewById(R.id.et_medication_name);
        etMedicationDose = findViewById(R.id.et_medication_dose);
        btnSaveMedication = findViewById(R.id.btn_save_medication);
        listViewMedication = findViewById(R.id.list_medication);

        btnSaveMedication.setOnClickListener(v -> {
            String name = etMedicationName.getText().toString().trim();
            String dose = etMedicationDose.getText().toString().trim();

            if (!name.isEmpty()) {
                // Retrieve current profile from SharedPreferences
                SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                String currentProfile = sharedPrefs.getString("current_profile", "default");

                // Insert medication with the profile name
                boolean inserted = dbHelper.insertMedication(currentProfile, name, dose);
                if(inserted){
                    Toast.makeText(this, "Medication saved!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error saving medication", Toast.LENGTH_SHORT).show();
                }
                etMedicationName.setText("");
                etMedicationDose.setText("");
                loadMedicationHistory();
            } else {
                Toast.makeText(this, "Please enter a medication name", Toast.LENGTH_SHORT).show();
            }
        });

        loadMedicationHistory();
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

        } else if(id ==R.id.miLogOut) {
            goToLoginPage();
            return true;
        }else if(id ==R.id.miSymptoms) {
            goSymptomPage();
            return true;
        }
        else if (id==R.id.miGraph){
            Graph();
            return true;}
        else if(id ==R.id.miTemperature) {
            goToTemperatureMeasurementPage();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }


    private void loadMedicationHistory() {
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentProfile = sharedPrefs.getString("current_profile", "default");

        Cursor cursor = dbHelper.getMedicationHistoryByProfile(currentProfile);
        String[] fromColumns = {"name", "dose", "timestamp"};
        int[] toViews = {R.id.med_name, R.id.med_dose, R.id.med_timestamp};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this, R.layout.item_medication, cursor, fromColumns, toViews, 0);

        listViewMedication.setAdapter(adapter);

        listViewMedication.setOnItemClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmation")
                    .setMessage("Do you want to delete this medication?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (dbHelper.deleteMedication(id)) {
                            Toast.makeText(MedicationActivity.this, "Medication deleted!", Toast.LENGTH_SHORT).show();
                            loadMedicationHistory();
                        } else {
                            Toast.makeText(MedicationActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });
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

    private void goToTemperatureStoragePage(){
        Intent intent = new Intent(this, TemperatureStoragePage.class);
        startActivity(intent);
    }

    private void Graph(){
        GraphFragment graphDialog = new GraphFragment();
        graphDialog.show(getSupportFragmentManager(), "GraphDialog");
    }

    private void goSymptomPage(){
        Intent intent = new Intent(this, SymptomLogActivity.class);
        startActivity(intent);
    }





}
