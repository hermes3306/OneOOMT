package com.joonho.runme;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.joonho.runme.util.ActivityStat;
import com.joonho.runme.util.ActivityUtil;
import com.joonho.runme.util.MyActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CurActivity extends AppCompatActivity {
    public static String TAG = "CurActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cur);

        Intent intent = getIntent();
        final ArrayList<MyActivity> mActivityList = (ArrayList<MyActivity>)intent.getSerializableExtra("locations");

        final Context _ctx = this;
        final MapView mMapView = (MapView) findViewById(R.id.mapView);
        MapsInitializer.initialize(this);

        mMapView.onCreate(savedInstanceState);  // check required ....
        mMapView.onResume();

        mMapView.getMapAsync(new OnMapReadyCallback() {
            final TextView tv_cursor = (TextView) findViewById(R.id.tv_cursor);
            final TextView tv_heading = (TextView) findViewById(R.id.tv_heading);
            final ImageButton imbt_prev = (ImageButton) findViewById(R.id.imbt_prev);
            final ImageButton imbt_next = (ImageButton) findViewById(R.id.imbt_next);
            final TextView tv_distance = (TextView) findViewById(R.id.tv_distance);
            final TextView tv_duration = (TextView) findViewById(R.id.tv_duration);
            final TextView tv_minperkm = (TextView) findViewById(R.id.tv_minperkm);
            final TextView tv_carolies = (TextView) findViewById(R.id.tv_carolies);
            final TextView tv_address = (TextView) findViewById(R.id.tv_address);

            public void GO(final GoogleMap googleMap) {
                imbt_prev.setVisibility(View.INVISIBLE);
                imbt_next.setVisibility(View.INVISIBLE);

                Display display = getWindowManager().getDefaultDisplay();
                DisplayMetrics metrics = new DisplayMetrics();
                display.getMetrics( metrics );
                int width = metrics.widthPixels;
                int height = metrics.heightPixels;

                drawGoogleMap(mActivityList, googleMap,width,height,false);
                ActivityStat activityStat = ActivityUtil.getActivityStat(mActivityList);

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

                String inx_str = "Total " + mActivityList.size() + " locations";
                tv_cursor.setText(inx_str);

                String date_str = ActivityUtil.getStartTime(mActivityList);
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
                    Toast.makeText(getApplicationContext(), "ERR: No Statistics Information !", Toast.LENGTH_LONG).show();
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
                GO(googleMap);

                imbt_prev.setOnClickListener(new View.OnClickListener(){
                    public void onClick (View view) {
                    }
                });

                imbt_next.setOnClickListener(new View.OnClickListener(){
                    public void onClick (View view) {
                    }
                });
            } /* on  MapReady */
        });

    }

    public static void drawGoogleMap(ArrayList<MyActivity> mList, GoogleMap gmap, int width, int height, boolean mode_append) {
        ActivityUtil.drawTrack(gmap, mList);
        ActivityUtil.drawMarkers(gmap, mList);
        ActivityUtil.doBoundBuild(gmap, width, height);
    }

}
