package com.example.coen390_feverapp;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class
InsertInfoFragment extends DialogFragment {

    protected ImageView infoImageView;
    protected FloatingActionButton closeFAB;

    public InsertInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_insert_info, container, false);

        infoImageView = view.findViewById(R.id.infoImageView);

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