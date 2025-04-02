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
    String selectedProfile;
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

        profileOptionSpinner = findViewById(R.id.profileOptionSpinner);
        showProfilesOnSpinner();

        submitButton = findViewById(R.id.submitButton);
        newSymptomButton = findViewById(R.id.newSymptomButton);
        goToLogButton = findViewById(R.id.goToLogButton);

        List<CheckBox> checkBoxes = initializeCheckBoxes();

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

                LinearLayout container = findViewById(R.id.linearCheckBoxLayout);
                for (int i = 0; i < container.getChildCount(); i++) {
                    View child = container.getChildAt(i);
                    if (child instanceof CheckBox) {
                        CheckBox newSymptomCheckbox = (CheckBox) child;
                        if (newSymptomCheckbox.isChecked()) {
                            symptoms = symptoms + newSymptomCheckbox.getText().toString() + ", ";
                        }
                    }
                }

                if(!symptoms.isEmpty()){
                    symptoms = symptoms.substring(0,symptoms.length()-2); // removes last ", " from string for display
                }
                Log.d("symptom_check", "symptom string: " + symptoms);

                SharedPreferences sharedPrefs = getSharedPreferences("user_prefs",MODE_PRIVATE);
                String currentProfile = sharedPrefs.getString("current_profile",null);
                String currentUser = sharedPrefs.getString("current_user", null);
                Log.d("current_profile_check", "current profile: " + currentProfile);
                int userID = dbHelper.getUserID(currentUser);
                Log.d("current_user_check","current user: " + currentUser + " , id: " + userID);
                String logTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                Log.d("time_check", "time: " + logTime);

                if(symptoms.isEmpty()){
                    Toast.makeText(getApplicationContext(),"No symptoms selected", Toast.LENGTH_LONG).show();
                } else if (selectedProfile == "Select profile"){
                    Toast.makeText(getApplicationContext(), "Please select a profile", Toast.LENGTH_LONG).show();
                }
                else {

                    boolean inserted = dbHelper.insertSymptoms(currentProfile, symptoms, logTime);

                    if (inserted) {
                        Toast.makeText(getApplicationContext(), "Symptoms saved", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error saving symptoms", Toast.LENGTH_SHORT).show();
                    }

                    for (CheckBox checkBox : checkBoxes) {
                        if (checkBox.isChecked()) {
                            checkBox.setChecked(false);
                        }
                    }

                    for (int i = 0; i < container.getChildCount(); i++) {
                        View child = container.getChildAt(i);
                        if (child instanceof CheckBox) {
                            ((CheckBox) child).setChecked(false);
                        }
                    }
                }
            }
        });



    }

    private List<CheckBox> initializeCheckBoxes(){
        List<CheckBox> checkBoxes = new ArrayList<>();
        chillsCheckBox = findViewById(R.id.chillsCheckBox);
        checkBoxes.add(chillsCheckBox);
        soreThroatCheckBox = findViewById(R.id.soreThroatCheckBox);
        checkBoxes.add(soreThroatCheckBox);
        headacheCheckBox = findViewById(R.id.headacheCheckBox);
        checkBoxes.add(headacheCheckBox);
        achesCheckBox = findViewById(R.id.achesCheckBox);
        checkBoxes.add(achesCheckBox);
        nauseaCheckBox = findViewById(R.id.nauseaCheckBox);
        checkBoxes.add(nauseaCheckBox);
        runnyNoseCheckBox = findViewById(R.id.runnyNoseCheckBox);
        checkBoxes.add(runnyNoseCheckBox);
        coughCheckBox = findViewById(R.id.coughCheckBox);
        checkBoxes.add(coughCheckBox);
        fatigueCheckBox = findViewById(R.id.fatigueCheckBox);
        checkBoxes.add(fatigueCheckBox);

        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUser = sharedPrefs.getString("current_user", null);
        int userID = dbHelper.getUserID(currentUser);

        List<String> userAddedSymptoms = dbHelper.getUserAddedSymptoms(userID);

        for (String symptom : userAddedSymptoms) {
            CheckBox symptomCheckBox = new CheckBox(this);
            symptomCheckBox.setText(symptom);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            int marginL = 0;
            int marginT = 20;
            int marginR = 0;
            int marginB = 20;

            params.setMargins(marginL, marginT, marginR, marginB);

            symptomCheckBox.setLayoutParams(params);

            LinearLayout container = findViewById(R.id.linearCheckBoxLayout);
            container.addView(symptomCheckBox);
            ScrollView scrollView = findViewById(R.id.checkboxScroll);
            scrollView.requestLayout();
            scrollView.fullScroll(View.FOCUS_DOWN);
            checkBoxes.add(symptomCheckBox);
        }
        return checkBoxes;
    }

    public void showProfilesOnSpinner(){
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUser = sharedPrefs.getString("current_user",null);
        List<String> profileList = dbHelper.getProfiles(currentUser);
        if(profileList.isEmpty()){
            //do nothing
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,profileList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profileOptionSpinner.setAdapter(adapter);

        profileOptionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedProfile = (String) parent.getItemAtPosition(position);
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
            goToHealth();
            return true;


        } else {
            return super.onOptionsItemSelected(item);
        }
    }



    private void goToExtraPage(){
        Intent intent = new Intent(this, ExtraPageActivity.class);
        startActivity(intent);
    }

    private void goToHealth(){
        Intent intent = new Intent(this,HealthDataActivity.class);
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