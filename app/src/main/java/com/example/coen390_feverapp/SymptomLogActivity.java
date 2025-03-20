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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SymptomLogActivity extends AppCompatActivity {

    protected TextView selectTextView;
    protected CheckBox chillsCheckBox, soreThroatCheckBox, headacheCheckBox, achesCheckBox,
                        nauseaCheckBox, runnyNoseCheckBox, coughCheckBox, fatigueCheckBox;
    protected Button submitButton, newSymptomButton;
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

        setupUI();

    }

    private void setupUI(){

        selectTextView = findViewById(R.id.selectTextView);

        chillsCheckBox = findViewById(R.id.chillsCheckBox);
        soreThroatCheckBox = findViewById(R.id.soreThroatCheckBox);
        headacheCheckBox = findViewById(R.id.headacheCheckBox);
        achesCheckBox = findViewById(R.id.achesCheckBox);
        nauseaCheckBox = findViewById(R.id.nauseaCheckBox);
        runnyNoseCheckBox = findViewById(R.id.runnyNoseCheckBox);
        coughCheckBox = findViewById(R.id.coughCheckBox);
        fatigueCheckBox = findViewById(R.id.fatigueCheckBox);

        profileOptionSpinner = findViewById(R.id.profileOptionSpinner);
        showUsersOnSpinner();

        submitButton = findViewById(R.id.submitButton);
        newSymptomButton = findViewById(R.id.newSymptomButton);

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

                if(chillsCheckBox.isChecked()) symptoms = symptoms + "Chills,";
                if(soreThroatCheckBox.isChecked()) symptoms = symptoms + "Sore Throat,";
                if(headacheCheckBox.isChecked()) symptoms = symptoms + "Headache,";
                if(achesCheckBox.isChecked()) symptoms = symptoms + "Muscle Aches,";
                if(nauseaCheckBox.isChecked()) symptoms = symptoms + "Nausea,";
                if(runnyNoseCheckBox.isChecked()) symptoms = symptoms + "Runny Nose,";
                if(coughCheckBox.isChecked()) symptoms = symptoms + "Cough,";
                if(fatigueCheckBox.isChecked()) symptoms = symptoms + "Fatigue,";
                //Toast.makeText(getApplicationContext(),symptoms,Toast.LENGTH_LONG).show();
                //add the user added symptoms too
                SharedPreferences sharedPrefs = getSharedPreferences("user_prefs",MODE_PRIVATE);
                String currentProfile = sharedPrefs.getString("current_profile",null);
                //Toast.makeText(getApplicationContext(),currentProfile,Toast.LENGTH_LONG).show();
                String logTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                dbHelper = new DBHelper(getApplicationContext());
                dbHelper.insertSymptoms(currentProfile,symptoms,logTime);
            }
        });
    }

    public void showUsersOnSpinner(){
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUser = sharedPrefs.getString("current_user",null);
        dbHelper = new DBHelper(getApplicationContext());
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
        else if (id==R.id.miSymptoms){
            goToSymptomLogActivity();
            return true;
        }
        else if (id==R.id.miTemperature) {
            goToTemperatureMeasurementPage();
            return true;
        }
        else if (id==R.id.miLogOut) {
            goToLogin();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void goToExtraPage(){
        Intent intent = new Intent(this, ExtraPage.class);
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

}