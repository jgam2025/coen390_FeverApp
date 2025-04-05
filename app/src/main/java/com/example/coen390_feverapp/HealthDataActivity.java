package com.example.coen390_feverapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
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

public class HealthDataActivity extends AppCompatActivity {

    private Spinner profilesOptionSpinner, weekOfSpinner;
    private ListView symptomsListView, medsListView;
    private Button trendButton;
    private DBHelper dbHelper;
    String selectedProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_health_data);
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

    private void setupUI(){
        dbHelper = new DBHelper(this);
        profilesOptionSpinner = findViewById(R.id.profilesOptionSpinner);
        showProfilesOnSpinner();

        weekOfSpinner = findViewById(R.id.weekOfSpinner);
        showDatesOnSpinner();

        trendButton = findViewById(R.id.trendButton);
        trendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GraphFragment graphDialog = new GraphFragment();
                graphDialog.show(getSupportFragmentManager(), "GraphDialog");
            }
        });

        symptomsListView = findViewById(R.id.symptomsListView);
        medsListView = findViewById(R.id.medsListView);
        loadMedicationHistory();
        loadSymptomHistory();
    }

    private void showDatesOnSpinner(){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.date_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weekOfSpinner.setAdapter(adapter);

        weekOfSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDate = (String) parent.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
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
        profilesOptionSpinner.setAdapter(adapter);

        profilesOptionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

    private void loadMedicationHistory() {
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentProfile = sharedPrefs.getString("current_profile", "default");

        Cursor cursor = dbHelper.getMedicationHistoryByProfile(currentProfile);
        String[] fromColumns = {"name", "dose", "timestamp"};
        int[] toViews = {R.id.med_name, R.id.med_dose, R.id.med_timestamp};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this, R.layout.item_medication, cursor, fromColumns, toViews, 0);

        medsListView.setAdapter(adapter);

        medsListView.setOnItemClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmation")
                    .setMessage("Do you want to delete this medication?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        if (dbHelper.deleteMedication(id)) {
                            Toast.makeText(HealthDataActivity.this, "Medication deleted!", Toast.LENGTH_SHORT).show();
                            loadMedicationHistory();
                        } else {
                            Toast.makeText(HealthDataActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }


    private void loadSymptomHistory(){
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentProfile = sharedPrefs.getString("current_profile", "default");

        List<String> symptomList = dbHelper.getSymptomHistory(currentProfile);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, symptomList);
        symptomsListView.setAdapter(adapter);

        if(symptomList.isEmpty()){
            //do nothing...
        }
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
        } else if (id == R.id.mihome) {
            goToHome();
            return true;


        } else {
            return super.onOptionsItemSelected(item);
        }
    }



    private void goToExtraPage(){
        Intent intent = new Intent(this, ExtraPageActivity.class);
        startActivity(intent);
    }

    private void goToHome(){
        Intent intent = new Intent(this,BaseActivity.class);
        startActivity(intent);
    }


}