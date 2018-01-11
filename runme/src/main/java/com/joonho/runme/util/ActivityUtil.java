package com.joonho.runme.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.joonho.runme.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by user on 2017-10-25.
 */

public class ActivityUtil {
    public static File mediaStorageDir = null;
    public static File backupDir = null;
    public static String TAG = "ActivityUtil";
    public static ArrayList<MyActivity> mActivityList = new ArrayList<MyActivity>();
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
                        FileUtil.copyFileUsingFileStreams(_src, _tar);
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

        final ArrayList<MyActivity> amyActList = new ArrayList<MyActivity>();
        final HashMap<String, ArrayList<MyActivity>> mHashMap = new HashMap<String, ArrayList<MyActivity>>();


        new AsyncTask<Void,Void,Void>() {
            String result;
            ProgressDialog asyncDialog = new ProgressDialog(context);

            @Override
            protected void onPreExecute() {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("Loading..");
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
                        ArrayList<MyActivity> list = deserializeFile(aflist[i]);
                        if(list == null) {
                            Log.e(TAG, "File (" + aflist[i] + ") deserialzation failed !"  );
                            continue;
                        }
                        for (int j = 0; j < list.size(); j++) {
                            MyActivity ma = list.get(j);
                            //ma.added_on;
                            Date tdate = StringUtil.StringToDate(ma.added_on, "yyyy년MM월dd일_HH시mm분ss초");
                            String key = StringUtil.DateToString1(tdate, "yyyy년MM월dd일(E)");

                            if (mHashMap.containsKey(key)) {
                                ArrayList<MyActivity> daylist = mHashMap.get(key);

                                if(daylist.contains(ma)) {

                                    Log.e(TAG, "Dup Data[MA] : " + ma);
                                    continue;
                                }

                                boolean found = false;
                                for(int q=0;q<daylist.size();q++) {
                                    MyActivity qma = daylist.get(q);
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
                                ArrayList<MyActivity> daylist = new ArrayList<MyActivity>();
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
                    ArrayList<MyActivity> daylist = mHashMap.get(keyset[i]);
                    Log.e(TAG, keyset[i] + "" + daylist.size());
                    serializeActivityIntoFile(daylist, keyset[i] + ".day");
                }

                asyncDialog.dismiss();
                super.onPostExecute(result);
            }
        }.execute();
    }

    public static void serializeActivityIntoFile(ArrayList<MyActivity> list, int start, int end, String fileName) {
        if(start <0 || end >= list.size()) return;

        if(!mediaStorageDir.exists()) mediaStorageDir.mkdirs();
        File file = new File(mediaStorageDir, fileName);
        Log.e(TAG, "**** Activity file: " + file.toString());
        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream out = new ObjectOutputStream(bos);

            for(int i=start;i<= end;i++) {
                MyActivity ma = list.get(i);
                out.writeObject(ma);
            }
            out.close();
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }

    public static void serializeActivityIntoFile(ArrayList<MyActivity> list, String fileName) {
        if(list== null) return;
        if(fileName == null) return;
        if(list.size()==0) return;
        if(fileName.length() ==0 ) return;
        serializeActivityIntoFile(list,0,list.size()-1,fileName);
    }


    public static String serializeWithCurrentTime(ArrayList<MyActivity> list) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.KOREA);
        Date now = new Date();
        String fileName = formatter.format(now);
        serializeActivityIntoFile(list,fileName + ".ser");
        return fileName + ".ser";
    }


    public static ArrayList<MyActivity> Loc2Activity(ArrayList<Location> loclist) {
        ArrayList<MyActivity> mylist = new ArrayList<MyActivity>();
        Log.e(TAG,"loclist.size() = " + loclist.size());

        for(int i=0;i<loclist.size();i++ ) {

            Location loc = (Location)loclist.get(i);
            if (loc == null ) return null;

            String added_on = StringUtil.DateToString1(new Date(loc.getTime()), "yyyy년MM월dd일_HH시mm분ss초" );
            MyActivity ma = new MyActivity(loc.getLatitude(), loc.getLongitude(), loc.getAltitude(), added_on);
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
        if (flist == null) return null;

        Arrays.sort(flist, Collections.reverseOrder());
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
        Arrays.sort(flist, Collections.<File>reverseOrder());

        return flist;
    }

    public static File[] getFilesStartsWithEndWith(final String prefix, final String postfix, boolean reverseorder) {
        FilenameFilter fnf = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().startsWith(prefix) && s.toLowerCase().endsWith(postfix);
            }
        };
        File[] flist  = mediaStorageDir.listFiles(fnf);
        if(reverseorder) Arrays.sort(flist, Collections.<File>reverseOrder());
        else Arrays.sort(flist);
        return flist;
    }

    public static File[] getFilesStartsWith(final String prefix) {
        FilenameFilter fnf = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().startsWith(prefix);
            }
        };
        File[] flist  = mediaStorageDir.listFiles(fnf);
        Arrays.sort(flist, Collections.<File>reverseOrder());
        return flist;
    }

    public static File[] getFilesEndsWith(final String postfix) {
        FilenameFilter fnf = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().endsWith(postfix);
            }
        };
        File[] flist  = mediaStorageDir.listFiles(fnf);
        Arrays.sort(flist, Collections.<File>reverseOrder());
        return flist;
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


    public static ArrayList<MyActivity> deserializeFile(File file) {
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

            list = new ArrayList<MyActivity>();
            MyActivity ma=null;

            do {
                try {
                    ma = (MyActivity) in.readObject();
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


    public static double getTotalDistanceDouble(ArrayList<MyActivity> list) {
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


    public static String getStartTime(ArrayList<MyActivity> list) {
        if(list == null) return null;
        if(list.size()==0) return null;

        Date date = StringUtil.StringToDate(list.get(0).added_on, "yyyy년MM월dd일_HH시mm분ss초");
        String date_str = StringUtil.DateToString1(date, "M월 d일 (E) H시 m분");
        return date_str;
    }

    public static String getEndTime(ArrayList<MyActivity> list) {
        if(list == null) return null;
        if(list.size()-1 <0) return null;

        Date date = StringUtil.StringToDate(list.get(list.size()-1).added_on, "yyyy년MM월dd일_HH시mm분ss초");
        String date_str = StringUtil.DateToString1(date, "M월 d일 (E) H시 m분");
        return date_str;
    }

    public static String getTimeStr(ArrayList<MyActivity> list, int pos) {
        if(list == null) return null;
        if(list.size()-1 <0) return null;

        Date date = StringUtil.StringToDate(list.get(pos).added_on, "yyyy년MM월dd일_HH시mm분ss초");
        String date_str = StringUtil.DateToString1(date, "M월 d일 (E) H시 m분");
        return date_str;
    }

    public static Date getStartTimeDate(ArrayList<MyActivity> list) {
        if(list == null) return null;
        if(list.size()==0) return null;
        Date date = StringUtil.StringToDate(list.get(0).added_on, "yyyy년MM월dd일_HH시mm분ss초");
        return date;
    }

    public static Date getEndTimeDate(ArrayList<MyActivity> list) {
        if(list == null) return null;
        if(list.size()==0) return null;
        Date date = StringUtil.StringToDate(list.get(list.size()-1).added_on, "yyyy년MM월dd일_HH시mm분ss초");
        return date;
    }

    public static void deserializeIntoMap(Context ctx, File file, GoogleMap gmap, int width, int height, boolean mode_append) {
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

            list = new ArrayList<MyActivity>();
            MyActivity ma=null;

            do {
                try {
                    ma = (MyActivity) in.readObject();
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
            MyActivity ma = (MyActivity)list.get(i);
            mActivityList.add(ma);
        }

        //Log.e(TAG, "Before drawMarkers()");

        drawTrack(gmap, mActivityList);
        drawMarkers(gmap, mActivityList);
        doBoundBuild(gmap, width, height);

        //Log.e(TAG, "After drawMarkers()");
    }


    public static void drawTrack(GoogleMap gmap, ArrayList<MyActivity> list) {
        if(list == null) return;
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


    public static void drawMarkers(GoogleMap gmap, ArrayList<MyActivity> list) {
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
    }

    public static void doBoundBuild(GoogleMap gmap, int width, int height) {
        if(markers.size()==0) return;

        LatLngBounds.Builder builder= new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen

        boolean berr = false;
        try {
            Log.e(TAG, "newLatLngBounds(bounds):" + bounds);
            Log.e(TAG, "newLatLngBounds(padding):" + padding);

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            gmap.moveCamera(cu);
        }catch(Exception e) {
            berr = true;
            Log.e(TAG,"ERR] BoundBuild:" + e.toString());

            moveCamera(gmap, myzoom);
        }
    }

    public static void moveCamera(GoogleMap googleMap, float _zoom) {
        if(mActivityList==null) return;
        if(mActivityList.size()==0) return;

        LatLng curloc = new LatLng(mActivityList.get(mActivityList.size()-1).latitude,
                mActivityList.get(mActivityList.size()-1).longitude);
        myzoom = _zoom;
        CameraPosition cameraPosition = new CameraPosition.Builder().target(curloc).zoom(_zoom).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }


    public static ActivityStat getActivityStat(ArrayList <MyActivity> list) {
        if(list == null) return null;
        if(list.size() < 2) return null;

        MyActivity start, stop;
        start = list.get(0);
        stop = list.get(list.size()-1);

        Date start_date, stop_date;
        start_date = StringUtil.StringToDate(start.added_on,"yyyy년MM월dd일_HH시mm분ss초"); // <-
        stop_date = StringUtil.StringToDate(stop.added_on,"yyyy년MM월dd일_HH시mm분ss초");  // <-

        Log.e(TAG, "출발:" + start.toString());
        Log.e(TAG, "종료:" + stop.toString());

        String duration = StringUtil.Duration(start_date, stop_date); // <-
        Log.e(TAG, duration);

        double total_distM = getTotalDistanceDouble(list);  // <-
        double total_distKm = total_distM / 1000f;
        double minpk = getMinPerKm(start_date, stop_date, total_distKm); // <-

        ActivityStat as = new ActivityStat(start_date, stop_date, duration, total_distM, total_distKm, minpk, 0);
        return as;
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
                int width = _ctx.getResources().getDisplayMetrics().widthPixels;
                int height = _ctx.getResources().getDisplayMetrics().heightPixels;

                deserializeIntoMap(_ctx, myfile, googleMap, width, height, false);
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


    public static void upgrade(File f) {
        //f.delete();

        ArrayList<MyActivity> list = deserializeFile(f);
        if(list == null) { // old version check required.
            Log.e(TAG,"" + f.getName() + " deserialize() failed");
        }
        else { // pass
            Log.e(TAG, "" + f.getName() + " deserialize() success");
        }
    }

    public static Date getActivityTime(MyActivity ma) {
        if(ma==null) return null;
        Date date = StringUtil.StringToDate(ma.added_on, "yyyy년MM월dd일_HH시mm분ss초");
        return date;
    }

    public static final Comparator<MyActivity> ALPHA_COMPARATOR  = new Comparator<MyActivity>() {
        private final Collator sCollator = Collator.getInstance();
        public int compare(MyActivity object1, MyActivity object2) {
            return sCollator.compare(object1.added_on, object2.added_on);
        }
    };


    public static boolean isSameActivity(MyActivity a1, MyActivity a2) {
        if(a1 == null || a2 == null) return false;
        if(a1.latitude == a2.latitude && a1.longitude == a2.longitude &&
                a1.added_on.equalsIgnoreCase(a2.added_on)) return true;
        return false;
    }

    public static void dododo() {
        Date today = new Date();
        Date day = today;
        String dododo_str = StringUtil.DateToString1(today, "yyyy_MM_dd");
        int cnt=0;

        do {
            Log.e(TAG, ">>>>>>>>>>>>>>>>>> " + String.format("%2d일전:",cnt)  + dododo_str + " >>>>>>>>   START ");
            dododo(dododo_str);
            day.setTime(day.getTime() - (1000 * 60 * 60 * 24));
            dododo_str = StringUtil.DateToString1(day, "yyyy_MM_dd");
            Log.e(TAG, ">>>>>>>>>>>>>>>>>> " + dododo_str + " >>>>>>>>   END \n\n\n");
            cnt++;
        } while(cnt < 60); // 2개월 동안
    }

    public static void dododo_tmp() {
        File dflist[] = getFilesStartsWithEndWith("201801",".ser",true);
        for(int i=0;i<dflist.length;i++) dflist[i].delete();



        dododo("2017_12_09");
        dododo("2017_12_08");
        dododo("2017_12_07");
        dododo("2017_12_06");
        dododo("2017_12_05");
    }

    public static void dododo(String day) {  // fmt: 2018_01_01
        File files[] = getFilesStartsWithEndWith(day, ".ser", false);
        ArrayList<String> fnamelist = dododo(files);
        for(int i=0;i<files.length;i++) {
            if(fnamelist.contains(files[i].getName() )) continue;
            else {
                Log.e(TAG, "*** removed : " + files[i].getName());
                files[i].delete();
            }
        }
        Log.e(TAG, "\n\n\n\n Done. ["+ day +"]");
    }

    public static void dododo_trash() {
        File files[] = getFilesStartsWithEndWith("2018_01_11", ".ser", false);
        File trash[] = getFilesStartsWithEndWith("2018_01_11", ".trash", false);

        File allfile[] = new File[files.length + trash.length];
        for(int i=0;i<files.length;i++) allfile[i] = files[i];

        int j=0;
        for(int i=files.length;i<allfile.length;i++)
        {
            allfile[i] = trash[j];
            j++;
        }

        ArrayList<String> fnamelist = dododo(allfile);

        for(int i=0;i<allfile.length;i++) {
            if(fnamelist.contains(allfile[i].getName() )) continue;
            else {
                Log.e(TAG, "*** removed : " + allfile[i].getName());
                allfile[i].delete();
            }
        }
    }

    public static ArrayList<String>  dododo(File files[]) {
        Log.e(TAG, "dododo");
        ArrayList<MyActivity> _all_list = new ArrayList<MyActivity>();

        for(int i=0;i<files.length;i++) {
            Log.e(TAG, "*** deserialize: " + files[i].getName());
            //if(files[i].getName().contains("(F)")) continue;
            ArrayList<MyActivity> _list = deserializeFile(files[i]);
            _all_list.addAll(_list);
            Log.e(TAG, "*** # of _all_list:" + _all_list.size());
        }

        Collections.sort(_all_list,ALPHA_COMPARATOR);
        MyActivity pma = null;

        int rmv_cnt = 0;
        for(int i=_all_list.size()-1;i>0;i--) {
            MyActivity ma = _all_list.get(i);
            if(i>0) pma = _all_list.get(i-1);
            if(isSameActivity(ma, pma)) Log.e(TAG, "" + String.format("%04d",i) + ":" + ma.added_on + "," + ma.latitude + " [S]");
            else  Log.e(TAG, "" + String.format("%04d",i) + ":" + ma.added_on + "," + ma.latitude + " [X]");
            if(isSameActivity(ma, pma)) {
                _all_list.remove(i);
                rmv_cnt++;
            }
        }
        Log.e(TAG, "**** # of Duplicates(removed): " + rmv_cnt);
        Log.e(TAG, "**** # of Remains: " + _all_list.size());


        String _cur_date = null;
        int startpos = 0;

        // By adding dummy Activity for tomorrow....
        Date tomorrow = new Date ( new Date().getTime ( ) + (long) ( 1000 * 60 * 60 * 24 ) );
        String _added_on = StringUtil.DateToString1(tomorrow, "yyyy년MM월dd일_HH시mm분ss초" );
        _all_list.add(new MyActivity(-1,-1, -1,_added_on));   // dummy Activity


        ArrayList <String> fnamelist = new ArrayList<>();

        for(int i=0;i<_all_list.size();i++) {
            MyActivity ma = _all_list.get(i);
            Date _date = getActivityTime(ma);

            if(i==0) _cur_date = StringUtil.DateToString1(_date,"yyyyMMdd");
            else {
                String t_str = StringUtil.DateToString1(_date,"yyyyMMdd");
                if(_cur_date.equalsIgnoreCase(t_str)) {
                    // pass
                }else {
                    // serialize the previous list;
                    MyActivity _pma = _all_list.get(i-1);
                    String _fname = StringUtil.DateToString1(getActivityTime(_pma), Config._filename_fmt) + "(F)" + Config._default_ext;
                    Log.e(TAG,"*" + _cur_date);
                    serializeActivityIntoFile(_all_list, startpos, i-1, _fname );
                    fnamelist.add(_fname);

                    Log.e(TAG,"**** " + _fname + "created successfully!" );
                    Log.e(TAG,"**** " + "from:" + startpos);
                    Log.e(TAG,"**** " + "to:" + (i-1));
                    Log.e(TAG,"\n\n");

                    _cur_date = t_str;
                    startpos=i;
                }
            }
        }

        return fnamelist;
//        if(Config._trash_after_dododo) {
//            for (int i = 0; i < files.length; i++) {
//                if(files[i].getName().contains("(F)")) continue;
//                File f = new File(mediaStorageDir, files[i].getName() + ".trash");
//                files[i].renameTo(f);
//                Log.e(TAG, "**** TRASH " + f .getName()+ "!");
//            }
//        } else {
//            for (int i = 0; i < files.length; i++) {
//                if(files[i].getName().contains("(F)")) continue;
//                files[i].delete();
//                Log.e(TAG, "**** DELETE " + files[i].getName() + " deleted!");
//            }
//        }
    }
}

