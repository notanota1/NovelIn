package com.suryakusuma.novelin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "NovelIn.db";
    
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "ID";
    public static final String COL_USERNAME = "USERNAME";
    public static final String COL_PASSWORD = "PASSWORD";

    public static final String TABLE_SAVED = "saved_novels";
    public static final String COL_SAVED_ID = "ID";
    public static final String COL_SAVED_USERNAME = "USERNAME";
    public static final String COL_NOVEL_TITLE = "TITLE";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 2); // Increased version for new table
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, USERNAME TEXT, PASSWORD TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_SAVED + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, USERNAME TEXT, TITLE TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED);
        onCreate(db);
    }

    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_USERNAME, username);
        contentValues.put(COL_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, contentValues);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COL_USERNAME + "=? AND " + COL_PASSWORD + "=?", new String[]{username, password}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean saveNovel(String username, String title) {
        if (isNovelSaved(username, title)) return true;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_SAVED_USERNAME, username);
        contentValues.put(COL_NOVEL_TITLE, title);
        long result = db.insert(TABLE_SAVED, null, contentValues);
        return result != -1;
    }

    public boolean isNovelSaved(String username, String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SAVED, null, COL_SAVED_USERNAME + "=? AND " + COL_NOVEL_TITLE + "=?", new String[]{username, title}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public List<String> getSavedNovels(String username) {
        List<String> titles = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SAVED, new String[]{COL_NOVEL_TITLE}, COL_SAVED_USERNAME + "=?", new String[]{username}, null, null, null);
        while (cursor.moveToNext()) {
            titles.add(cursor.getString(0));
        }
        cursor.close();
        return titles;
    }
}
