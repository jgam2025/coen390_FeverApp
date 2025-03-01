package com.example.coen390_feverapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DBName ="register.db";

    public DBHelper(@Nullable Context context) {
        super(context, DBName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.execSQL("create table users(username TEXT primary key, password TEXT )");

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
}

