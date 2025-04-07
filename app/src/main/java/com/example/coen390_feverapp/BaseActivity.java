package com.example.coen390_feverapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    protected TextView homeTextView, dateTextView, qTextView;
    protected Button tempButton, histButton, medButton, symptomButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_base);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide the title
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupUI();
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

        }

        else if (id == R.id.miMore) {
            GoToExtra();
            return true;


        } else{
            return super.onOptionsItemSelected(item);

        }
    }

    private void GoToExtra(){
        Intent intent = new Intent(this, ExtraPageActivity.class);
        startActivity(intent);
    }



    private void goToHealth(){
        Intent intent = new Intent(this, HealthDataActivity.class);
        startActivity(intent);
    }

    void setupUI(){
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", 0);
        String currentUser = sharedPrefs.getString("current_user",null);
        homeTextView = findViewById(R.id.homeTextView);
        homeTextView.setText("Welcome, " + currentUser + "!");



        qTextView = findViewById(R.id.qTextView);

        tempButton = findViewById(R.id.tempButton);
        tempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToScanMeasurement();
            }
        });



        medButton = findViewById(R.id.medButton);
        medButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMedicationLog();
            }
        });

        symptomButton = findViewById(R.id.symptomButton);
        symptomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSymptomLog();
            }
        });
    }

    void goToMedicationLog(){
        Intent intent = new Intent(this, MedicationActivity.class);
        startActivity(intent);
    }

    void goToScanMeasurement(){
        Intent intent = new Intent(this, ScanMeasurementActivity.class);
        startActivity(intent);
    }



    void goToSymptomLog(){
        Intent intent = new Intent(this, SymptomLogActivity.class);
        startActivity(intent);
    }
}