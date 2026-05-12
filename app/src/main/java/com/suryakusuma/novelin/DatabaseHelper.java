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
    public static final String COL_NOVEL_AUTHOR = "AUTHOR";
    public static final String COL_NOVEL_DESC = "DESCRIPTION";
    public static final String COL_NOVEL_COVER_URL = "COVER_URL";
    public static final String COL_NOVEL_COVER_RES = "COVER_RES";
    public static final String COL_NOVEL_URL = "NOVEL_URL"; // Kolom baru untuk URL Scraping

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 4); // Naikkan versi ke 4
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, USERNAME TEXT UNIQUE, PASSWORD TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_SAVED + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, USERNAME TEXT, TITLE TEXT, AUTHOR TEXT, DESCRIPTION TEXT, COVER_URL TEXT, COVER_RES INTEGER, NOVEL_URL TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED);
            db.execSQL("CREATE TABLE " + TABLE_SAVED + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, USERNAME TEXT, TITLE TEXT, AUTHOR TEXT, DESCRIPTION TEXT, COVER_URL TEXT, COVER_RES INTEGER, NOVEL_URL TEXT)");
        }
    }

    public boolean saveNovel(String username, Novel novel) {
        if (isNovelSaved(username, novel.getTitle())) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_SAVED_USERNAME, username);
        contentValues.put(COL_NOVEL_TITLE, novel.getTitle());
        contentValues.put(COL_NOVEL_AUTHOR, novel.getAuthor());
        contentValues.put(COL_NOVEL_DESC, novel.getDescription());
        contentValues.put(COL_NOVEL_COVER_URL, novel.getCoverUrl());
        contentValues.put(COL_NOVEL_COVER_RES, novel.getCoverResourceId());
        contentValues.put(COL_NOVEL_URL, novel.getNovelUrl());
        
        long result = db.insert(TABLE_SAVED, null, contentValues);
        return result != -1;
    }

    public List<Novel> getFullSavedNovels(String username) {
        List<Novel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SAVED, null, COL_SAVED_USERNAME + "=?", new String[]{username}, null, null, null);
        
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOVEL_TITLE));
            String author = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOVEL_AUTHOR));
            String desc = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOVEL_DESC));
            String url = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOVEL_COVER_URL));
            int resId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_NOVEL_COVER_RES));
            String novelUrl = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOVEL_URL));

            Novel novel;
            if (url != null && !url.isEmpty()) {
                novel = new Novel(title, author, desc, url, novelUrl);
            } else {
                novel = new Novel(title, author, desc, resId, new ArrayList<>());
            }
            list.add(novel);
        }
        cursor.close();
        return list;
    }

    public boolean isNovelSaved(String username, String title) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SAVED, null, COL_SAVED_USERNAME + "=? AND " + COL_NOVEL_TITLE + "=?", new String[]{username, title}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean deleteSavedNovel(String username, String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_SAVED, COL_SAVED_USERNAME + "=? AND " + COL_NOVEL_TITLE + "=?", new String[]{username, title}) > 0;
    }

    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID}, COL_USERNAME + "=?", new String[]{username}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
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
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID}, COL_USERNAME + "=? AND " + COL_PASSWORD + "=?", new String[]{username, password}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}
