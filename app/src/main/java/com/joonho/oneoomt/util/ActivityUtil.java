package com.joonho.oneoomt.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import com.joonho.oneoomt.R;
import com.joonho.oneoomt.RunningActivity;
import com.joonho.oneoomt.file.myActivity;
import com.joonho.oneoomt.file.myPicture;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import static com.joonho.oneoomt.RunningActivity.mCurLoc;
import static com.joonho.oneoomt.RunningActivity.mLatLngList;
import static com.joonho.oneoomt.RunningActivity.mLocTime;

/**
 * Created by user on 2017-10-25.
 */

public class ActivityUtil {
    public static File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "OneOOMT");
    public static String TAG = "ActivityUtil";
    public static ArrayList<myActivity> mActivityList = new ArrayList<myActivity>();
    public static ArrayList<Marker> markers = new ArrayList<Marker>();

    public static void serialize(String fileName) {
        if(!mediaStorageDir.exists()) mediaStorageDir.mkdirs();
        File file = new File(mediaStorageDir, fileName);

        Log.e(TAG, "ActivityFileName to be written: " + file.toString());

        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream out = new ObjectOutputStream(bos);

            for(int i=0;i<mLatLngList.size();i++) {
                Date t_date = new Date((long)mLocTime.get(i));
                String addon = StringUtil.DateToString1(t_date, "yyyy년MM월dd일_HH시mm분ss초");
                myActivity ma = new myActivity(mLatLngList.get(i).latitude, mLatLngList.get(i).longitude, addon);
                out.writeObject(ma);
            }
            out.close();
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }

    public static File[] getFiles() {
        FilenameFilter fnf = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().endsWith(".ser");
            }
        };

        File[] flist  = mediaStorageDir.listFiles(fnf);
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
        if(file == null)  return;

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
                    if(list != null) return;
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

        if(mode_append) {
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
            gmap.clear();
        }


        for(int i=0;i<list.size();i++) {
            myActivity ma = (myActivity)list.get(i);
            mActivityList.add(ma);
        }

        Log.e(TAG, "Before drawMarkers()");
        drawMarkers(ctx, gmap, mActivityList);
        drawTrack(gmap, mActivityList);
        Log.e(TAG, "After drawMarkers()");
    }

    public static  void showActivityAlertDialog_2(Context ctx, File file, int index) {
        final Dialog dialog = new Dialog(ctx);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        /////make map clear
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.setContentView(R.layout.dialogmap);////your custom content

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
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
            }
        });

        dialog.show();
    }

    public static int interval = 100;  // 10개에 1개 마크를 기록함.
    public static void moveLast(GoogleMap gmap, ArrayList<myActivity> list) {
        // 마지막 위치에 대해서 이동함.
        myActivity last = list.get(list.size()-1);
        LatLng lastll = new LatLng(last.latitude, last.longitude);
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastll, 10));

        CameraPosition cameraPosition = new CameraPosition.Builder().target(lastll).zoom(10).build();
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


    public static void drawMarkers(Context ctx, GoogleMap gmap, ArrayList<myActivity> list) {
        for(int i=0; i < list.size(); i++) {
            LatLng ll = new LatLng(list.get(i).latitude, list.get(i).longitude);
            float color = (i==0) ?  BitmapDescriptorFactory.HUE_GREEN : ((i==list.size()-1)? BitmapDescriptorFactory.HUE_RED  :  BitmapDescriptorFactory.HUE_CYAN);
            String title = list.get(i).added_on;
            Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title(title)
                        .icon(BitmapDescriptorFactory.defaultMarker(color))
                        .draggable(true)
                        .visible(true)
                        .snippet("위도: " + ll.latitude + "경도: " + ll.longitude));
            markers.add(marker);
        }

        LatLngBounds.Builder builder= new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();

        int width = ctx.getResources().getDisplayMetrics().widthPixels;
        int height = ctx.getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.10); // offset from edges of the map 10% of screen

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        gmap.moveCamera(cu);
    }

    public static int position = 0;
    public static  void showActivityAlertDialog(Context ctx, File file, int index) {
        position = index;
        final Context _ctx = ctx;
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctx);
        final File _file = file;
        alertDialog.setView(R.layout.dialogmap);

        alertDialog.setNegativeButton("Back",null);
        final AlertDialog alert = alertDialog.create();
        alert.show();

        MapView mMapView = (MapView) alert.findViewById(R.id.mapView);
        MapsInitializer.initialize(ctx);

        mMapView.onCreate(alert.onSaveInstanceState());
        mMapView.onResume();

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                deserializeIntoMap(_ctx, _file, googleMap, false);
                ImageButton imbt_prev = (ImageButton) alert.findViewById(R.id.imbt_prev);
                ImageButton imbt_next = (ImageButton) alert.findViewById(R.id.imbt_next);
                final TextView tv_cursor = (TextView) alert.findViewById(R.id.tv_cursor);
                final TextView tv_heading = (TextView) alert.findViewById(R.id.tv_heading);


                final File flist[] = getFiles();
                String inx_str = "" + (position+1)  + "/" + flist.length;
                tv_cursor.setText(inx_str);

                Date date = new Date(_file.lastModified());
                String date_str = StringUtil.DateToString1(date, "MM월 dd일 HH시");
                String _minDist = "";
                String sinfo = "\n " + date_str + "\n  (" + _minDist + " 거리)";
                tv_heading.setText(sinfo);

                imbt_prev.setOnClickListener(new View.OnClickListener(){
                    public void onClick (View view) {
                        if (position > 0 && position < flist.length) {
                            position--;
                            final File myfile = flist[position];
                            deserializeIntoMap(_ctx, myfile, googleMap, false);
                            String inx_str = "" + (position+1)  + "/" + flist.length;
                            tv_cursor.setText(inx_str);
                        }
                    }
                });

                imbt_next.setOnClickListener(new View.OnClickListener(){
                    public void onClick (View view) {
                        File flist[] = getFiles();
                        if (position >= 0 && position < flist.length-1) {
                            position++;
                            final File myfile = flist[position];
                            deserializeIntoMap(_ctx, myfile, googleMap, false);
                            String inx_str = "" + (position+1)  + "/" + flist.length;
                            tv_cursor.setText(inx_str);
                        }
                    }
                });
            }
        });
        alert.show();
    }


}
