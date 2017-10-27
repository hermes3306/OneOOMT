package com.joonho.oneoomt.db;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.joonho.oneoomt.HistoryActivity;
import com.joonho.oneoomt.file.myActivity;
import com.joonho.oneoomt.util.StringUtil;
import com.joonho.oneoomt.util.modifiedDate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import static java.lang.System.in;
import static java.lang.System.out;

public class DBGateway {
    private static final String TAG = "DBGateway";
    private static DBHelp dbHelp=null;
    private static SQLiteDatabase mDB;

    public void init(Context ctx) {
        if(dbHelp == null) {
            dbHelp = new DBHelp(ctx);
            mDB = dbHelp.getWritableDatabase();
            Log.e(TAG, " DBHelp in initialize .........");
        }

        if(mDB.isOpen() == false) {
            Log.e(TAG," SQLiteDatabase mDB.isOpen is false ...........");
            try {
                mDB = dbHelp.getWritableDatabase();
            }catch(Exception e) {
                e.printStackTrace();
                throw e;
            }
            Log.e(TAG, " SQLiteDatabase mDB open.........");
        }
    }

    /*  ***************************************************      */
    /*                      SELECT                               */
    /*  ***************************************************      */
    public int numberofMarkers(Context ctx) {
        init(ctx);
        StringBuffer sql = new StringBuffer();
        sql.append("select * from mylocation6");
        Cursor cursor = mDB.rawQuery(sql.toString(), null);
        return cursor.getCount();
    }

    public Vector allLatLngVector(Context ctx) {
        init (ctx);

        StringBuffer sql = new StringBuffer();
        sql.append("select latitude, longitude, added_on from mylocation6");
        Cursor cursor = mDB.rawQuery(sql.toString(), null);


        Vector v = new Vector();

        while(cursor.moveToNext()) {
            double latitude = cursor.getDouble(0);
            double longitude = cursor.getDouble(1);
            String added_on = cursor.getString(2);

            Hashtable ht = new Hashtable();
            ht.put("latitude", latitude );
            ht.put("longitude", longitude);
            ht.put("added_on", added_on);

            v.add(ht);
        }
        return v;
    }

    public List<LatLng> allLatLng(Context ctx) {
        init (ctx);

        StringBuffer sql = new StringBuffer();
        sql.append("select latitude, longitude from mylocation6");
        Cursor cursor = mDB.rawQuery(sql.toString(), null);


        List <LatLng> lll = new ArrayList<LatLng>();
        while(cursor.moveToNext()) {
            double latitude = cursor.getDouble(0);
            double longitude = cursor.getDouble(1);
            LatLng latlng = new LatLng(latitude, longitude);
            lll.add(latlng);
        }
        return lll;
    }

    public Vector allLocTime(Context ctx) {
        init (ctx);

        StringBuffer sql = new StringBuffer();
        sql.append("select added_on from mylocation6");
        Cursor cursor = mDB.rawQuery(sql.toString(), null);


        Vector vt = new Vector();
        while(cursor.moveToNext()) {
            String  added_on  = cursor.getString(0);
            Date date = StringUtil.StringToDate(added_on,"yyyy-MM-dd HH:mm:ss");
            vt.add(date.getTime());
            Log.e(TAG, "" + "" + added_on + "     > " + date.getTime());
        }
        return vt;
    }

    public ArrayList<myActivity> allActivitiy(Context ctx) {
        init (ctx);

        StringBuffer sql = new StringBuffer();
        sql.append("select latitude, longitude, added_on from mylocation6");
        Cursor cursor = mDB.rawQuery(sql.toString(), null);


        ArrayList <myActivity> lll = new ArrayList<myActivity>();
        while(cursor.moveToNext()) {
            double latitude = cursor.getDouble(0);
            double longitude = cursor.getDouble(1);
            String  added_on  = cursor.getString(2);

            myActivity ma = new myActivity(latitude, longitude, added_on);
            Log.e(TAG, ma.toString());
            lll.add(ma);
        }
        return lll;
    }


    public int LocSize(Context ctx) {
        init(ctx);

        StringBuffer sql = new StringBuffer();
        sql.append("select * from mylocation6");
        Cursor cursor = mDB.rawQuery(sql.toString(), null);

        int size = 0; //Optimization required....
        while (cursor.moveToNext()) {
            size++;
        }
        return size;
    }


