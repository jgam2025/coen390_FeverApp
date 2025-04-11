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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class NewProfileFragment extends DialogFragment {

    protected TextView createTextView;
    protected EditText nameEditText;
    protected Button saveButton;
    protected FloatingActionButton closeButton;
    private SymptomLogActivity symptom_activity;


    public NewProfileFragment() {
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(context instanceof SymptomLogActivity){
            symptom_activity = (SymptomLogActivity) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_profile, container, false);

        createTextView = view.findViewById(R.id.createTextView);
        nameEditText = view.findViewById(R.id.nameEditText);
        saveButton = view.findViewById(R.id.saveButton);
        closeButton = view.findViewById(R.id.closeButton);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPrefs = getActivity().getSharedPreferences("user_prefs", 0);
                String currentUser = sharedPrefs.getString("current_user",null);
                Log.d("current_user_check",currentUser);
                DBHelper dbHelper = new DBHelper(getActivity().getBaseContext());
                int userID = dbHelper.getUserID(currentUser);
                String name = nameEditText.getText().toString();

                if(!name.isBlank()){
                    Profile profile = new Profile(name,userID);
                    dbHelper.insertProfile(profile);

                    Toast.makeText(getContext(), "Profile Saved", Toast.LENGTH_SHORT).show();

                    if(symptom_activity != null){
                        symptom_activity.showProfilesOnSpinner();
                    }
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Please Enter a Name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}