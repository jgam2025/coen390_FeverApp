package com.example.coen390_feverapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SymptomLogActivity extends AppCompatActivity  {

    protected TextView selectTextView;
    protected CheckBox chillsCheckBox, soreThroatCheckBox, headacheCheckBox, achesCheckBox,
                        nauseaCheckBox, runnyNoseCheckBox, coughCheckBox, fatigueCheckBox;
    protected Button submitButton, newSymptomButton, goToLogButton;
    protected Spinner profileOptionSpinner;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_symptom_log);
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

        dbHelper = new DBHelper(this);
        setupUI();

    }

    private void setupUI(){

        selectTextView = findViewById(R.id.selectTextView);

        List<CheckBox> staticCheckBoxes = new ArrayList<>();
        chillsCheckBox = findViewById(R.id.chillsCheckBox);
        staticCheckBoxes.add(chillsCheckBox);
        soreThroatCheckBox = findViewById(R.id.soreThroatCheckBox);
        staticCheckBoxes.add(soreThroatCheckBox);
        headacheCheckBox = findViewById(R.id.headacheCheckBox);
        staticCheckBoxes.add(headacheCheckBox);
        achesCheckBox = findViewById(R.id.achesCheckBox);
        staticCheckBoxes.add(achesCheckBox);
        nauseaCheckBox = findViewById(R.id.nauseaCheckBox);
        staticCheckBoxes.add(nauseaCheckBox);
        runnyNoseCheckBox = findViewById(R.id.runnyNoseCheckBox);
        staticCheckBoxes.add(runnyNoseCheckBox);
        coughCheckBox = findViewById(R.id.coughCheckBox);
        staticCheckBoxes.add(coughCheckBox);
        fatigueCheckBox = findViewById(R.id.fatigueCheckBox);
        staticCheckBoxes.add(fatigueCheckBox);

        profileOptionSpinner = findViewById(R.id.profileOptionSpinner);
        showUsersOnSpinner();

        submitButton = findViewById(R.id.submitButton);
        newSymptomButton = findViewById(R.id.newSymptomButton);
        goToLogButton = findViewById(R.id.goToLogButton);

        newSymptomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open new symptom fragment!
                NewSymptomFragment newSymptom = new NewSymptomFragment();
                newSymptom.show(getSupportFragmentManager(), "NewSymptom");
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //submit to database
                String symptoms= "";

                for(CheckBox checkBox: staticCheckBoxes){
                    if(checkBox.isChecked()){
                        symptoms = symptoms + checkBox.getText().toString() + ",";
                    }
                }

                // TODO: add the user-added symptoms to the string as well
                // access checkbox list from fragment in here
                // initialize checkboxes from db in here if theyve been created

                System.out.println(symptoms);
                SharedPreferences sharedPrefs = getSharedPreferences("user_prefs",MODE_PRIVATE);
                String currentProfile = sharedPrefs.getString("current_profile",null);
                System.out.println(currentProfile);
                String logTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                System.out.println(logTime);

                boolean inserted = dbHelper.insertSymptoms(currentProfile, symptoms, logTime);

                if (inserted) {
                    Toast.makeText(getApplicationContext(), "Symptoms saved", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error saving symptoms", Toast.LENGTH_SHORT).show();
                }

                for(CheckBox checkBox : staticCheckBoxes){
                    if (checkBox.isChecked()){
                        checkBox.setChecked(false);
                    }
                }

            }
        });

        goToLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSymptomStoragePage();
            }
        });

        loadUserAddedSymptoms();

    }

    private void loadUserAddedSymptoms(){
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUser = sharedPrefs.getString("current_user", null);
        int userID = dbHelper.getUserID(currentUser);

        List<String> userAddedSymptomsList = dbHelper.getUserAddedSymptoms(userID);
        for (String symptom : userAddedSymptomsList) {
            CheckBox symptomCheckBox = new CheckBox(this);
            symptomCheckBox.setText(symptom);
            LinearLayout container = findViewById(R.id.linearCheckBoxLayout);
            container.addView(symptomCheckBox);
            ScrollView scrollView = findViewById(R.id.checkboxScroll);
            scrollView.requestLayout();
            scrollView.fullScroll(View.FOCUS_DOWN);
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
        profileOptionSpinner.setAdapter(adapter);

        profileOptionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        } else if (id == R.id.miperson) {
            goToTemperatureStorage();
            return true;

        } else if (id == R.id.miadd) {
            addProfile();
            return true;

        }
        else if (id == R.id.miMedication) {
            goToMedication();
            return true;
        }

        else if (id==R.id.miSymptoms){
            goToSymptomLogActivity();
            return true;
        }
        else if (id==R.id.miTemperature) {
            goToTemperatureMeasurementPage();
            return true;
        }
        else if (id==R.id.miGraph) {
            Graph();
            return true;
        }
        else if (id==R.id.miLogOut) {
            goToLogin();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void goToSymptomStoragePage(){
        Intent intent = new Intent(this, SymptomStoragePage.class);
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
    private void goToTemperatureStorage(){
        Intent intent = new Intent(this, TemperatureStoragePage.class);
        startActivity(intent);
    }
    private void goToMedication(){
        Intent intent = new Intent(this, MedicationActivity.class);
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

    private void Graph(){
        GraphFragment graphDialog = new GraphFragment();
        graphDialog.show(getSupportFragmentManager(), "GraphDialog");
    }

}