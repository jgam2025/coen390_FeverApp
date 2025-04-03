package com.example.coen390_feverapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ExtraPageActivity extends AppCompatActivity {

    String[] pageNames = {"Health Resources & Phone Numbers", "Sensor Calibration","Export Health Data"};

    protected ListView additionalPageListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_extra_page);

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

        }  else if (id == R.id.mihome) {
            goToHomepage();
            return true;

        } else{
                return super.onOptionsItemSelected(item);

        }
    }

    private void goToHealth(){
        Intent intent = new Intent(this, HealthDataActivity.class);
        startActivity(intent);
    }


    private void goToHomepage(){
        Intent intent = new Intent(this, BaseActivity.class);
        startActivity(intent);
    }
    private void addProfile(){
        NewProfileFragment newProfile = new NewProfileFragment();
        newProfile.show(getFragmentManager(), "InsertProfile");
    }

    private void goToMedicationPage(){
        Intent intent = new Intent(this, MedicationActivity.class);
        startActivity(intent);
    }
    private void Graph(){
        GraphFragment graphDialog = new GraphFragment();
        graphDialog.show(getSupportFragmentManager(), "GraphDialog");
    }
    private void goSymptomPage(){
        Intent intent = new Intent(this, SymptomLogActivity.class);
        startActivity(intent);
    }

    private void setupUI() {

        additionalPageListView = findViewById(R.id.additionalPageListView);

        // set adapter (will write the items on the listView)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pageNames);
        additionalPageListView.setAdapter(adapter);

        additionalPageListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPage = pageNames[position];

            if (selectedPage.equals("Health Resources & Phone Numbers")){
                Intent intent = new Intent(ExtraPageActivity.this, PhoneNumberActivity.class);
                startActivity(intent);
            }

            if(selectedPage.equals("Sensor Calibration")){
                Intent intent = new Intent(ExtraPageActivity.this, CalibrationActivity.class);
                startActivity(intent);
            }

            if (selectedPage.equals("Export Health Data")) {
                Intent intent = new Intent(ExtraPageActivity.this, ExportDataActivity.class);
                startActivity(intent);
            }
        });
    }


}