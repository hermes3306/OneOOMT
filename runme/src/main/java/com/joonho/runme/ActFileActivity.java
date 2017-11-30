package com.joonho.runme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActFileActivity extends AppCompatActivity {
    public static String TAG = "ActFileActivity";
    public static int position = 0;
    public static ArrayList<MyActivity> mActivityList = new ArrayList<MyActivity>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_file);

        Intent intent = getIntent();
        String fname = intent.getExtras().getString("file");
        position = intent.getExtras().getInt("pos");

        final Context _ctx = this;
        final File _file = new File(fname);

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

            final File flist[] = ActivityUtil.getFiles();

            public void GO(final GoogleMap googleMap, File myfile) {

                Display display = getWindowManager().getDefaultDisplay();
                DisplayMetrics metrics = new DisplayMetrics();
                display.getMetrics( metrics );
                int width = metrics.widthPixels;
                int height = metrics.heightPixels;

                ActivityUtil.deserializeIntoMap(_ctx, myfile, googleMap, width, height,false);
                mActivityList = ActivityUtil.mActivityList;

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

                String inx_str = "" + (position+1)  + "/" + flist.length + "\n";
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
                        File flist[] = ActivityUtil.getFiles();
                        if (position >= 0 && position < flist.length-1) {
                            position++;
                            GO(googleMap, flist[position]);
                        }
                    }
                });
            } /* on  MapReady */
        });
    } /* onCreate */


}



