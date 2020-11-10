package com.estmob.android.sendanywhere.sdk.ui.example.core.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "SendHstory.db";
    public static final String HISTORY_TABLE_NAME = "history";
    public static final String CONTACTS_COLUMN_ID = "id";
    public static final String HISTORY_COLUMN_NAME = "name";
    public static final String HISTORY_COLUMN_PATH = "path";
    public static final String HISTORY_COLUMN_SIZE = "size";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(
                "create table history " +
                        "(id integer primary key, name text, path text, size text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS history");
        onCreate(db);
    }

    public void insertFileDetail (String name, String path, long size) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("path", path);
        contentValues.put("size", size);
        db.insert("history", null, contentValues);
    }

    public ArrayList<String> getAllName() {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from history", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(HISTORY_COLUMN_NAME)));
            res.moveToNext();
        }
        return array_list;
    }

    public ArrayList<String> getAllPath() {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from history", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getString(res.getColumnIndex(HISTORY_COLUMN_PATH)));
            res.moveToNext();
        }

        return array_list;
    }

    public ArrayList<Long> getAllSize() {
        ArrayList<Long> array_list = new ArrayList<Long>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from history", null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            array_list.add(res.getLong(res.getColumnIndex(HISTORY_COLUMN_SIZE)));
            res.moveToNext();
        }
        return array_list;
    }
}