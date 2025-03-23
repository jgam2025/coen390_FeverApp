package com.example.coen390_feverapp;

import android.content.Context;
import android.content.SharedPreferences;
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

import java.util.ArrayList;
import java.util.List;


public class NewSymptomFragment extends DialogFragment {

    protected EditText newSymptomEditText;
    protected Button saveSymptomButton;
    protected FloatingActionButton closeNewFAB;

    public NewSymptomFragment() {
        // Required empty public constructor
    }

    /*
    public interface onSymptomSavedListener {
        void onSymptomSaved(List<CheckBox> newCheckBoxList);
    }

    private onSymptomSavedListener listener;

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(context instanceof onSymptomSavedListener){
            listener = (onSymptomSavedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnSymptomSavedListener");
        }
    }


     */
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
                //create checkbox
                CheckBox newSymptomCheckbox = new CheckBox(getContext());
                newSymptomCheckbox.setText(symptom);
                LinearLayout container = getActivity().findViewById(R.id.linearCheckBoxLayout);
                container.addView(newSymptomCheckbox);
                ScrollView scrollView = getActivity().findViewById(R.id.checkboxScroll);
                scrollView.requestLayout();
                scrollView.fullScroll(View.FOCUS_DOWN);

                List<CheckBox> newCheckBoxList = new ArrayList<>();
                newCheckBoxList.add(newSymptomCheckbox);
                //listener.onSymptomSaved(newCheckBoxList);
                //save checkbox into db associated w user
                SharedPreferences sharedPrefs = getActivity().getSharedPreferences("user_prefs", 0);
                String currentUser = sharedPrefs.getString("current_user",null);
                int user = dbHelper.getUserID(currentUser);
                dbHelper.insertNewSymptom(symptom,user);

                dismiss();
            }
        });

        return view;
    }
}