package com.example.coen390_feverapp;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class NewSymptomFragment extends DialogFragment {

    protected EditText newSymptomEditText;
    protected Button saveSymptomButton;
    protected FloatingActionButton closeNewFAB;

    public NewSymptomFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_symptom, container, false);

        newSymptomEditText = view.findViewById(R.id.newSymptomEditText);
        saveSymptomButton = view.findViewById(R.id.saveSymptomButton);
        closeNewFAB = view.findViewById(R.id.closeNewFAB);

        closeNewFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        saveSymptomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBHelper dbHelper;
                dbHelper = new DBHelper(getContext());
                String symptom = newSymptomEditText.getText().toString();
                if(symptom.isEmpty()){
                    Toast.makeText(getContext(), "Please enter a symptom", Toast.LENGTH_LONG).show();
                }
                // put into db
                dbHelper.insertNewSymptom(symptom);
                //create checkbox
                CheckBox newSymptomCheckbox = new CheckBox(getContext());
                newSymptomCheckbox.setText(symptom);
                LinearLayout container = getActivity().findViewById(R.id.linearCheckBoxLayout);
                container.addView(newSymptomCheckbox);
                ScrollView scrollView = getActivity().findViewById(R.id.checkboxScroll);
                scrollView.requestLayout();
                scrollView.fullScroll(View.FOCUS_DOWN);

                //save checkbox into db so it will be shown next time user opens activity

                dismiss();
            }
        });

        return view;
    }
}