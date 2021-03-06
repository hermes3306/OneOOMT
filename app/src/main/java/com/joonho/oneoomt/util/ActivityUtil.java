package com.joonho.oneoomt.util;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.joonho.oneoomt.AdminActivity;
import com.joonho.oneoomt.R;
import com.joonho.oneoomt.RunningActivity;
import com.joonho.oneoomt.Test01Activity;
import com.joonho.oneoomt.file.ActivityStat;
import com.joonho.oneoomt.file.myActivity;
import com.joonho.oneoomt.file.myActivity2;
import com.joonho.oneoomt.file.myPicture;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import static com.joonho.oneoomt.RunningActivity.mCurLoc;
import static com.joonho.oneoomt.RunningActivity.mLatLngList;
import static com.joonho.oneoomt.RunningActivity.mLocTime;

/**
 * Created by user on 2017-10-25.
 */

public class ActivityUtil {
    public static File mediaStorageDir = null;
    public static File backupDir = null;
    public static String TAG = "ActivityUtil";
    public static ArrayList<myActivity> mActivityList = new ArrayList<myActivity>();
    public static float myzoom = 16;
    public static ArrayList<Marker> markers = new ArrayList<Marker>();
    public static String _default_ext = ".ser";

    static {
        mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "OneOOMT");
        String backupdir = StringUtil.DateToString1(new Date(), "yyyyMMdd");
        backupDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "OneOOMT" + backupdir);
    }

    public static void Admin_Backup_All_Activities(final Context context) {
        new AsyncTask<Void,Void,Void>() {
            String result;
            ProgressDialog asyncDialog = new ProgressDialog(context);
            File flist[] = getFiles();

            @Override
            protected Void doInBackground(Void... voids) {

                asyncDialog.setMax(flist.length);
                for(int i=0;i<flist.length;i++) {
                    asyncDialog.setProgress(i);
                    File _src = flist[i];
                    if(_src == null ) continue;
                    if(!_src.exists()) continue;

                    if(!backupDir.exists()) backupDir.mkdir();
                    File _tar = new File(backupDir, _src.getName());
                    Log.e(TAG, "Backup " + _src.getAbsolutePath() + " To " + _tar.getAbsolutePath());
                    try {
                        FileUtils.copyFileUsingFileStreams(_src, _tar);
                    }catch(Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, e.toString());
                    }
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("로딩중입니다..");
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                asyncDialog.dismiss();
                super.onPostExecute(aVoid);
                Toast.makeText(context, "Total " + flist.length + " activities Backup Success !!", Toast.LENGTH_LONG).show();
            }
        }.execute();
    }





    public static void Admin_Rebuild_Activities_Daily(final Context context) {
        _default_ext = ".ser";
        final File aflist[] = getFiles();
        if (aflist == null) return;
        if (aflist.length == 0) return;

        final ArrayList<myActivity> amyActList = new ArrayList<myActivity>();
        final  HashMap<String, ArrayList<myActivity>> mHashMap = new HashMap<String, ArrayList<myActivity>>();


        new AsyncTask<Void,Void,Void>() {
            String result;
            ProgressDialog asyncDialog = new ProgressDialog(context);

            @Override
            protected void onPreExecute() {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("로딩중입니다..");
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                try {

                    // -------------------------------------------
                    asyncDialog.setMax(aflist.length);
                    for (int i = 0; i < aflist.length; i++) {
                        asyncDialog.setProgress(i);
                        String afname = aflist[i].getName();
                        if(afname.endsWith(".day")) continue;

                        Log.e(TAG, "" + i + " ]" + aflist[i].getName() + "\n");
                        ArrayList<myActivity> list = deserializeFile(aflist[i]);
                        if(list == null) {
                            Log.e(TAG, "File (" + aflist[i] + ") deserialzation failed !"  );
                            continue;
                        }
                        for (int j = 0; j < list.size(); j++) {
                            myActivity ma = list.get(j);
                            //ma.added_on;
                            Date tdate = StringUtil.StringToDate(ma.added_on, "yyyy년MM월dd일_HH시mm분ss초");
                            String key = StringUtil.DateToString1(tdate, "yyyy년MM월dd일(E)");

                            if (mHashMap.containsKey(key)) {
                                ArrayList<myActivity> daylist = mHashMap.get(key);

                                if(daylist.contains(ma)) {

                                    Log.e(TAG, "Dup Data[MA] : " + ma);
                                    continue;
                                }

                                boolean found = false;
                                for(int q=0;q<daylist.size();q++) {
                                    myActivity qma = daylist.get(q);
                                    if ( qma.added_on.equals(ma.added_on) ) {
                                        found = true;
                                        break;
                                    }
                                }
                                if(found) {
                                    Log.e(TAG, "Dup Date[DT} : " + ma);
                                    continue;
                                }

                                daylist.add(ma);
                            } else {
                                ArrayList<myActivity> daylist = new ArrayList<myActivity>();
                                daylist.add(ma);
                                mHashMap.put(key, daylist);
                            }
                        }

                    }

                    // --------------------------------------------

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {

                String[] keyset = new String[mHashMap.size()];
                int inx=0;
                for(String key : mHashMap.keySet()) {
                    keyset[inx] = key;
                    inx++;
                }

                Arrays.sort(keyset);

                for(int i=0;i<keyset.length;i++) {
                    ArrayList<myActivity> daylist = mHashMap.get(keyset[i]);
                    Log.e(TAG, keyset[i] + "" + daylist.size());
                    serializeActivityIntoFile(daylist, keyset[i] + ".day");
                }

                asyncDialog.dismiss();
                super.onPostExecute(result);
            }
        }.execute();




    }

    public static void serializeActivityIntoFile(ArrayList<myActivity> list, String fileName) {
        if(list== null) return;
        if(fileName == null) return;

        if(list.size()==0) return;
        if(fileName.length() ==0 ) return;

        if(!mediaStorageDir.exists()) mediaStorageDir.mkdirs();
        File file = new File(mediaStorageDir, fileName);

        Log.e(TAG, "ActivityFileName to be written: " + file.toString());

        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream out = new ObjectOutputStream(bos);

            for(int i=0;i<list.size();i++) {
                myActivity ma = list.get(i);
                out.writeObject(ma);
            }
            out.close();
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }


    public static void serialize(String fileName) {   //  현재 스크린상의  Activity를 저장함.
        ArrayList<myActivity> malist = new ArrayList<myActivity>();
        for (int i = 0; i < mLatLngList.size(); i++) {
            Date t_date = new Date((long) mLocTime.get(i));
            String addon = StringUtil.DateToString1(t_date, "yyyy년MM월dd일_HH시mm분ss초");
            myActivity ma = new myActivity(mLatLngList.get(i).latitude, mLatLngList.get(i).longitude, addon);
            malist.add(ma);
        }
        serializeActivityIntoFile(malist, fileName);
    }

    public static String serializeWithCurrentTime(ArrayList<myActivity> list) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy년MM월dd일_HH시mm분ss초", Locale.KOREA);
        Date now = new Date();
        String fileName = formatter.format(now);
        serializeActivityIntoFile(list,fileName + ".ser");
        return fileName;
    }

    public static ArrayList<myActivity> Loc2Activity(ArrayList<Location> loclist) {
        ArrayList<myActivity> mylist = new ArrayList<myActivity>();
        for(int i=0;i<loclist.size();i++ ) {
            Location loc = (Location)loclist.get(i);
            String added_on = StringUtil.DateToString1(new Date(loc.getTime()), "yyyy년MM월dd일_HH시mm분ss초" );
            myActivity ma = new myActivity(loc.getLatitude(), loc.getLongitude(), added_on);
            mylist.add(ma);
        }
        return mylist;
    }

    public static ArrayList<myActivity2> Loc2Activity2(ArrayList<Location> loclist) {
        ArrayList<myActivity2> mylist = new ArrayList<myActivity2>();
        for(int i=0;i<loclist.size();i++ ) {
            Location loc = (Location)loclist.get(i);
            String added_on = StringUtil.DateToString1(new Date(loc.getTime()), "yyyy년MM월dd일_HH시mm분ss초" );
            myActivity2 ma = new myActivity2(loc.getLatitude(), loc.getLongitude(), loc.getAltitude(), added_on);
            mylist.add(ma);
        }
        return mylist;
    }

    public static File[] getFiles() {
        FilenameFilter fnf = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().endsWith(_default_ext);
            }
        };

        File[] flist  = mediaStorageDir.listFiles(fnf);
        Arrays.sort(flist);
        return flist;
    }

    public static File[] getFilesDaily() {
        FilenameFilter fnf = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().endsWith(".day");
            }
        };

        File[] flist  = mediaStorageDir.listFiles(fnf);
        Arrays.sort(flist);
        return flist;
    }

    public static void serialize() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy년MM월dd일_HH시mm분ss초", Locale.KOREA);
        Date now = new Date();
        String fileName = formatter.format(now) + ".ser";
        serialize(fileName);
    }

    public static File getLastActivityFile() {
        File dir = mediaStorageDir;
        if(!dir.exists()) dir.mkdirs();
        File[] files = dir.listFiles();
        if(files.length >0) {
            Arrays.sort(files, new modifiedDate());
            return files[0];
        }
        else return null;
    }

    public String getLastActivityFileName() {
        File f = getLastActivityFile();
        if (f!= null) return f.getName();
        return null;
    }

    public static void deserialize_with_lastActivity() {
        File file = getLastActivityFile();
        deserialize(file, true);
    }

    public static void deserialize(String fileName, boolean mode_append) {
        if(fileName.endsWith(".ser")) {
            // Okay
        } else {
            fileName.concat(".ser");
        }
        File file = new File(mediaStorageDir, fileName);
        deserialize(file, mode_append);
    }

    public static void deserialize(File file, boolean mode_append) {
        ArrayList list = deserializeFile(file);
        if (list==null) return;
        if (list.size()==0) return;

        if(!mode_append) {
            mLatLngList.clear();
            mLocTime.clear();
        }

        for(int i=0;i<list.size();i++) {
            myActivity ma = (myActivity)list.get(i);
            mLatLngList.add(new LatLng(ma.latitude, ma.longitude));
            Date tdate = StringUtil.StringToDate(ma.added_on, "yyyy년MM월dd일_HH시mm분ss초");
            mLocTime.add(tdate.getTime());
        }
    }

    public static ArrayList<myActivity> deserializeFile(File file) {
        if(file == null)  {
            Log.e(TAG,"ERR] Try to deserialize null file.....");
            return null;
        }

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
        return list;
    }

    public static void deserializeIntoMap(Context ctx, File file, GoogleMap gmap, boolean mode_append) {
        if(file == null)  {
            Log.e(TAG, "No File to deserialized");
            return;
        } else Log.e(TAG, "" + file.getAbsolutePath() + " to be deserialzed");

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
                    ex.printStackTrace();
                    Log.e(TAG, ex.toString());
                    break;
                }
            } while(ma != null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
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

        Log.e(TAG, "# of Activities: " + list.size() );

        if(!mode_append) {
            mActivityList.clear();
            markers.clear();
            gmap.clear();
        }


        for(int i=0;i<list.size();i++) {
            myActivity ma = (myActivity)list.get(i);
            mActivityList.add(ma);
        }

        //Log.e(TAG, "Before drawMarkers()");

        drawTrack(gmap, mActivityList);
        drawMarkers(ctx, gmap, mActivityList);

        //Log.e(TAG, "After drawMarkers()");
    }

    public static  void showActivityAlertDialog_2(Context ctx, File file, int index) {
        final Dialog dialog = new Dialog(ctx);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        /////make map clear
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.setContentView(R.layout.dialogmap2);////your custom content

        MapView mMapView = (MapView) dialog.findViewById(R.id.mapView);
        MapsInitializer.initialize(ctx);

        mMapView.onCreate(dialog.onSaveInstanceState());
        mMapView.onResume();

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                LatLng posisiabsen = new LatLng(40.626401, 22.948352); ////your lat lng
                googleMap.addMarker(new MarkerOptions().position(posisiabsen).title("Yout title"));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(posisiabsen));
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(myzoom), 2000, null);
            }
        });
        dialog.show();
    }

    public static int interval = 100;  // 10개에 1개 마크를 기록함.
    public static void moveLast(GoogleMap gmap, ArrayList<myActivity> list) {
        // 마지막 위치에 대해서 이동함.
        myActivity last = list.get(list.size()-1);
        LatLng lastll = new LatLng(last.latitude, last.longitude);
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastll, myzoom));

        CameraPosition cameraPosition = new CameraPosition.Builder().target(lastll).zoom(myzoom).build();
        gmap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public static void drawTrack(GoogleMap gmap, ArrayList<myActivity> list) {
        ArrayList<LatLng> l = new ArrayList<>();
        for(int i=0; i<list.size();i++) {
            l.add(new LatLng(list.get(i).latitude, list.get(i).longitude));
        }

        PolylineOptions plo = new PolylineOptions();
        plo.color(Color.RED);
        Polyline line = gmap.addPolyline(plo);
        line.setWidth(20);
        line.setPoints(l);
    }

    public static double  getTotalDistanceKm(ArrayList<myActivity> list) {
        double dist_meter = getTotalDistanceDouble(list);
        double dist_kilo = dist_meter / 1000f;
        return dist_kilo;
    }

    public static double getMinPerKm(Date start, Date end, double km) {
        long dur_sec = (end.getTime() - start.getTime())/1000;
        long dur_min = dur_sec/60;

        double minpk = (double)(dur_min / km);
        return minpk;
    }

    public static double getMinPerKm(long start, long end, double km) {
        long dur_sec = (end-start) /1000;
        long dur_min = dur_sec/60;

        double minpk = (double)(dur_min / km);
        return minpk;
    }



    public static double getTotalDistanceDouble(ArrayList<myActivity> list) {
        if(list == null) return 0;
        if(list.size() ==2) return 0;

        double dist_meter = 0;
        for(int i=0; i<list.size()-1; i++) {
            double bef_lat = list.get(i).latitude;
            double bef_lon = list.get(i).longitude;
            double aft_lat = list.get(i+1).latitude;
            double aft_lon = list.get(i+1).longitude;

            CalDistance cd = new CalDistance(bef_lat, bef_lon, aft_lat, aft_lon);
            double dist_2 = cd.getDistance();
            if(Double.isNaN(dist_2)) {
                Log.e(TAG, "Double.NaN between ("+bef_lat + ","+ bef_lon +") ~ ("+ aft_lat + ","+ aft_lon + ")" ) ;
                continue;
            } else if ( Double.isNaN(dist_meter + dist_2)) {
                Log.e(TAG, "Double.NaN between ("+bef_lat + ","+ bef_lon +") ~ ("+ aft_lat + ","+ aft_lon + ")" ) ;
                continue;
            }
            dist_meter = dist_meter + dist_2;
            //Log.e(TAG, "" + i + "]" +  list.get(i).added_on + dist_2 + " sum: " + dist_meter +  " ("+bef_lat + ","+ bef_lon +") ~ ("+ aft_lat + ","+ aft_lon + ")");
            //Log.e(TAG, "" + dist_2 + " sum: " + dist_meter);
        }
        return dist_meter;
    }



    public static ActivityStat getActivityStat(ArrayList <myActivity> list) {
        if(list == null) return null;
        if(list.size() ==2) return null;

        myActivity start, stop;
        start = list.get(0);
        stop = list.get(list.size()-1);

        Date start_date, stop_date;
        start_date = StringUtil.StringToDate(start.added_on,"yyyy년MM월dd일_HH시mm분ss초"); // <-
        stop_date = StringUtil.StringToDate(stop.added_on,"yyyy년MM월dd일_HH시mm분ss초");  // <-

        Log.e(TAG, "출발점:" + start.toString());
        Log.e(TAG, "종착점:" + stop.toString());

        String duration = StringUtil.Duration(start_date, stop_date); // <-
        Log.e(TAG, duration);

        double total_distM = getTotalDistanceDouble(list);  // <-
        double total_distKm = total_distM / 1000f;
        double minpk = getMinPerKm(start_date, stop_date, total_distKm); // <-

        ActivityStat as = new ActivityStat(start_date, stop_date, duration, total_distM, total_distKm, minpk, 0);
        return as;
    }


    public static String getStartTime(ArrayList<myActivity> list) {
        if(list == null) return null;
        if(list.size()==0) return null;

        Date date = StringUtil.StringToDate(list.get(0).added_on, "yyyy년MM월dd일_HH시mm분ss초");
        String date_str = StringUtil.DateToString1(date, "M월 d일 (E) H시 m분");
        return date_str;
    }

    public static String getEndTime(ArrayList<myActivity> list) {
        if(list == null) return null;
        if(list.size()-1 <0) return null;

        Date date = StringUtil.StringToDate(list.get(list.size()-1).added_on, "yyyy년MM월dd일_HH시mm분ss초");
        String date_str = StringUtil.DateToString1(date, "M월 d일 (E) H시 m분");
        return date_str;
    }

    public static void drawMarkers(Context ctx, GoogleMap gmap, ArrayList<myActivity> list) {
        double tot_distance = getTotalDistanceDouble(list);

        int disunit = 1000;
        String unitstr = "미터";
        if (tot_distance > 1000) {  // 1km 이상
            disunit = 1000;
            unitstr = "킬로";
        } else disunit = 100;

        double t_distance = 0;
        double t_lap = disunit;
        for(int i=0; i < list.size(); i++) {
            LatLng ll = new LatLng(list.get(i).latitude, list.get(i).longitude);
            float color = (i==0) ?  BitmapDescriptorFactory.HUE_GREEN : ((i==list.size()-1)? BitmapDescriptorFactory.HUE_RED  :  BitmapDescriptorFactory.HUE_CYAN);

             String title = list.get(i).added_on;
            if(i==0) {
                Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title(title)
                        .icon(BitmapDescriptorFactory.defaultMarker(color))
                        .draggable(true)
                        .visible(true)
                        .snippet("출발"));
                markers.add(marker);
            } else if(i==list.size()-1) {
                Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title(title)
                        .icon(BitmapDescriptorFactory.defaultMarker(color))
                        .draggable(true)
                        .visible(true)
                        .snippet("종료"));
                markers.add(marker);
            }
            else {
                CalDistance cd = new CalDistance(list.get(i-1).latitude, list.get(i-1).longitude, list.get(i).latitude, list.get(i).longitude);
                double dist = cd.getDistance();
                if(Double.isNaN(dist)) continue;
                if(Double.isNaN(dist + t_distance)) continue;

                t_distance = t_distance + dist;
                if(t_distance > t_lap) {
                    int interval = (int)(t_distance / disunit);
                    //Log.e(TAG, "" + interval + unitstr);
                    t_lap += disunit;


                    Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title(title)
                            .icon(BitmapDescriptorFactory.defaultMarker(color))
                            .draggable(true)
                            .visible(true)
                            .snippet(""+interval + unitstr));
                    markers.add(marker);
                }
            }
        }

        LatLngBounds.Builder builder= new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        int width = ctx.getResources().getDisplayMetrics().widthPixels;
        int height = ctx.getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen

        Log.e(TAG, "newLatLngBounds(bounds):" + bounds);
        Log.e(TAG, "newLatLngBounds(padding):" + padding);

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        gmap.moveCamera(cu);



    }

    public static int position = 0;
    public static  void showActivityAlertDialog(Context ctx, File file, int index) {
        position = index;
        final Context _ctx = ctx;
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctx);
        final File _file = file;
        alertDialog.setView(R.layout.dialogmap2);

        alertDialog.setNegativeButton("Back",null);
        final AlertDialog alert = alertDialog.create();
        alert.show();

        final MapView mMapView = (MapView) alert.findViewById(R.id.mapView);
        MapsInitializer.initialize(ctx);

        mMapView.onCreate(alert.onSaveInstanceState());
        mMapView.onResume();



        mMapView.getMapAsync(new OnMapReadyCallback() {
            final TextView tv_cursor = (TextView) alert.findViewById(R.id.tv_cursor);
            final TextView tv_heading = (TextView) alert.findViewById(R.id.tv_heading);
            final ImageButton imbt_prev = (ImageButton) alert.findViewById(R.id.imbt_prev);
            final ImageButton imbt_next = (ImageButton) alert.findViewById(R.id.imbt_next);
            final TextView tv_distance = (TextView)alert.findViewById(R.id.tv_distance);
            final TextView tv_duration = (TextView)alert.findViewById(R.id.tv_duration);
            final TextView tv_minperkm = (TextView)alert.findViewById(R.id.tv_minperkm);
            final TextView tv_carolies = (TextView)alert.findViewById(R.id.tv_carolies);
            final TextView tv_address = (TextView)alert.findViewById(R.id.tv_address);

            final File flist[] = getFiles();

            public void GO(final GoogleMap googleMap, File myfile) {
                deserializeIntoMap(_ctx, myfile, googleMap, false);
                ActivityStat activityStat = getActivityStat(mActivityList);

                Geocoder geocoder = new Geocoder(_ctx, Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(mActivityList.get(0).latitude, mActivityList.get(0).longitude,1);
                }catch(Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                }

                String addinfo = null;
                if(addresses == null || addresses.size() ==0) {
                    Log.e(TAG, "No Addresses found !!");
                }else {
                    addinfo = addresses.get(0).getAddressLine(0).toString();
                }

                String inx_str = "" + (position+1)  + "/" + flist.length + "\n";
                tv_cursor.setText(inx_str);

                String date_str = getStartTime(mActivityList);
                if(activityStat !=null) {

                    String _minDist = String.format("%.2f", activityStat.distanceKm);
                    //String sinfo = "\n " + date_str + "\n  (" + _minDist + "Km)";
                    String sinfo = "\n " + date_str;

                    tv_heading.setText(sinfo);
                    tv_address.setText(addinfo);
                    tv_distance.setText(_minDist);
                    tv_duration.setText(activityStat.duration);
                    tv_minperkm.setText(String.format("  %.2f",activityStat.minperKm));
                    tv_carolies.setText("   " + activityStat.calories);
                } else {
                    Toast.makeText(alert.getContext(), "ERR: No Statistics Information !", Toast.LENGTH_LONG).show();
                    String _minDist = String.format("-");
                    String sinfo = "\n " + date_str + "\n  (" + _minDist + "Km)";
                    tv_heading.setText(sinfo);
                    tv_distance.setText(_minDist);
                    tv_duration.setText("-");
                    tv_minperkm.setText("-");
                    tv_carolies.setText("-");
                }
            }

            @Override
            public void onMapReady(final GoogleMap googleMap) {
                GO(googleMap, _file);

                imbt_prev.setOnClickListener(new View.OnClickListener(){
                    public void onClick (View view) {
                        if (position > 0 && position < flist.length) {
                            position--;
                            GO(googleMap, flist[position]);
                        }
                    }
                });

                imbt_next.setOnClickListener(new View.OnClickListener(){
                    public void onClick (View view) {
                        File flist[] = getFiles();
                        if (position >= 0 && position < flist.length-1) {
                            position++;
                            GO(googleMap, flist[position]);
                        }
                    }
                });
            }
        });
        alert.show();
    }


}
