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
    private String medicationNameText, selectedProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication);
        Toolbar toolbar = findViewById(R.id.medsToolbar);
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
            //todo: create separate function for this code to go into because its a lot for oncreate
            String medicationNameSpinner = (String) medsSpinner.getSelectedItem();
            medicationNameText = medsEditText.getText().toString().trim();
            String medicationDose = doseEditText.getText().toString().trim();

            Log.d("edit_text_check", "medication from edit text: " + medicationNameText);
            Log.d("spinner_check", "medication from spinner: " + medicationNameSpinner);

            Log.d("profile_check", "profile from spinner: " + selectedProfile);
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
                } else if (medicationNameSpinner != null && !medicationNameSpinner.isEmpty()
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
                //all the possible things a user could mess up:
            } else if (!medicationNameText.isEmpty() && medicationNameSpinner != "") {
                Toast.makeText(this,
                        "Please select from the dropdown menu OR enter a medication in the text field",
                        Toast.LENGTH_LONG).show();
            } else if (medicationNameSpinner == "" && medicationNameText.isEmpty()) {
                Toast.makeText(this, "Please select a medication", Toast.LENGTH_LONG).show();
            }

            if (selectedProfile == "Select profile"){
                Toast.makeText(this,"Please select a profile", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void showProfilesOnSpinner() {
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUser = sharedPrefs.getString("current_user", null);
        List<String> profileList = dbHelper.getProfiles(currentUser);
        if (profileList.isEmpty()) {

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
        if (medicationList.isEmpty()) {
            //todo: say that no new medications have been saved
        }
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
    /*
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

}


     */