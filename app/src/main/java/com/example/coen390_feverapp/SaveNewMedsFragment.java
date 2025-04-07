package com.example.coen390_feverapp;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


public class SaveNewMedsFragment extends DialogFragment {

    private Button yesButton, noButton;
    private MedicationActivity medicationActivity;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(context instanceof MedicationActivity){
            medicationActivity = (MedicationActivity) context;
        }
    }

    public SaveNewMedsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_save_new_meds, container, false);

        yesButton = view.findViewById(R.id.yesButton);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPrefs = getActivity().getSharedPreferences("user_prefs", 0);
                String currentUser = sharedPrefs.getString("current_user",null);
                Log.d("current_user_check",currentUser);
                DBHelper dbHelper = new DBHelper(getActivity().getBaseContext());
                int userID = dbHelper.getUserID(currentUser);
                String medicationName = medicationActivity.getMedicationNameText();
                /*if(medicationActivity != null){
                    if(!dbHelper.medicationInDB(medicationName,userID)) {
                        Log.d("med_db_check", "medication: " + medicationName);
                        boolean inserted = dbHelper.insertNewMedication(medicationName, userID);
                        if (inserted) {
                            Toast.makeText(getContext(), "New medication saved!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "Error inserting medication", Toast.LENGTH_LONG).show();
                        }
                        medicationActivity.showMedicationsOnSpinner();
                    }
                }*/
                //save into db
                dismiss();
            }
        });

        noButton = view.findViewById(R.id.noButton);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }
}