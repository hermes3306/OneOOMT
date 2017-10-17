package com.joonho.oneoomt.db;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by joonhopark on 2017. 9. 9..
 */

public class PropsDB {

    private static final String TAG = "PropsDB";
    PropsDBHelper dbHelp=null;

    public void init(Context ctx) {
        if(dbHelp == null) {
            Log.e(TAG," PropsDBHelper is null ...........");
            dbHelp = new PropsDBHelper(ctx);
            Log.e(TAG, " PropsDBHelper in initialize .........");
        }
    }

    public void deleteAll() {
        SQLiteDatabase db = null;
        try {
            db = dbHelp.getWritableDatabase();
        }catch(Exception e) {
            Log.e(TAG,e.toString());
            e.printStackTrace();
        }

        if (db == null) {
            Log.e(TAG, "Databse not found!!!");
            return;
        }

        StringBuffer sql = new StringBuffer();
        sql.append("delete from myProperties2");
        Log.e(TAG, sql.toString());
        db.execSQL(sql.toString());
    }

    public void addProperty(Context ctx, String prop, String val) {
        init(ctx);
        SQLiteDatabase db = null;

        try {
            db = dbHelp.getWritableDatabase();
        }catch(Exception e) {
            Log.e(TAG,e.toString());
            e.printStackTrace();
        }

        if (db == null) {
            Log.e(TAG, "Databse not found!!!");
            return;
        }

        StringBuffer sql = new StringBuffer();
        sql.append("insert into myProperties2 (property, value) values (");
        sql.append("\"" + prop + "\"");
        sql.append(", ");
        sql.append("\"" + val + "\"");
        sql.append(");");

        Log.e(TAG, sql.toString());
        db.execSQL(sql.toString());
    }

    public String getProperty(Context ctx, String prop) {
        init(ctx);

        SQLiteDatabase db = null;
        try {
            db = dbHelp.getReadableDatabase();
        }catch(Exception e) {
            e.printStackTrace();
        }

        if (db == null) {
            Toast.makeText(ctx, "Database not found", Toast.LENGTH_LONG);
            return null;
        }

        StringBuffer sql = new StringBuffer();
        sql.append("select value from myproperties2 where property=\"" + prop + "\"");
        Cursor cursor = db.rawQuery(sql.toString(), null);


        // need to optimize
        if (cursor.moveToLast()) {
            return cursor.getString(0);
        }
        return null;
    }
}
