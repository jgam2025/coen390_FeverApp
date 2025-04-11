package com.example.coen390_feverapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DBName ="register.db";
    private Context context;

    public DBHelper(@Nullable Context context) {
        super(context, DBName, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.execSQL("create table users(username TEXT primary key, password TEXT )"); //users table

        sqLiteDatabase.execSQL("CREATE TABLE profiles (user_id INTEGER NOT NULL, " +
                "profile_name TEXT NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES users(username))"); //profiles table

        sqLiteDatabase.execSQL("CREATE TABLE temperature (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "profile_name TEXT NOT NULL, " +
                "measurement_time TEXT NOT NULL, " +
                "temperature_value TEXT NOT NULL)"); // temp data table

        sqLiteDatabase.execSQL("CREATE TABLE medication (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "profile_name TEXT NOT NULL, " +
                "name TEXT NOT NULL, " +
                "dose TEXT, " +
                "timestamp TEXT NOT NULL)"); //medication data table

        sqLiteDatabase.execSQL("CREATE TABLE symptoms (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "profile_name TEXT NOT NULL, " +
                "symptom_type TEXT, " +
                "timestamp TEXT NOT NULL)"); //symptoms data table

        sqLiteDatabase.execSQL("CREATE TABLE user_added_symptoms (" +
                "user_id INTEGER NOT NULL, " +
                "symptom TEXT NOT NULL)"); //new user added symptoms data table

        sqLiteDatabase.execSQL("CREATE TABLE user_added_medications (" +
                "user_id INTEGER NOT NULL, " +
                "medication_name TEXT NOT NULL)"); // new user added medications data table
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1){
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS users");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS temperature");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS medication");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS symptoms");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS user_added_symptoms");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS user_added_medications");
    }


    public boolean insertData(String username, String password){
        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("username",username);
        contentValues.put("password",password);
        long result = myDB.insert("users",null, contentValues);
        if (result==-1)return false;
        else return true;

    }

    public int getUserID(String username) {
        SQLiteDatabase myDB = this.getReadableDatabase();
        Cursor cursor = myDB.rawQuery("SELECT username FROM users WHERE username = ?", new String[]{username});

        if(cursor != null && cursor.moveToFirst()){
            @SuppressLint("Range") String currentUser = cursor.getString(cursor.getColumnIndex("username"));
            cursor.close();
            return currentUser.hashCode();
        } else return -1;
    }

    public boolean checkUsername(String username){
        SQLiteDatabase myDB = this.getReadableDatabase();
        Cursor cursor = myDB.rawQuery("SELECT * FROM users WHERE username = ? COLLATE NOCASE", new String[]{username});
        return cursor.getCount() > 0;
    }

    public boolean checkUser(String username, String pwd){
        SQLiteDatabase myDB = this.getReadableDatabase();
        Cursor cursor = myDB.rawQuery("SELECT * FROM users WHERE username = ? COLLATE NOCASE AND password = ?", new String[]{username, pwd});
        return cursor.getCount() > 0;
    }

    public boolean insertProfile(Profile profile){
        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("user_id",profile.getUserId());
        contentValues.put("profile_name",profile.getName());
        long result = myDB.insert("profiles",null,contentValues);
        if (result == -1) return false;
        else return true;
    }

    public List<String> getProfiles(String user){
        SQLiteDatabase myDB = this.getReadableDatabase();
        List<String> profileList = new ArrayList<>();
        profileList.add("Select profile");
        Cursor cursor = null;
        int userID = getUserID(user);
        try {
            cursor = myDB.rawQuery("SELECT profile_name FROM profiles WHERE user_id = ?", new String[]{String.valueOf(userID)});
            if(cursor != null){
                if(cursor.moveToFirst()){
                    do {
                        @SuppressLint("Range") String profileName = cursor.getString(cursor.getColumnIndex("profile_name"));
                        profileList.add(profileName);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        } catch (Exception e){
            Toast.makeText(context, "Get error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return profileList;
    }


    public boolean insertTemperature(String profileName, String measurementTime, String temperatureValue) {
        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("profile_name", profileName);
        contentValues.put("measurement_time", measurementTime);
        contentValues.put("temperature_value", temperatureValue);
        long result = myDB.insert("temperature", null, contentValues);
        return result != -1;
    }
    public Cursor getLastTemperatureByProfile(String profile) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM temperature WHERE profile_name = ? ORDER BY id DESC LIMIT 1", new String[]{profile});
    }

    public Cursor getMeasurementsByFullDateAndProfile(String year, String monthDay, String profile) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM temperature WHERE substr(measurement_time,1,4)=? AND substr(measurement_time,6,5)=? AND profile_name = ? ORDER BY measurement_time DESC",
                new String[]{year, monthDay, profile}
        );
    }

    public Cursor getAllMeasurementsByProfile(String profile, String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        if(startDate == null) {
            return db.rawQuery(
                    "SELECT * FROM temperature WHERE profile_name = ? ORDER BY measurement_time ASC",
                    new String[]{profile}
            );
        } else {
            if (endDate == null){
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                String now = simpleDateFormat.format(calendar.getTime());
                return db.rawQuery("SELECT * FROM temperature WHERE profile_name = ? " +
                        "AND measurement_time BETWEEN ? AND ? ORDER BY measurement_time DESC", new String[]{profile, startDate, now});
            } else if (endDate != null) {
                return db.rawQuery("SELECT * FROM temperature WHERE profile_name = ? AND measurement_time BETWEEN ? AND ? ORDER BY measurement_time DESC",
                        new String[]{profile,startDate,endDate}
                );
            }
        }
        return null;
    }

    /*
    public Cursor getTemperatureMeasurementEntries(String profile, String startDate, String endDate){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        List<String>
        if(startDate == null){ // null startdate - all time
            try{
                cursor = db.rawQuery("SELECT * FROM temperature WHERE profile_name = ? ORDER BY measurement_time ASC",
                        new String[]{profile});
                if(cursor != null){
                    if(cursor.moveToFirst()){
                        do {

                        }
                    }
                }
            }
        }

    }
     */

    //dose in mg
    public boolean insertMedication(String profile, String name, String dose, String timestamp) {
        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("profile_name", profile);
        cv.put("name", name);
        cv.put("dose", dose);
        cv.put("timestamp", timestamp);
        long result = myDB.insert("medication", null, cv);
        return result != -1;
    }

    public boolean insertNewMedicationToList(String medicationName, int userID){
        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("medication_name", medicationName);
        contentValues.put("user_id", userID);
        long result = myDB.insert("user_added_medications", null, contentValues);
        return result != -1;
    }

    public List<String> getUserAddedMedicationsList(int userId) {
        List<String> newMeds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        newMeds.add("");
        try {
            cursor = db.rawQuery("SELECT medication_name FROM user_added_medications WHERE user_id = ?", new String[]{String.valueOf(userId)});
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        @SuppressLint("Range") String medication = cursor.getString(cursor.getColumnIndex("medication_name"));
                        newMeds.add(medication);
                    } while (cursor.moveToNext());
                }
            }
            cursor.close();
        } catch (Exception e){
            Toast.makeText(context, "Get error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            System.out.println("get error: " + e.getMessage());
        }
        return newMeds;
    }

    //check if medication already in db
    public boolean checkMedication(String medication, int userID){
        SQLiteDatabase myDB = this.getReadableDatabase();
        Cursor cursor = myDB.rawQuery("SELECT * FROM user_added_medications WHERE medication_name = ? AND user_id = ? COLLATE NOCASE", new String[]{medication, String.valueOf(userID)});
        return cursor.getCount() > 0;
    }


    public List<String> getMedicationHistoryList(String profile, String startDate, String endDate) {
        SQLiteDatabase myDB = this.getReadableDatabase();
        List<String> medicationList = new ArrayList<>();
        Cursor cursor = null;
        if(startDate==null) {
            Log.d("null_date", "date is null");
            try {
                cursor = myDB.rawQuery("SELECT * FROM medication WHERE profile_name = ? ORDER BY timestamp DESC", new String[]{profile});
                if (cursor != null) {
                    Log.d("profile_check", "profile: " + profile);//profile name= medication for this profile
                    if (cursor.moveToFirst()) {
                        Log.d("check", "cursor moved to first a");
                        do {
                            @SuppressLint("Range") String medicationName = cursor.getString(cursor.getColumnIndex("name"));
                            @SuppressLint("Range") String medicationDose = cursor.getString(cursor.getColumnIndex("dose"));
                            @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("timestamp"));
                            if (medicationDose != "") {
                                medicationList.add("Date & Time: " + time + "\n" + medicationName + ", " + medicationDose + "mg");
                            } else {
                                medicationList.add("Date & Time: " + time + "\n" + medicationName);
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                Toast.makeText(context, "Get error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                System.out.println("get error: " + e.getMessage());
            }
        } else {
            if(endDate == null){ // means it wasn't yesterday
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                String now = simpleDateFormat.format(calendar.getTime());
                Log.d("range", "between " + startDate + " & " + now);

                try {
                    cursor = myDB.rawQuery("SELECT _id, name, dose, timestamp FROM medication WHERE profile_name = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp DESC",
                            new String[]{profile, startDate, now});
                    if (cursor != null) {
                        Log.d("profile_check","profile: " + profile);
                        if (cursor.moveToFirst()) {
                            Log.d("check","cursor moved to first b");
                            do {
                                @SuppressLint("Range") String medicationName = cursor.getString(cursor.getColumnIndex("name"));
                                @SuppressLint("Range") String medicationDose = cursor.getString(cursor.getColumnIndex("dose"));
                                @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("timestamp"));
                                if (medicationDose != null && !medicationDose.isEmpty())  {
                                    medicationList.add("Date & Time: " + time + "\n" + medicationName + ", " + medicationDose + "mg");
                                } else {
                                    medicationList.add("Date & Time: " + time + "\n" + medicationName);
                                }
                            } while (cursor.moveToNext());
                        }
                        cursor.close();
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "Get error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    System.out.println("get error: " + e.getMessage());
                }
            } else if (endDate != null){ // yesterdayyyy
                Log.d("range", "between " + startDate + " & " + endDate);
                try {
                    cursor = myDB.rawQuery("SELECT _id, name, dose, timestamp FROM medication WHERE profile_name = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp DESC",
                            new String[]{profile, startDate, endDate});
                    if (cursor != null) {
                        Log.d("profile_check","profile: " + profile);
                        if (cursor.moveToFirst()) {
                            Log.d("check","cursor moved to first b");
                            do {
                                @SuppressLint("Range") String medicationName = cursor.getString(cursor.getColumnIndex("name"));
                                @SuppressLint("Range") String medicationDose = cursor.getString(cursor.getColumnIndex("dose"));
                                @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("timestamp"));
                                if (medicationDose != null && !medicationDose.isEmpty())  {
                                    medicationList.add("Date & Time: " + time + "\n" + medicationName + ", " + medicationDose + "mg");
                                } else {
                                    medicationList.add(medicationName);
                                }
                            } while (cursor.moveToNext());
                        }
                        cursor.close();
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "Get error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    System.out.println("get error: " + e.getMessage());
                }
            }
        }

        return medicationList;
    }


    // Deletes a medication record by its _id.
    public boolean deleteMedication(long id) {
        SQLiteDatabase myDB = this.getWritableDatabase();
        int result = myDB.delete("medication", "_id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }


    public boolean insertSymptoms(String profile, String symptoms, String timestamp){
        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("profile_name",profile);
        contentValues.put("symptom_type",symptoms);
        contentValues.put("timestamp",timestamp);
        long result = myDB.insert("symptoms",null,contentValues);
        return result != -1;
    }

    public List<String> getSymptomHistory(String profile, String startDate, String endDate){
        SQLiteDatabase myDB = this.getReadableDatabase();
        List<String> symptomsList = new ArrayList<>();
        Cursor cursor = null;
        if(startDate==null) {
            Log.d("null_date", "date is null");
            try {
                cursor = myDB.rawQuery("SELECT symptom_type, timestamp FROM symptoms WHERE profile_name = ? ORDER BY timestamp DESC", new String[]{profile});
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        //Log.d("check","cursor moved to first a");
                        do {
                            @SuppressLint("Range") String symptomsString = cursor.getString(cursor.getColumnIndex("symptom_type"));
                            @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("timestamp"));
                            symptomsList.add("Date & Time: " + time + "\nSymptoms: " + symptomsString);
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                Toast.makeText(context, "Get error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                System.out.println("get error: " + e.getMessage());
            }
        } else { // date is not null -> select symptoms from desired range
            if(endDate == null){ // not yesterday
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                String now = simpleDateFormat.format(calendar.getTime());
                Log.d("range", "between " + startDate + " & " + now);

                try {
                    cursor = myDB.rawQuery(
                            "SELECT symptom_type, timestamp FROM symptoms WHERE profile_name = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp DESC",
                            new String[]{profile, startDate, now});

                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            //Log.d("check","cursor moved to first b");
                            do {
                                @SuppressLint("Range") String symptomsString = cursor.getString(cursor.getColumnIndex("symptom_type"));
                                @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("timestamp"));
                                symptomsList.add("Date & Time: " + time + "\nSymptoms: " + symptomsString);
                            } while (cursor.moveToNext());
                        }
                        cursor.close();
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "Get error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if (endDate != null){ //its yesterYAY
                Log.d("range", "between " + startDate + " & " + endDate);
                try {
                    cursor = myDB.rawQuery(
                            "SELECT symptom_type, timestamp FROM symptoms WHERE profile_name = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp DESC",
                            new String[]{profile, startDate, endDate});

                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            //Log.d("check","cursor moved to first b");
                            do {
                                @SuppressLint("Range") String symptomsString = cursor.getString(cursor.getColumnIndex("symptom_type"));
                                @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("timestamp"));
                                symptomsList.add("Date & Time: " + time + "\nSymptoms: " + symptomsString);
                            } while (cursor.moveToNext());
                        }
                        cursor.close();
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "Get error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

        }
        return symptomsList;
    }


    public boolean insertNewSymptom(String symptom, int userID){
        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("symptom",symptom);
        contentValues.put("user_id",userID);
        long result = myDB.insert("user_added_symptoms",null,contentValues);
        return result != -1;
    }

    public List<String> getUserAddedSymptoms(int userId) {
        List<String> symptoms = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT symptom FROM user_added_symptoms WHERE user_id = ?", new String[]{String.valueOf(userId)});
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        @SuppressLint("Range") String symptom = cursor.getString(cursor.getColumnIndex("symptom"));
                        symptoms.add(symptom);
                    } while (cursor.moveToNext());
                }
            }
            cursor.close();
        } catch (Exception e){
            Toast.makeText(context, "Get error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            System.out.println("get error: " + e.getMessage());
        }
        return symptoms;
    }

    public boolean checkSymptom(String symptom){
        SQLiteDatabase myDB = this.getReadableDatabase();
        Cursor cursor = myDB.rawQuery("SELECT * FROM user_added_symptoms WHERE symptom = ? COLLATE NOCASE", new String[]{symptom});
        Cursor cursor2 = myDB.rawQuery("SELECT * FROM symptoms WHERE symptom_type = ? COLLATE NOCASE", new String[]{symptom});
        if(cursor.getCount() > 0) return true;
        else if (cursor2.getCount() > 0) return true;
        else return false;
    }
    public List<String> getTemperatureHistoryList(String profile, String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> temps = new ArrayList<>();
        Cursor cursor = null;

        try {
            if (endDate == null) {
                endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        .format(Calendar.getInstance().getTime());
            }

            cursor = db.rawQuery(
                    "SELECT measurement_time, temperature_value FROM temperature " +
                            "WHERE profile_name = ? AND measurement_time BETWEEN ? AND ? ORDER BY measurement_time ASC",
                    new String[]{profile, startDate, endDate});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("measurement_time"));
                    @SuppressLint("Range") String temp = cursor.getString(cursor.getColumnIndex("temperature_value"));
                    temps.add(time + "," + temp);
                } while (cursor.moveToNext());

                cursor.close();
            }

        } catch (Exception e) {
            Toast.makeText(context, "Temperature fetch error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return temps;
    }



}

