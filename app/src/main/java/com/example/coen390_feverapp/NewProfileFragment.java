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

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class NewProfileFragment extends DialogFragment {

    protected TextView createTextView;
    protected EditText nameEditText;
    protected Button saveButton;
    protected FloatingActionButton closeButton;
    private TemperatureMeasurementPage activity;

    public NewProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(context instanceof TemperatureMeasurementPage){
            activity = (TemperatureMeasurementPage) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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

                    if(activity != null){
                        activity.showUsersOnSpinner();
                    }
                    dismiss();
                }
            }
        });

        return view;
    }
}