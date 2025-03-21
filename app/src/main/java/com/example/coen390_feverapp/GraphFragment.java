package com.example.coen390_feverapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.database.Cursor;
import android.graphics.Color;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
        dbHelper = new DBHelper(getContext());

        fetchTemperatureData();
        return view;
    }

    private void fetchTemperatureData() {
        List<Entry> entries = new ArrayList<>();
        final List<String> dateTimeLabels = new ArrayList<>();

        // Get the current profile
        currentProfile = requireActivity().getSharedPreferences("user_prefs", getContext().MODE_PRIVATE)
                .getString("current_profile", "default");

        // Retrieve all temperature data
        Cursor cursor = dbHelper.getMeasurementsByFullDateAndProfile("", "", currentProfile); // Get all data
        int index = 0;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") float temperature = cursor.getFloat(cursor.getColumnIndex("temperature_value"));
                @SuppressLint("Range") String measurementTime = cursor.getString(cursor.getColumnIndex("measurement_time"));

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

        // Configure X Axis to display date & time
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

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

        // Set data and refresh chart
        chart.setData(new LineData(dataSet));
        chart.invalidate();
    }
}

