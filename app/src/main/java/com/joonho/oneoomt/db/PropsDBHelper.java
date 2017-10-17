package com.joonho.oneoomt.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by joonhopark on 2017. 9. 9..
 */

public class PropsDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "myProperties2";
    private static final int DATABASE_VERSION = 1;

    public PropsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table myProperties2(_id integer primary key autoincrement, property text, value text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists myProperties2");
        onCreate(db);
    }
}
