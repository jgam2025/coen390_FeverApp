package com.example.coen390_feverapp;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class
InsertInfoFragment extends DialogFragment {

    protected TextView titleTextView, infoTextView;
    protected FloatingActionButton closeFAB;

    public InsertInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_insert_info, container, false);

        titleTextView = view.findViewById(R.id.titleTextView);
        infoTextView = view.findViewById(R.id.infoTextView);
        infoTextView.setText(getString(R.string.info_text));

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