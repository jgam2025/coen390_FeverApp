package com.example.coen390_feverapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
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

import java.util.List;

/*public class SymptomStoragePage extends AppCompatActivity {

    protected TextView textTextView;
    protected Spinner profileSelectSpinner;
    protected ListView symptomsListView;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_symptom_storage_page);
        Toolbar toolbar = findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide the title
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.checkBoxLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DBHelper(this);
        setupUI();
        getSymptomHistory();
    }

    private void setupUI(){

        profileSelectSpinner = findViewById(R.id.profileSelectSpinner);
        showUsersOnSpinner();

        symptomsListView = findViewById(R.id.symptomListView);
    }

    private void getSymptomHistory(){
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentProfile = sharedPrefs.getString("current_profile", "default");
        String currentUser = sharedPrefs.getString("current_user", null);
        int userID = dbHelper.getUserID(currentUser);

        List<String> symptomList = dbHelper.getSymptomHistory(currentProfile);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, symptomList);
        symptomsListView.setAdapter(adapter);

        if(symptomList.isEmpty()){

        }
    }

    public void showUsersOnSpinner(){
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUser = sharedPrefs.getString("current_user",null);
        List<String> profileList = dbHelper.getProfiles(currentUser);
        if(profileList.isEmpty()){

        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,profileList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profileSelectSpinner.setAdapter(adapter);

        profileSelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedProfile = (String) parent.getItemAtPosition(position);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString("current_profile", selectedProfile);
                editor.apply();
                getSymptomHistory();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

    }


    //menu functions
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
        }else if (id == R.id.miperson) {
            goToTemperatureStorage();
            return true;

        }else if(id == R.id.miMedication){
            goToMedicationPage();
            return true;
        }
        else if (id == R.id.miTemperature) {
            goToTemperatureMeasurementPage();
            return true;
        }
        else if (id == R.id.miGraph) {
            Graph();
            return true;
        }
        else if (id==R.id.miSymptoms){
            goToSymptomLogActivity();
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

    private void goToTemperatureStorage(){
        Intent intent = new Intent(this, TemperatureStoragePage.class);
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

}*/