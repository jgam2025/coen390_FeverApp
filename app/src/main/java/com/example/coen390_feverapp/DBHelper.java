package com.example.coen390_feverapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

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
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)"); //medication data table

        sqLiteDatabase.execSQL("CREATE TABLE symptoms (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "profile_name TEXT NOT NULL, " +
                "symptom_type TEXT, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)"); //symptoms data table

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

    //functions for user creation and validation
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

    //functions for inserting and retrieving profiles
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

    //functions for inserting and retrieving temp data

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

    //functions for inserting and retreiving medications
    public boolean insertMedication(String profile, String name, String dose) {
        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("profile_name", profile);
        cv.put("name", name);
        cv.put("dose", dose);
        long result = myDB.insert("medication", null, cv);
        return result != -1;
    }

    public boolean insertNewMedication(String medicationName, int userID){
        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("medication_name", medicationName);
        contentValues.put("user_id", userID);
        long result = myDB.insert("user_added_medications", null, contentValues);
        return result != -1;
    }

    public List<String> getUserAddedMedications(int userId) {
        List<String> newMeds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
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
            Toast.makeText(context, "Get error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            System.out.println("get error: " + e.getMessage());
        }
        return newMeds;
    }

    public Cursor getMedicationHistoryByProfile(String profile) {
        SQLiteDatabase myDB = this.getReadableDatabase();
        return myDB.rawQuery("SELECT * FROM medication WHERE profile_name = ? ORDER BY _id DESC", new String[]{profile});
    }

    public Cursor getAllMeasurementsByProfile(String profile) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM temperature WHERE profile_name = ? ORDER BY measurement_time ASC",
                new String[]{profile}
        );
    }


    // Deletes a medication record by its _id.
    public boolean deleteMedication(long id) {
        SQLiteDatabase myDB = this.getWritableDatabase();
        int result = myDB.delete("medication", "_id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }


    //functions for inserting and retrieving symptoms from checkboxes
    public boolean insertSymptoms(String profile, String symptoms, String timestamp){
        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("profile_name",profile);
        contentValues.put("symptom_type",symptoms);
        contentValues.put("timestamp",timestamp);
        long result = myDB.insert("symptoms",null,contentValues);
        return result != -1;
    }

    public List<String> getSymptomHistory(String profile){
        SQLiteDatabase myDB = this.getReadableDatabase();
        List<String> symptomsList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = myDB.rawQuery("SELECT symptom_type, timestamp FROM symptoms WHERE profile_name = ? ORDER BY user_id DESC", new String[]{profile});
            if(cursor!=null){
                if (cursor.moveToFirst()) {
                    do{
                        @SuppressLint("Range") String symptomsString = cursor.getString(cursor.getColumnIndex("symptom_type"));
                        @SuppressLint("Range") String time = cursor.getString(cursor.getColumnIndex("timestamp"));
                        symptomsList.add("Date & Time: " + time + "\nSymptoms: " + symptomsString);
                    } while (cursor.moveToNext());
                } cursor.close();
            }
        } catch (Exception e){
            Toast.makeText(context, "Get error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            System.out.println("get error: " + e.getMessage());
        }
        return symptomsList;
    }

    //functions for inserting and retrieving user-added symptoms
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

}

