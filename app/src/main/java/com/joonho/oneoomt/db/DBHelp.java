package com.joonho.oneoomt.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelp  extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mylocation6";
    private static final int DATABASE_VERSION = 1;

    public DBHelp(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuffer sb =  new StringBuffer()
                .append("create table mylocation6(_id integer primary key autoincrement, ")
                .append("latitude real,")
                .append("longitude real, ")
                .append("added_on datetime not null default (datetime('now', 'localtime')));");

        db.execSQL(sb.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists mylocation6");
        onCreate(db);
    }
}
