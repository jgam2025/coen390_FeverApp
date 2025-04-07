package com.example.coen390_feverapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class PhoneNumberActivity extends AppCompatActivity {

    List<TextView> phoneNumberTextViewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_phone_number);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        setupUI();
        setUpToolbar();
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

            return true;
        }

        else if (id == R.id.miMore) {
            goToExtraPage();
            return true;
        }
        else{
            return super.onOptionsItemSelected(item);

        }
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void goToExtraPage(){
        Intent intent = new Intent(this, ExtraPageActivity.class);
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

    private void goToMedicationPage(){
        Intent intent = new Intent(this, MedicationActivity.class);
        startActivity(intent);
    }
    private void Graph(){
        GraphFragment graphDialog = new GraphFragment();
        graphDialog.show(getSupportFragmentManager(), "GraphDialog");
    }

    private void goToSymptomLogActivity(){
        Intent intent = new Intent(this,SymptomLogActivity.class);
        startActivity(intent);
    }

    private void setupUI() {
        phoneNumberTextViewList = new ArrayList<>();
        phoneNumberTextViewList.add(findViewById(R.id.emergencyNumber));
        phoneNumberTextViewList.add(findViewById(R.id.infoSanteNumber));
        phoneNumberTextViewList.add(findViewById(R.id.QMASNumber));

        for (TextView phoneNumberTextView:phoneNumberTextViewList) {
            phoneNumberTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialNumber(phoneNumberTextView);
                }
            });
        }

        TextView webMdLink = findViewById(R.id.webMdLink);
        TextView clevelandClinicLink = findViewById(R.id.clevelandClinicLink);

        webMdLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent webLinkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.webmd.com/first-aid/fevers-causes-symptoms-treatments"));
                startActivity(webLinkIntent);
            }
        });

        clevelandClinicLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent webLinkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://my.clevelandclinic.org/health/symptoms/10880-fever"));
                startActivity(webLinkIntent);
            }
        });

    }

    private void dialNumber(TextView phoneNumberTextView) {
        String phoneNumberText = phoneNumberTextView.getText().toString().replace("-", "").trim();

        Uri telUri = Uri.parse("tel:" + Long.parseLong(phoneNumberText));
        Intent dialPhoneIntent = new Intent(Intent.ACTION_DIAL, telUri);
        startActivity(dialPhoneIntent);

    }
}