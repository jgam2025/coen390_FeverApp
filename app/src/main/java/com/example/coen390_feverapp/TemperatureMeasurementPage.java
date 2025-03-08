package com.example.coen390_feverapp;
import android.view.Menu;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupUI();
        dbHelper = new DBHelper(this);
        showUsersOnSpinner();
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
            goToTemperatureStoragePage();
            return true;
        } else if (id == R.id.miMore) {
            goToPlusPage();
            return true;

        } else if(id ==R.id.miLogOut) {
            goToLoginPage();
            return true;
        }else if(id ==R.id.miWeather) {
            goToWeatherPage();
            return true;

        }else {
            return super.onOptionsItemSelected(item);
        }
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

    private void goToScanMeasurementPage(){
        Intent intent = new Intent(this, ScanMeasurementActivity.class);
        startActivity(intent);
    }
    private void goToTemperatureStoragePage(){
        Intent intent = new Intent(this, TemperatureStoragePage.class);
        startActivity(intent);
    }

    private void goToPlusPage(){
        Intent intent = new Intent(this, PlusPage.class);
        startActivity(intent);
    }

    private void goToLoginPage(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void goToWeatherPage(){
        Intent intent = new Intent(this, WeatherPage.class);
        startActivity(intent);
    }

    private void showUsersOnSpinner(){
        List<String> userList = dbHelper.getAllUsers();
        if(userList.isEmpty()){

        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,userList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userSpinner.setAdapter(adapter);

    }

}