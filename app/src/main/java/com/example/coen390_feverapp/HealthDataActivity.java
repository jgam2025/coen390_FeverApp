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
import android.widget.ListView;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class HealthDataActivity extends AppCompatActivity {

    private Spinner profilesOptionSpinner, weekOfSpinner;
    private ListView symptomsListView, medsListView;
    private Button trendButton;
    private DBHelper dbHelper;
    String selectedProfile, selectedRange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_health_data);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true); // Hide the title
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

        //insertTestData();

        weekOfSpinner = findViewById(R.id.weekOfSpinner);
        showDatesOnSpinner();
        selectedRange = (String) weekOfSpinner.getSelectedItem();

        profilesOptionSpinner = findViewById(R.id.profilesOptionSpinner);
        showProfilesOnSpinner();

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
                selectedRange = (String) parent.getItemAtPosition(position);
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
                handleRangeSelection(selectedRange);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

    }

    private void handleRangeSelection(String selectedRange){
        Calendar startDate = Calendar.getInstance(); // the date they will be going back to!
        Calendar endDate = Calendar.getInstance();

        switch (selectedRange){
            case "Today":
                Log.d("case_check", "Today");
                endDate = null;
                break;
            case "Yesterday":
                startDate.add(Calendar.DATE, -1);
                endDate.add(Calendar.DATE, -1);
                endDate.set(Calendar.HOUR_OF_DAY, 23);
                endDate.set(Calendar.MINUTE, 59);
                Log.d("case_check", "Yesterday");
                break;
            case "Last 7 days":
                startDate.add(Calendar.DATE, -6);
                endDate = null;
                Log.d("case_check", "Last 7 Days");
                break;
            case "Last 14 days":
                startDate.add(Calendar.DATE, -13);
                endDate = null;
                Log.d("case_check", "Last 14 days");
                break;
            case "Last month":
                startDate.add(Calendar.MONTH, -1);
                endDate = null;
                Log.d("case_check", "Last Month");
                break;
            case "All time":
                startDate=null;
                endDate = null;
                Log.d("case_check", "All Time");
                break;
            default:
                startDate=null;
                endDate = null;
                break;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        String formattedStartDate, formattedEndDate;

        if (startDate != null) {
            startDate.set(Calendar.HOUR_OF_DAY, 0);
            startDate.set(Calendar.MINUTE, 0);
            formattedStartDate = simpleDateFormat.format(startDate.getTime());
        } else {
            formattedStartDate = null;
        }

        if(endDate != null){
            formattedEndDate = simpleDateFormat.format(endDate.getTime());
        } else {
            formattedEndDate = null;
        }

        if(selectedProfile != "Select profile") {
            loadSymptomHistory(formattedStartDate, formattedEndDate);
            loadMedicationHistory(formattedStartDate, formattedEndDate);
        } else {
            List<String> noItems = new ArrayList<>();
            noItems.add("No profile selected!");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,noItems);
            medsListView.setAdapter(adapter);
            symptomsListView.setAdapter(adapter);
        }
    }

    private void loadMedicationHistory(String startDate, String endDate) {
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentProfile = sharedPrefs.getString("current_profile", "default");

        List<String> medsList = dbHelper.getMedicationHistoryList(currentProfile, startDate, endDate);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, medsList);
        medsListView.setAdapter(adapter);
    }

    private void loadSymptomHistory(String startDate, String endDate){

        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentProfile = sharedPrefs.getString("current_profile", "default");

        List<String> symptomList = dbHelper.getSymptomHistory(currentProfile, startDate, endDate);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, symptomList);
        symptomsListView.setAdapter(adapter);
    }

    private void insertTestData(){
        SharedPreferences sharedPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentProfile = sharedPrefs.getString("current_profile", "default");

        //insert some medications
        dbHelper.insertMedication(currentProfile, "test med", "20","2025-04-06 12:00:00");
        dbHelper.insertMedication(currentProfile, "test med", "20","2025-04-05 12:00:00");
        dbHelper.insertMedication(currentProfile, "test med", "","2025-04-04 12:00:00");
        dbHelper.insertMedication(currentProfile, "test med", "","2025-03-30 12:00:00");
        dbHelper.insertMedication(currentProfile, "test med", "20","2025-03-24 12:00:00");
        dbHelper.insertMedication(currentProfile, "test med", "20","2025-03-09 12:00:00");
        dbHelper.insertMedication(currentProfile, "test med", "20","2025-02-20 12:00:00");

        //insert some symptoms
        dbHelper.insertSymptoms(currentProfile, "test symptoms", "2025-04-06 12:00:00");
        dbHelper.insertSymptoms(currentProfile, "test symptoms", "2025-04-05 12:00:00");
        dbHelper.insertSymptoms(currentProfile, "test symptoms", "2025-04-04 12:00:00");
        dbHelper.insertSymptoms(currentProfile, "test symptoms", "2025-03-30 12:00:00");
        dbHelper.insertSymptoms(currentProfile, "test symptoms", "2025-03-24 12:00:00");
        dbHelper.insertSymptoms(currentProfile, "test symptoms", "2025-03-09 12:00:00");
        dbHelper.insertSymptoms(currentProfile, "test symptoms", "2025-02-20 12:00:00");

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