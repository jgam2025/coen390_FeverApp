package com.example.coen390_feverapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.database.Cursor;
import android.graphics.Color;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.ArrayList;
import java.util.List;

public class GraphFragment extends DialogFragment {

    private LineChart chart;
    private DBHelper dbHelper;
    private String currentProfile; // Profile name for filtering

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graph, container, false);
        chart = view.findViewById(R.id.chart);
        dbHelper = new DBHelper(requireActivity());

        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE);
        currentProfile = prefs.getString("current_profile", "default");
        Log.d("GRAPH_DEBUG", "Current profile in graph: " + currentProfile);

        fetchTemperatureData();
        return view;
    }

    private void fetchTemperatureData() {
        Log.d("GRAPH_DEBUG", "fetchTemperatureData() was called");
        List<Entry> entries = new ArrayList<>();
        final List<String> dateTimeLabels = new ArrayList<>();

        // Get the current profile
        currentProfile = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("current_profile", "default");
        Log.d("GRAPH_DEBUG", "Current Profile: " + currentProfile);


        // Retrieve all temperature data
        Cursor cursor = dbHelper.getAllMeasurementsByProfile(currentProfile);
// Get all data

        if (cursor == null) {
            Log.d("GRAPH_DEBUG", "Cursor is null!");
        } else if (!cursor.moveToFirst()) {
            Log.d("GRAPH_DEBUG", "Cursor is empty for profile: " + currentProfile);
        } else {
            Log.d("GRAPH_DEBUG", "Data found for profile: " + currentProfile);
        }
        int index = 0;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") float temperature = cursor.getFloat(cursor.getColumnIndex("temperature_value"));
                @SuppressLint("Range") String measurementTime = cursor.getString(cursor.getColumnIndex("measurement_time"));

                Log.d("GRAPH_DEBUG", "Reading: " + measurementTime + " - " + temperature); // 👈 DEBUG

                // Store temperature values and corresponding date-time labels
                entries.add(new Entry(index, temperature));
                dateTimeLabels.add(measurementTime); // Full date-time for X-axis labels
                index++;
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (!entries.isEmpty()) {
            plotGraph(entries, dateTimeLabels);
        }
    }

    private void plotGraph(List<Entry> entries, final List<String> dateTimeLabels) {
        LineDataSet dataSet = new LineDataSet(entries, "Temperature Readings");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.RED);
        dataSet.setValueTextSize(12f);
        dataSet.setDrawValues(false);

        // Configure X Axis to display date & time
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawLabels(false);
        xAxis.setDrawAxisLine(true);
        chart.setExtraBottomOffset(16f);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(entries.size() - 1);



        // Format X-axis labels directly in GraphDialogFragment
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);
                if (index >= 0 && index < dateTimeLabels.size()) {
                    return dateTimeLabels.get(index); // Display date-time instead of index numbers
                } else {
                    return "";
                }
            }
        });

        // Configure Y Axis (Temperature)
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setGranularity(0.5f);
        chart.getAxisRight().setEnabled(false);
        chart.setExtraLeftOffset(15f);
        chart.setExtraRightOffset(30f);

        // Set data and refresh chart
        chart.setData(new LineData(dataSet));
        chart.invalidate();

        chart.getDescription().setEnabled(false);
    }
}

