package com.example.coen390_feverapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;
import android.util.Log;


import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DBName ="register.db";
    private Context context;

    public static final int DB_VERSION = 4;

    public DBHelper(@Nullable Context context) {
        super(context, DBName, null, DB_VERSION);

        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            sqLiteDatabase.execSQL("CREATE TABLE users(username TEXT PRIMARY KEY, password TEXT )");
            Log.d("DBHelper", "Table users created");

            sqLiteDatabase.execSQL("CREATE TABLE profiles (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id TEXT NOT NULL, " +
                    "profile_name TEXT NOT NULL, " +
                    "FOREIGN KEY(user_id) REFERENCES users(username))");
            Log.d("DBHelper", "Table profiles created");

            sqLiteDatabase.execSQL("CREATE TABLE Medication (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "dose TEXT, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
            Log.d("DBHelper", "Table Medication created");

        } catch (Exception e) {
            Log.e("DBHelper", "Error creating tables: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase,int oldVersion, int newVersion){
        sqLiteDatabase.execSQL("drop table if exists users");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS profiles"); //medicamentation
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS Medication");//medicamentation
        onCreate(sqLiteDatabase);

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
    public boolean deleteMedication(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete("Medication", "id=?", new String[]{String.valueOf(id)});
        db.close();
        return rowsDeleted > 0;
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

    public Cursor getAllUsersCursor(){
        SQLiteDatabase myDB = this.getReadableDatabase();
        return myDB.query("users", new String[] {"id AS _id","username"},null,null,null,null,"username");
    }


    public List<String> getAllUsers(){
        SQLiteDatabase myDB = this.getReadableDatabase();
        List<String> userList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = myDB.query("users", null, null, null, null, null, null);
            if (cursor != null){
                if(cursor.moveToFirst()){
                    do {
                        @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndex("username"));
                        userList.add(username);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Get error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return userList;
    }
    public void checkTables() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if (cursor.moveToFirst()) {
            do {
                Log.d("DBHelper", "Table found: " + cursor.getString(0));
            } while (cursor.moveToNext());
        }

        cursor.close();
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
    //medicamentation
    public boolean insertMedication(String name, String dose) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("dose", dose);

        long newRowId = db.insert("Medication", null, values);
        db.close();

        return newRowId != -1;
    }


    //medicamentation
    public Cursor getMedicationHistory() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id AS _id, name, dose, timestamp FROM Medication ORDER BY timestamp DESC", null);

        return cursor;
    }




}

