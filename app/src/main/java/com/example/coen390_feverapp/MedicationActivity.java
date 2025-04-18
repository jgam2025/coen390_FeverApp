package com.example.coen390_feverapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        dbHelper = new DBHelper(this);

        medsEditText = findViewById(R.id.medsEditText);
        doseEditText = findViewById(R.id.doseEditText);

        medsSpinner = findViewById(R.id.medsSpinner);
        showMedicationsOnSpinner();
        setUpToolbar();
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
     //back button
    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    //validate input --> if validated, submit meds
    private boolean validateInput(String selectedProfile){

        Log.d("edit text check", "medication from edit text: " + medicationNameText);
        Log.d("spinner check", "medication from spinner: " + medicationNameSpinner);
        Log.d("profile check", "profile from spinner: " + selectedProfile);

        if (!medicationNameText.isEmpty() && medicationNameSpinner != "") {
            Toast.makeText(this,
                    "select a medication  or enter a medication ",
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
        String logTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        if (!medicationNameText.isEmpty() ^ medicationNameSpinner != "") {
            Log.d("progress_check", "if condition reached");
            SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String currentProfile = sharedPrefs.getString("current_profile", "default");

            if (!medicationNameText.isEmpty() && selectedProfile != "Select profile") { // selected medication from edit text
                Log.d("edit_text_check", "Edit text not empty");
                SaveNewMedsFragment newMedsFragment = new SaveNewMedsFragment();
                newMedsFragment.show(getFragmentManager(), "NewMedication");

                boolean inserted = dbHelper.insertMedication(currentProfile, medicationNameText, medicationDose, logTime);
                if (inserted) {
                    Toast.makeText(this, "Medication saved!", Toast.LENGTH_SHORT).show();
                    Log.d("med_check", "Medication inserted: " + currentProfile + ", " + medicationNameText + ", " + medicationDose);

                } else {
                    Toast.makeText(this, "Error saving medication", Toast.LENGTH_SHORT).show();
                }
                medsEditText.setText("");
                doseEditText.setText("");
            } else if (medicationNameSpinner != ""
                    && selectedProfile != "Select profile") {
                Log.d("spinner_check", "spinner item selected: " + selectedProfile);// selected medication from spinner
                boolean inserted = dbHelper.insertMedication(currentProfile, medicationNameSpinner, medicationDose, logTime);
                if (inserted) {
                    Toast.makeText(this, "Medication saved!", Toast.LENGTH_SHORT).show();
                    Log.d("med_check", "Medication inserted: " + currentProfile + ", " + medicationNameSpinner + ", " + medicationDose);
                } else {
                    Toast.makeText(this, "Error saving medication", Toast.LENGTH_SHORT).show();
                }
                // Clear fields
                medsEditText.setText("");
                doseEditText.setText("");
            }
        };
    }

    // profiles spinner based on current user
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
 //show  medications spinner with previously added medications
    public void showMedicationsOnSpinner() {
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUser = sharedPrefs.getString("current_user", null);
        int userID = dbHelper.getUserID(currentUser);
        List<String> medicationList = dbHelper.getUserAddedMedicationsList(userID);

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
    //toolbar menu item selection
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

    //go to health history page
    private void goToHealth() {
        Intent intent = new Intent(this, HealthDataActivity.class);
        startActivity(intent);
    }

    //go to home page
    private void goToHome() {
        Intent intent = new Intent(this, BaseActivity.class);
        startActivity(intent);
    }

    //go to extra page
    private void goToExtra() {
        Intent intent = new Intent(this, ExtraPageActivity.class);
        startActivity(intent);
    }
}


