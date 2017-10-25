package com.joonho.oneoomt.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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

    public static  void showActivityAlertDialog(Context ctx, File file, int index) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctx);
        //alertDialog.setView(ll);
        alertDialog.setView(R.layout.actview);


        alertDialog.setNegativeButton("Back",null);
        AlertDialog alert = alertDialog.create();
        alert.show();
    }
//
//    public void hotelBooking(Context context){
//        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
//        alertDialog.setTitle("Hotel Info");
//        LayoutInflater layoutInflater = LayoutInflater.from(context);
//        View promptView = layoutInflater.inflate(R.layout.traveler_hotel_registration, null);
//        alertDialog.setView(promptView);
//
//        MapView mMapView = (MapView) alertDialog.findViewById(R.id.mapView2);
//        MapsInitializer.initialize(this);
//
//        mMapView.onCreate(alertDialog.onSaveInstanceState());
//        mMapView.onResume();
//
//
//        mMapView.getMapAsync(new OnMapReadyCallback() {
//            @Override
//            public void onMapReady(final GoogleMap googleMap) {
//                LatLng posisiabsen = new LatLng(40.626401, 22.948352); ////your lat lng
//                googleMap.addMarker(new MarkerOptions().position(posisiabsen).title(hotelname));
//                googleMap.moveCamera(CameraUpdateFactory.newLatLng(posisiabsen));
//                googleMap.getUiSettings().setZoomControlsEnabled(true);
//                googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
//            }
//        });
//
//        final RatingBar rb = (RatingBar) promptView.findViewById(R.id.ratingBar);
//        rb.setRating(3);
//
//        final TextView hoteltitle= (TextView)promptView.findViewById(R.id.HotelInfoTitle);
//        hoteltitle.setText("Hotel " + hotelname);
//
//        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Favorite", new DialogInterface.OnClickListener()
//        {
//            public void onClick(DialogInterface dialog, int which)
//            {
//                Toast.makeText(getApplicationContext(), "Hotel has been saved to favorites!",
//                        Toast.LENGTH_LONG).show();
//
//            }
//
//        });
//        alertDialog.show();
//    }

}
