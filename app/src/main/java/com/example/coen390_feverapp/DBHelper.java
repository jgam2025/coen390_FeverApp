package com.example.coen390_feverapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

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
        sqLiteDatabase.execSQL("create table users(username TEXT primary key, password TEXT )");

        sqLiteDatabase.execSQL("CREATE TABLE profiles (profile_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "profile_name TEXT NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES users(username))");
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1){
        sqLiteDatabase.execSQL("drop table if exists users");

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

    public boolean insertProfile(Profile profile){
        SQLiteDatabase myDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("profile_id",profile.getId());
        contentValues.put("user_id",profile.getUserId());
        contentValues.put("profile_name",profile.getName());
        long result = myDB.insert("profiles",null,contentValues);
        if (result == -1) return false;
        else return true;
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
        return myDB.query("users", new String[] {"_id","username"},null,null,null,null,"username");
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

}