    public void printMarkers(Context ctx) {
        init (ctx);

        StringBuffer sql = new StringBuffer();
        sql.append("select latitude, longitude, added_on from mylocation6");
        Cursor cursor = mDB.rawQuery(sql.toString(), null);

        int i=1;
        while(cursor.moveToNext()) {
            double latitude = cursor.getDouble(0);
            double longitude = cursor.getDouble(1);
            String added_on = cursor.getString(2);

            Log.e(TAG, "" + i + ": l/l=" + latitude + "/" + longitude + " at " + added_on);
            i++;
        }
    }

    /*  ***************************************************      */
    /*                      INSERT                               */
    /*  ***************************************************      */
    public void addLoc(Context ctx, Location loc) {
        init(ctx);
        StringBuffer sql = new StringBuffer();
        sql.append("insert into mylocation6(latitude, longitude) values (");
        sql.append(loc.getLatitude());
        sql.append(", ");
        sql.append(loc.getLongitude());
        sql.append(");");

        Log.e(TAG, sql.toString());
        try {
            mDB.execSQL(sql.toString());
        }catch(Exception  ee) {
            ee.printStackTrace();
            // new UI().errDlg(ctx,"addLoc Error", ee.toString());
        }
    }

    public void dbresetwithoutloastloc(Context ctx) {
        init(ctx);

        LatLng lastll = lastLatLng(ctx);
        String sql = "delete from mylocation6";
        Log.e(TAG,sql);
        mDB.execSQL(sql);

        if(lastll != null) {
            StringBuffer sql2 = new StringBuffer();
            sql2.append("insert into mylocation6 (latitude, longitude) values (");
            sql2.append(lastll.latitude);
            sql2.append(", ");
            sql2.append(lastll.longitude);
            sql2.append(");");

            Log.e(TAG, sql2.toString());
            mDB.execSQL(sql2.toString());
        }
    }


    public Hashtable lastLatLngHashtable(Context ctx) {
        init(ctx);

        StringBuffer sql = new StringBuffer();
        sql.append("select latitude, longitude, added_on from mylocation6");
        try {
            Cursor cursor = mDB.rawQuery(sql.toString(), null);


            if (cursor.moveToLast()) {
                double latitude = cursor.getDouble(0);
                double longitude = cursor.getDouble(1);
                String added_on = cursor.getString(2);

                Hashtable ht = new Hashtable();
                ht.put("latitude", latitude );
                ht.put("longitude", longitude);
                ht.put("added_on", added_on);
                return ht;
            }
        }catch(Exception ee) {
            ee.printStackTrace();
        }
        return null;
    }

    public LatLng lastLatLng(Context ctx) {
        init(ctx);
        StringBuffer sql = new StringBuffer();
        sql.append("select latitude, longitude from mylocation6");
        try {
            Cursor cursor = mDB.rawQuery(sql.toString(), null);


            if (cursor.moveToLast()) {
                double latitude = cursor.getDouble(0);
                double longitude = cursor.getDouble(1);
                LatLng latlng = new LatLng(latitude, longitude);
                return latlng;
            }
        }catch(Exception ee) {
            ee.printStackTrace();
        }
        return null;
    }

    public static String default_DIR = "OneOOMT";

    public void serailizeActivitywithName(Context ctx, String fname) {
        serializeActivities2(ctx,false,default_DIR, fname);
    }

    public String serializeActivities2(Context ctx, boolean external, String dirname) {
        return serializeActivities2(ctx,external,dirname, null);
    }

    public String serializeActivities2(Context ctx, boolean external, String dirname, String fname) {
        init (ctx);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.KOREA);
        Date now = new Date();

        String fileName = formatter.format(now) + ".ser";
        if(fname != null) fileName = fname;

        File dir = null;
        if(external) {
            dir = new File(ctx.getExternalFilesDir("/"), dirname);
        } else {
            dir = new File(ctx.getFilesDir(), dirname);
        }

        if(!dir.exists()) dir.mkdirs();
        File file = new File(dir, fileName);

        Log.e(TAG, "ActivityFileName to be written: " + file.toString());

        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream out = new ObjectOutputStream(bos);

