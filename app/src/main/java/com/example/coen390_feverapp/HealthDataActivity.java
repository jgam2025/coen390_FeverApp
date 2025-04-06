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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
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

        insertTestData();

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

    }

    private void showDatesOnSpinner(){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.date_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weekOfSpinner.setAdapter(adapter);

        weekOfSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRange = (String) parent.getItemAtPosition(position);
                Log.d("selected_range", selectedRange);
                handleRangeSelection(selectedRange);
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

    private void handleRangeSelection(String selectedRange){
        Calendar startDate = Calendar.getInstance(); // the date they will be going back to!

        switch (selectedRange){
            case "Today":
                Log.d("case_check", "Today");
                break;
            case "Yesterday":
                startDate.add(Calendar.DATE, -1);
                Log.d("case_check", "Yesterday");
                break;
            case "Last 7 Days":
                startDate.add(Calendar.DATE, -6);
                Log.d("case_check", "Last 7 Days");
                break;
            case "Last 14 Days":
                startDate.add(Calendar.DATE, -13);
                Log.d("case_check", "Last 14 days");
                break;
            case "Last Month":
                startDate.add(Calendar.MONTH, -1);
                Log.d("case_check", "Last Month");
                break;
            case "All Time":
                startDate=null;
                Log.d("case_check", "All Time");
                break;
            default:
                startDate=null;
                break;
        }

        loadSymptomHistory(startDate);
        loadMedicationHistory(startDate);
    }


    private void loadMedicationHistory(Calendar date) {
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentProfile = sharedPrefs.getString("current_profile", "default");
        String formattedDate;
        if(date!=null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            formattedDate = simpleDateFormat.format(date.getTime());
        } else {
            formattedDate = null;
        }
        List<String> medsList = dbHelper.getMedicationHistoryList(currentProfile, formattedDate);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, medsList);
        medsListView.setAdapter(adapter);
    }


    private void loadSymptomHistory(Calendar date){
        String formattedDate;
        if(date!=null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            formattedDate = simpleDateFormat.format(date.getTime());
        } else {
            formattedDate = null;
        }
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentProfile = sharedPrefs.getString("current_profile", "default");

        List<String> symptomList = dbHelper.getSymptomHistory(currentProfile, formattedDate);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, symptomList);
        symptomsListView.setAdapter(adapter);
    }

    private void insertTestData(){
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentProfile = sharedPrefs.getString("current_profile", "default");
        String currentUser = sharedPrefs.getString("current_user",null);
        int userID = dbHelper.getUserID(currentUser);

        //insert medications:
        dbHelper.insertMedication(currentProfile, "xanax", "20","2025-04-03 12:00:00");
        dbHelper.insertMedication(currentProfile, "ozempic", "","2025-04-02 12:00:00");
        dbHelper.insertMedication(currentProfile, "adderall", "","2025-04-01 12:00:00");
        dbHelper.insertMedication(currentProfile, "ozempic", "","2025-03-29 12:00:00");
        dbHelper.insertMedication(currentProfile, "zoloft", "20","2025-03-28 12:00:00");
        dbHelper.insertMedication(currentProfile, "ozempic", "0","2025-03-27 12:00:00");
        dbHelper.insertMedication(currentProfile, "adderall", "","2025-03-26 12:00:00");
        dbHelper.insertMedication(currentProfile, "lexapro", "20","2025-03-25 12:00:00");
        dbHelper.insertMedication(currentProfile, "lexapro", "15","2025-03-24 12:00:00");
        dbHelper.insertMedication(currentProfile, "ozempic", "20","2025-02-25 12:00:00");

        //insert symptoms:
        dbHelper.insertSymptoms(currentProfile, "too much swag", "2025-04-03 12:00:00");
        dbHelper.insertSymptoms(currentProfile, "in a coma", "2025-04-02 12:00:00");
        dbHelper.insertSymptoms(currentProfile, "in a coma", "2025-04-01 12:00:00");
        dbHelper.insertSymptoms(currentProfile, "Nausea", "2025-03-29 12:00:00");
        dbHelper.insertSymptoms(currentProfile, "Headache", "2025-03-28 12:00:00");
        dbHelper.insertSymptoms(currentProfile, "Cough", "2025-03-27 12:00:00");
        dbHelper.insertSymptoms(currentProfile, "Cough, Headache", "2025-03-26 12:00:00");
        dbHelper.insertSymptoms(currentProfile, "swag", "2025-03-27 12:00:00");
        dbHelper.insertSymptoms(currentProfile, "swag", "2025-02-28 12:00:00");
        dbHelper.insertSymptoms(currentProfile, "in a coma", "2025-02-27 12:00:00");

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