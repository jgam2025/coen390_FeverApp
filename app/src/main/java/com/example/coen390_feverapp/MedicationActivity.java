package com.example.coen390_feverapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
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

public class MedicationActivity extends AppCompatActivity {

    private EditText medsEditText, doseEditText;
    private Button submitMedButton;
    private Spinner medsSpinner, profilesSpinner;
    private DBHelper dbHelper;
    private String medicationNameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication);

        dbHelper = new DBHelper(this);

        medsEditText = findViewById(R.id.medsEditText);
        doseEditText = findViewById(R.id.doseEditText);

        medsSpinner = findViewById(R.id.medsSpinner);
        showMedicationsOnSpinner();

        profilesSpinner = findViewById(R.id.profilesSpinner);
        showProfilesOnSpinner();

        submitMedButton = findViewById(R.id.submitMedButton);
        submitMedButton.setOnClickListener(v -> {

            String medicationNameSpinner = (String) medsSpinner.getSelectedItem();
            medicationNameText = medsEditText.getText().toString().trim();
            String medicationDose = doseEditText.getText().toString().trim();

            if ((!medicationNameText.isEmpty() && medicationNameSpinner == null) ||
                    (medicationNameSpinner != null && !medicationNameSpinner.isEmpty() && medicationNameText.isEmpty())) {

                SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                String currentProfile = sharedPrefs.getString("current_profile", "default");

                if(!medicationNameText.isEmpty()) { // selected medication from edit text
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
                } else if (medicationNameSpinner!=null && !medicationNameSpinner.isEmpty()){ // selected medication from spinner
                    boolean inserted = dbHelper.insertMedication(currentProfile, medicationNameSpinner, medicationDose);
                    if (inserted) {
                        Toast.makeText(this, "Medication saved!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error saving medication", Toast.LENGTH_SHORT).show();
                    }
                    medsEditText.setText("");
                    doseEditText.setText("");
                }
            } else {
                Toast.makeText(this, "Please enter a medication name", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void showProfilesOnSpinner(){
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUser = sharedPrefs.getString("current_user",null);
        List<String> profileList = dbHelper.getProfiles(currentUser);
        if(profileList.isEmpty()){
            //todo: message saying its empty
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,profileList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profilesSpinner.setAdapter(adapter);

        profilesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

    public void showMedicationsOnSpinner(){
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUser = sharedPrefs.getString("current_user",null);
        int userID = dbHelper.getUserID(currentUser);
        List<String> medicationList = dbHelper.getUserAddedMedications(userID);
        if(medicationList.isEmpty()){
            //todo: say that no new medications have been saved
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,medicationList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        medsSpinner.setAdapter(adapter);

        medsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    public String getMedicationNameText() {
        return medicationNameText;
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
     */



    //toolbar menu items

    /*
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



     */



}
