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
        showProfilesOnSpinner();
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

    public void showProfilesOnSpinner(){
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



//menu setup functions
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu from the menu.xml file in the menu directory
        getMenuInflater().inflate(R.menu.toolbar, menu);

        return true;
    }

    private void goToScanMeasurementPage(){
        Intent intent = new Intent(this, ScanMeasurementActivity.class);
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



    private void goToLoginPage(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void goToMedicationPage(){
        Intent intent = new Intent(this, MedicationActivity.class);
        startActivity(intent);
    }


    private void goToExtraPage(){
        Intent intent = new Intent(this, ExtraPageActivity.class);
        startActivity(intent);
    }

    private void goToSymptomLogActivity(){
        Intent intent = new Intent(this,SymptomLogActivity.class);
        startActivity(intent);
    }

}