package com.example.coen390_feverapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SymptomLogActivity extends AppCompatActivity {

    protected TextView selectTextView;
    protected CheckBox chillsCheckBox, soreThroatCheckBox, headacheCheckBox, achesCheckBox,
                        nauseaCheckBox, runnyNoseCheckBox, coughCheckBox, fatigueCheckBox;
    protected Button submitButton, newSymptomButton;

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
        headacheCheckBox = findViewById(R.id.achesCheckBox);
        achesCheckBox = findViewById(R.id.achesCheckBox);
        nauseaCheckBox = findViewById(R.id.nauseaCheckBox);
        runnyNoseCheckBox = findViewById(R.id.runnyNoseCheckBox);
        coughCheckBox = findViewById(R.id.coughCheckBox);
        fatigueCheckBox = findViewById(R.id.fatigueCheckBox);

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
            }
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