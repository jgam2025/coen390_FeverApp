package com.example.coen390_feverapp;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GraphFragment extends DialogFragment {

    private LineChart chart;
    private DBHelper dbHelper;
    private String currentProfile;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graph, container, false);
        chart = view.findViewById(R.id.chart);
        dbHelper = new DBHelper(requireActivity());


        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE);
        currentProfile = prefs.getString("current_profile", "default");
        Log.d("GRAPH_DEBUG", "Current profile in graph: " + currentProfile);

        fetchTemperatureData(view);
        return view;
    }

    private void fetchTemperatureData(View view) {
        Log.d("GRAPH_DEBUG", "fetchTemperatureData() was called");
        List<Entry> entries = new ArrayList<>();
        final List<String> dateTimeLabels = new ArrayList<>();

        currentProfile = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("current_profile", "default");
        Log.d("GRAPH_DEBUG", "Current Profile: " + currentProfile);

        String startDate = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("start_date", "default");
        String endDate = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("end_date", "default");

        Cursor cursor = dbHelper.getAllMeasurementsByProfile(currentProfile, startDate, endDate);

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
                @SuppressLint("Range") float rawTemp = cursor.getFloat(cursor.getColumnIndex("temperature_value"));
                float temperature = autoConvertIfFahrenheit(rawTemp);

                @SuppressLint("Range") String measurementTime = cursor.getString(cursor.getColumnIndex("measurement_time"));

                Log.d("GRAPH_DEBUG", "Reading: " + measurementTime + " - " + temperature);

                entries.add(new Entry(index, temperature));
                dateTimeLabels.add(measurementTime);
                index++;
            } while (cursor.moveToNext());
        }


        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") float temperature = cursor.getFloat(cursor.getColumnIndex("temperature_value"));
                @SuppressLint("Range") String measurementTime = cursor.getString(cursor.getColumnIndex("measurement_time"));

                Log.d("GRAPH_DEBUG", "Reading: " + measurementTime + " - " + temperature);

                entries.add(new Entry(index, temperature));
                dateTimeLabels.add(measurementTime);
                index++;
            } while (cursor.moveToNext());
        }

        cursor.close();
        
        if (!entries.isEmpty()) {
            plotGraph(entries, dateTimeLabels);
        }
    }

    private float autoConvertIfFahrenheit(float value) {
        if (value > 60f) {
            return (value - 32f) * 5f / 9f;
        } else {
            return value;
        }
    }

    private void plotGraph(List<Entry> entries, final List<String> dateTimeLabels) {
        LineDataSet dataSet = new LineDataSet(entries, "Temperature Readings");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.RED);
        dataSet.setValueTextSize(12f);
        dataSet.setDrawValues(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(0.2f);
        xAxis.setDrawAxisLine(true);
        chart.setExtraBottomOffset(5f);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(entries.size() - 1);
        xAxis.setLabelRotationAngle(0f);

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = Math.round(value);
                if (index >= 0 && index < dateTimeLabels.size()) {
                    String fullLabel = dateTimeLabels.get(index);

                    try {
                        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        Date date = inputFormat.parse(fullLabel);
                        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd\nHH:mm", Locale.getDefault());
                        return outputFormat.format(date);

                    } catch (ParseException e) {
                        e.printStackTrace();
                        return fullLabel;
                    }
                }
                return "";
            }
        });

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setGranularity(0.5f);
        chart.getAxisRight().setEnabled(false);
        chart.setExtraLeftOffset(13f);
        chart.setExtraRightOffset(60f);


        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.1f°C", value);
            }
        });

        chart.setData(new LineData(dataSet));
        chart.invalidate();
        chart.getDescription().setEnabled(false);
    }

    private void displayAverageTemperature(List<Entry> entries, TextView textView) {
        if (entries == null || entries.isEmpty()) {
            textView.setText("Average: -- °C");
            return;
        }

        float sum = 0f;
        for (Entry entry : entries) {
            sum += entry.getY();
        }

        float average = sum / entries.size();
        textView.setText(String.format(Locale.getDefault(), "Average: %.1f°C", average));
    }
}

