package com.example.coen390_feverapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;

import java.util.List;
import java.util.Objects;

public class MedicationActivity extends AppCompatActivity {

    private EditText medsEditText, doseEditText;
    private Button submitMedButton;
    private Spinner medsSpinner, profilesSpinner;
    private DBHelper dbHelper;
    private String medicationNameText, selectedProfile, medicationNameSpinner;

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

        medsEditText = findViewById(R.id.medsEditText);
        doseEditText = findViewById(R.id.doseEditText);

        medsSpinner = findViewById(R.id.medsSpinner);
        showMedicationsOnSpinner();

        profilesSpinner = findViewById(R.id.profilesSpinner);
        showProfilesOnSpinner();

        submitMedButton = findViewById(R.id.submitMedButton);
        submitMedButton.setOnClickListener(v -> {

            medicationNameSpinner = (String) medsSpinner.getSelectedItem();
            medicationNameText = medsEditText.getText().toString().trim();
            String medicationDose = doseEditText.getText().toString().trim();

            if(validateInput(selectedProfile)){
                submitMeds(selectedProfile,medicationDose);
            }

        });

    }


    //validate input --> if validated, submit meds
    private boolean validateInput(String selectedProfile){

        Log.d("edit_text_check", "medication from edit text: " + medicationNameText);
        Log.d("spinner_check", "medication from spinner: " + medicationNameSpinner);
        Log.d("profile_check", "profile from spinner: " + selectedProfile);

        if (!medicationNameText.isEmpty() && medicationNameSpinner != "") {
            Toast.makeText(this,
                    "Please select from the dropdown menu OR enter a medication in the text field",
                    Toast.LENGTH_LONG).show();
        } else if (medicationNameSpinner == "" && medicationNameText.isEmpty()) {
            Toast.makeText(this, "Please select a medication", Toast.LENGTH_LONG).show();
        }

        if (selectedProfile == "Select profile"){
            Toast.makeText(this,"Please select a profile", Toast.LENGTH_LONG).show();
        }

        return true;
    }

    private void submitMeds(String selectedProfile, String medicationDose){
        if (!medicationNameText.isEmpty() ^ medicationNameSpinner != "") {
            Log.d("progress_check", "if condition reached");
            SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String currentProfile = sharedPrefs.getString("current_profile", "default");

            if (!medicationNameText.isEmpty() && selectedProfile != "Select profile") { // selected medication from edit text
                Log.d("edit_text_check", "Edit text not empty");
                SaveNewMedsFragment newMedsFragment = new SaveNewMedsFragment();
                newMedsFragment.show(getFragmentManager(), "NewMedication");
                boolean inserted = dbHelper.insertMedication(currentProfile, medicationNameText, medicationDose);
                if (inserted) {
                    Toast.makeText(this, "Medication saved!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error saving medication", Toast.LENGTH_SHORT).show();
                }
                medsEditText.setText("");
                doseEditText.setText("");
            } else if (medicationNameSpinner != ""
                    && selectedProfile != "Select profile") {
                Log.d("spinner_check", "spinner item selected: " + selectedProfile);// selected medication from spinner
                boolean inserted = dbHelper.insertMedication(currentProfile, medicationNameSpinner, medicationDose);
                if (inserted) {
                    Toast.makeText(this, "Medication saved!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error saving medication", Toast.LENGTH_SHORT).show();
                }
                medsEditText.setText("");
                doseEditText.setText("");
            }
        };
    }

    private void showProfilesOnSpinner() {
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUser = sharedPrefs.getString("current_user", null);
        List<String> profileList = dbHelper.getProfiles(currentUser);
        if (profileList.isEmpty()) {
            //do nothing...
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, profileList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profilesSpinner.setAdapter(adapter);

        profilesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedProfile = (String) parent.getItemAtPosition(position);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString("current_profile", selectedProfile);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    public void showMedicationsOnSpinner() {
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUser = sharedPrefs.getString("current_user", null);
        int userID = dbHelper.getUserID(currentUser);
        List<String> medicationList = dbHelper.getUserAddedMedications(userID);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, medicationList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        medsSpinner.setAdapter(adapter);

        medsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public String getMedicationNameText() {
        return medicationNameText;
    }


    //menu
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


