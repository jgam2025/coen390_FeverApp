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
    //list names for extra page features
    String[] pageNames = {"Health Resources & Phone Numbers", "Add New Profile", "Export Health Data", "Sensor Calibration", "Log Out"};
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

    //toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    //make toolbar functional
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.miperson) {
            goToHealth();
            return true;
        }
        else if (id == R.id.mihome) {
            GoToHome();
            return true;
        } else{
            return super.onOptionsItemSelected(item);
        }
    }

    //go to home page
    private void GoToHome(){
        Intent intent = new Intent(this, BaseActivity.class);
        startActivity(intent);
    }

    //go to health history page
    private void goToHealth(){
        Intent intent = new Intent(this, HealthDataActivity.class);
        startActivity(intent);
    }

    private void setupUI() {
        additionalPageListView = findViewById(R.id.additionalPageListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pageNames);
        additionalPageListView.setAdapter(adapter);

        additionalPageListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPage = pageNames[position];

            if (selectedPage.equals("Health Resources & Phone Numbers")) {
                startActivity(new Intent(this, PhoneNumberActivity.class));
            } else if (selectedPage.equals("Sensor Calibration")) {
                startActivity(new Intent(this, CalibrationActivity.class));
            } else if (selectedPage.equals("Export Health Data")) {
                startActivity(new Intent(this, ExportDataActivity.class));
            } else if (selectedPage.equals("Add New Profile")) {
                NewProfileFragment newProfile = new NewProfileFragment();
                newProfile.show(getFragmentManager(), "InsertProfile");
            } else if (selectedPage.equals("Log Out")) {
                startActivity(new Intent(this, LoginActivity.class));
            }
        });
    }
}