            StringBuffer sql = new StringBuffer();
            sql.append("select latitude, longitude, added_on from mylocation6");
            Cursor cursor = mDB.rawQuery(sql.toString(), null);

            ArrayList list = new ArrayList<>();

            int i=0;
            while(cursor.moveToNext()) {
                double latitude = cursor.getDouble(0);
                double longitude = cursor.getDouble(1);
                String added_on = cursor.getString(2);

                myActivity ma = new myActivity(latitude, longitude, added_on);
                out.writeObject(ma);
                list.add(ma);
                i++;
                //Log.e(TAG, "" + i + "th activity(" + latitude + "," + longitude + "," + added_on + ")" );
            }
            out.close();
            Log.e(TAG, "\n\n serializeActivities2() called!!\n\n");
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
        return fileName;
    }


    public void deleteDB(Context ctx) {
        init(ctx);
        try {
            mDB.execSQL("delete from mylocation6");
            Log.e(TAG, "table mylocation6 deleted!!");
        }catch(Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public void ListToDB(Context ctx, List<LatLng> list) {
        deleteDB(ctx);
//        mDB.beginTransaction();
        try {
            for (int i = 0; i < list.size(); i++) {
                LatLng l = list.get(i);
                StringBuffer sql = new StringBuffer();
                sql.append("insert into mylocation6(latitude, longitude) values(");
                sql.append(l.latitude);
                sql.append(", ");
                sql.append(l.longitude);
                sql.append(")");
                Log.e(TAG, sql.toString());
                mDB.execSQL(sql.toString());
                //mDB.setTransactionSuccessful();
            }
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        } finally {
//            mDB.endTransaction();
        }
    }

    public void deserializeToDB(Context ctx, String fname) {
        init (ctx);

        ArrayList <myActivity> list = this.deserializeActivities(ctx, fname);
        if(list.size()==0) {return;}
        if(list == null) {return;}

        double lat[] = new double[list.size()];
        double lon[] = new double[list.size()];
        ArrayList<String> ado = new ArrayList<String>();

        for(int i=0;i<list.size();i++) {
            lat[i] = ((myActivity)(list.get(i))).latitude;
            lon[i] = ((myActivity)(list.get(i))).longitude;
            ado.add(((myActivity)(list.get(i))).added_on);
        }

//        mDB.beginTransaction();
        for(int i=0;i<lat.length;i++) {
            StringBuffer sql = new StringBuffer();
            sql.append("insert into mylocation6(latitude, longitude, added_on) values(");
            sql.append(lat[i]);
            sql.append(", ");
            sql.append(lon[i]);
            sql.append(", ");
            sql.append("\"" + ado.get(i) + "\"");
            sql.append(");");

            Log.e(TAG, sql.toString());
            try {
                mDB.execSQL(sql.toString());
//                mDB.setTransactionSuccessful();
            }catch(Exception  ee) {
                ee.printStackTrace();
                Log.e(TAG, ee.toString());
            }
        }
//        mDB.endTransaction();
    }

    /******************************************************/
    /***         File Operations 10/01/17 jhpark     *****/
    /******************************************************/
    public File[] getallActivities(Context ctx) {
    File dir = new File(ctx.getFilesDir(), "OneOOMT");
        if(!dir.exists()) dir.mkdirs();
        File[] files = dir.listFiles();
        // Arrays.sort(files);
        Arrays.sort(files, new modifiedDate());
        Log.e(TAG, "# of Activities: " + files.length);
        for(int i=files.length-1;i>=0;i--) {
            Log.e(TAG,"" + files[i].getAbsolutePath());
        }
        return files;
    }

    public File getLastActivityFile(Context ctx) {
        File dir = new File(ctx.getFilesDir(), "OneOOMT");
        if(!dir.exists()) dir.mkdirs();
        File[] files = dir.listFiles();
        if(files.length >0) {
            Arrays.sort(files, new modifiedDate());
            return files[0];
        }
        else return null;
    }

    public String getLastActivityFileName(Context ctx) {
        File f = getLastActivityFile(ctx);
        if (f!= null) return f.getName();
        return null;
    }

    public List<LatLng> syncLastActivityFilewithScreen(Context ctx) {
        ArrayList <myActivity> mal = getLastActivity(ctx);
        List <LatLng> list = new ArrayList<LatLng>();
        Log.e(TAG, "********* syncLastActivityFilewithScreen() called");
        for(int i=0;i<mal.size();i++) {
            myActivity ma = mal.get(i);
            LatLng ll = new LatLng(ma.latitude, ma.longitude);
            list.add(ll);
            Log.e(TAG, "" + i + ")" + ll );
        }
        return list;
    }

    public List<LatLng> syncActivityFilewithScreen(Context ctx, String filepath) {
        File file = new File(filepath);
        Log.e(TAG, "********* ActivityFileName to be synced: " + file.getAbsolutePath());
        ArrayList <myActivity> mal = getActivity(ctx, file);

        List <LatLng> list = new ArrayList<LatLng>();
        Log.e(TAG, "********* syncActivityFilewithScreen() called");
        for(int i=0;i<mal.size();i++) {
            myActivity ma = mal.get(i);
            LatLng ll = new LatLng(ma.latitude, ma.longitude);
            list.add(ll);
            Log.e(TAG, "" + i + ")" + ll );
        }
        return list;
    }

    public ArrayList<myActivity> getActivity(Context ctx, File file) {
        if(file == null) {
            Toast.makeText(ctx, "No File Found!", Toast.LENGTH_SHORT).show();
            return null;
        }
        Log.e(TAG, "********* ActivityFileName to be read: " + file.getAbsolutePath());

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream in = null;

        ArrayList list = null;
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            in = new ObjectInputStream(bis);

            list = new ArrayList<myActivity>();
            myActivity ma=null;

            do {
                try {
                    ma = (myActivity) in.readObject();
                    list.add(ma);
                }catch(Exception ex) {
                    if(list != null) return list;
                }
            } while(ma != null);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
                if (bis !=null) in.close();
                if (fis !=null) fis.close();

                if(list.size()==0) {
                    Log.e(TAG, "File ("+ file.getAbsolutePath() +") corrupted !!!!");
                    file.delete();
                    Log.e(TAG, "File ("+ file.getAbsolutePath() +") deleted  !!!!");
                }
            }catch(Exception e) {}
        }
        return null;
    }

    public ArrayList<myActivity> getLastActivity(Context ctx) {
        init(ctx);
        File file = getLastActivityFile(ctx);
        return getActivity(ctx, file);
    }

    /* Checks if external storage is available for read and write */
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean serializeBoth = true;

    public String serializeActivities(Context ctx) {
        if(isExternalStorageWritable() && serializeBoth) serializeActivities2(ctx, true, "OneOOMT");
        return serializeActivities2(ctx, false, "OneOOMT");
    }

    public void deleteActivity(Context ctx, String fname) {
        File dir = new File(ctx.getFilesDir(), "OneOOMT");
        File file = new File(dir, fname);
        Log.e(TAG, "********* ActivityFileName to be deleted: " + file.getAbsolutePath());
        file.delete();
    }

    public ArrayList<myActivity> deserializeActivities(Context ctx, String fname) {
        init(ctx);
        File dir = new File(ctx.getFilesDir(), "OneOOMT");
        File file = new File(dir, fname);
        Log.e(TAG, "********* ActivityFileName to be read: " + file.getAbsolutePath());


        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream in = null;

        ArrayList list = null;
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            in = new ObjectInputStream(bis);

            list = new ArrayList<myActivity>();
            myActivity ma=null;

            do {
                try {
                    ma = (myActivity) in.readObject();
                    list.add(ma);
                }catch(Exception ex) {
                    if(list != null) return list;
                }
            } while(ma != null);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
                if (bis !=null) in.close();
                if (fis !=null) fis.close();

                if(list.size()==0) {
                    Log.e(TAG, "File ("+ fname +") corrupted !!!!");
                    file.delete();
                    Log.e(TAG, "File ("+ fname +") deleted  !!!!");

                }
            }catch(Exception e) {}
        }
        return null;
    }


    public void printLocs(Context ctx) {
        init(ctx);

        List<LatLng> lll = allLatLng(ctx);

        if (lll == null) {
            Log.e(TAG, "Database not found!!!");
            return;
        }

        int i=0;
        for(LatLng ll : lll) {
            Log.e(TAG, "(" + i + ") Latitude/Longitude: " + ll.latitude + "," +ll.longitude);
        }
    }
}
