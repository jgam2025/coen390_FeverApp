package com.example.coen390_feverapp;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class InsertDialogFragment extends DialogFragment {

    protected TextView titleTextView, howToTextView, instructionsTextView, environmentTextView,
        environmentInstrTextView, additionalInfoTextView, modelTextView, contactTextView;
    protected FloatingActionButton closeFAB;

    public InsertDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_insert_dialog, container, false);

        titleTextView = view.findViewById(R.id.titleTextView);
        howToTextView = view.findViewById(R.id.howToTextView);
        instructionsTextView = view.findViewById(R.id.instructionsTextView);
        environmentTextView = view.findViewById(R.id.environmentTextView);
        environmentInstrTextView = view.findViewById(R.id.environmentInstrTextView);
        additionalInfoTextView = view.findViewById(R.id.additionalInfoTextView);
        modelTextView = view.findViewById(R.id.modelTextView);
        contactTextView = view.findViewById(R.id.contactTextView);
        closeFAB = view.findViewById(R.id.closeFAB);

        closeFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }
}