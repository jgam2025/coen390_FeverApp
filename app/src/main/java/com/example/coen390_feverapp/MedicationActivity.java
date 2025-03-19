package com.example.coen390_feverapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;

public class MedicationActivity extends AppCompatActivity {
    private EditText etMedicationName, etMedicationDose;
    private Button btnSaveMedication;
    private ListView listViewMedication;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication);

        dbHelper = new DBHelper(this);

        etMedicationName = findViewById(R.id.et_medication_name);
        etMedicationDose = findViewById(R.id.et_medication_dose);
        btnSaveMedication = findViewById(R.id.btn_save_medication);
        listViewMedication = findViewById(R.id.list_medication);

        btnSaveMedication.setOnClickListener(v -> {
            String name = etMedicationName.getText().toString().trim();
            String dose = etMedicationDose.getText().toString().trim();

            if (!name.isEmpty()) {
                dbHelper.insertMedication(name, dose);
                Toast.makeText(this, "Medication saved!", Toast.LENGTH_SHORT).show();
                etMedicationName.setText("");
                etMedicationDose.setText("");
                loadMedicationHistory();
            } else {
                Toast.makeText(this, "Please enter a medication name", Toast.LENGTH_SHORT).show();
            }
        });

        loadMedicationHistory();
    }

    private void loadMedicationHistory() {
        Cursor cursor = dbHelper.getMedicationHistory();
        String[] fromColumns = {"name", "dose", "timestamp"};
        int[] toViews = {R.id.med_name, R.id.med_dose, R.id.med_timestamp};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this, R.layout.item_medication, cursor, fromColumns, toViews, 0);

        listViewMedication.setAdapter(adapter);

        listViewMedication.setOnItemClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Confirmation")
                    .setMessage("do you want to delete this medication ?")
                    .setPositiveButton("yes", (dialog, which) -> {

                        if (dbHelper.deleteMedication(id)) {
                            Toast.makeText(MedicationActivity.this, "Medication deleted!", Toast.LENGTH_SHORT).show();
                            loadMedicationHistory();
                        } else {
                            Toast.makeText(MedicationActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("No", (dialog, which) -> {

                        dialog.dismiss();
                    })
                    .show();
        });
    }




}
