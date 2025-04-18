package com.example.coen390_feverapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupUI();

        if (getIntent().hasExtra("measurement")) {
            double measurement = getIntent().getDoubleExtra("measurement", 0.0);
            displayFeverAlert(measurement);
        }
    }

    //function to display fever alerts to users
   private void displayFeverAlert(double celsius) {
        String category;
        int backgroundColor;
        if (celsius < 37.5) {
            category = "No Fever";
            backgroundColor = Color.GRAY;
        } else if (celsius < 38.0) {
            category = "Mild Fever";
            backgroundColor = Color.YELLOW;
        } else if (celsius < 39.0) {
            category = "Fever";
            backgroundColor = Color.parseColor("#FFA500"); // Orange
        } else {
            category = "High Fever";
            backgroundColor = Color.RED;
        }

        //sends alert
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Fever Alert");
        builder.setMessage("Your temperature indicates: " + category);
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(backgroundColor));
        });
        dialog.show();
    }

    //toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    //giving functionality to toolbar icons
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

    //go to extra page
    private void GoToExtra(){
        Intent intent = new Intent(this, ExtraPageActivity.class);
        startActivity(intent);
    }

    //go to health history page
    private void goToHealth(){
        Intent intent = new Intent(this, HealthDataActivity.class);
        startActivity(intent);
    }

    void setupUI(){
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", 0);
        String currentUser = sharedPrefs.getString("current_user",null);
        homeTextView = findViewById(R.id.homeTextView);
        homeTextView.setText("Welcome, " + currentUser + "!");
        tempButton = findViewById(R.id.tempButton);
        //temp button redirects users to scan measurement page
        tempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToScanMeasurement();
            }
        });

        medButton = findViewById(R.id.medButton);
        //med button redirects user to medication page
        medButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMedicationLog();
            }
        });

        symptomButton = findViewById(R.id.symptomButton);
        //symptom button redirects user to symptom page
        symptomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSymptomLog();
            }
        });
    }

    //go to medication page
    void goToMedicationLog(){
        Intent intent = new Intent(this, MedicationActivity.class);
        startActivity(intent);
    }

    //go to scan measurement page
    void goToScanMeasurement(){
        Intent intent = new Intent(this, ScanMeasurementActivity.class);
        startActivity(intent);
    }

    //go to symptom page
    void goToSymptomLog(){
        Intent intent = new Intent(this, SymptomLogActivity.class);
        startActivity(intent);
    }
}