package com.joonho.runme;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
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
import com.joonho.runme.util.MapUtil;
import com.joonho.runme.util.MyActivity;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

public class CloudFileActivity extends AppCompatActivity {
    public static String TAG = "ActFileActivity";
    public static int position = 0;
    public static ArrayList<MyActivity> mActivityList = new ArrayList<MyActivity>();
    public static String add1 = null;
    public static String add2 = null;
    public static boolean tog_add = true;
    public static String fnames[] = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_file);

        Intent intent = getIntent();
        fnames = intent.getExtras().getStringArray("files");
        position = intent.getExtras().getInt("pos");

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

            public void GO(final GoogleMap googleMap, URL url) {
                Display display = getWindowManager().getDefaultDisplay();
                DisplayMetrics metrics = new DisplayMetrics();
                display.getMetrics( metrics );
                int width = metrics.widthPixels;
                int height = metrics.heightPixels;

                //ActivityUtil.deserializeIntoMap(_ctx, url, googleMap, width, height,false);
                mActivityList = ActivityUtil.mActivityList;

                ActivityStat activityStat = ActivityUtil.getActivityStat(mActivityList);

                if(mActivityList.size()>1) {
                    add1 = MapUtil.getAddress(_ctx, mActivityList.get(0));
                    add2 = MapUtil.getAddress(_ctx, mActivityList.get(mActivityList.size()-1));
                }

                String inx_str = "\n" + (position+1)  + "/" + fnames.length + "\n" + "Total " + mActivityList.size() + " locations";
                tv_cursor.setText(inx_str);

                String date_str = ActivityUtil.getStartTime(mActivityList);
                if(activityStat !=null) {

                    String _minDist = String.format("%.2f", activityStat.distanceKm);
                    //String sinfo = "\n " + date_str + "\n  (" + _minDist + "Km)";
                    String sinfo = "\n " + date_str;

                    tv_heading.setText(sinfo);
                    tv_address.setText("From:" +  add1);
                    tv_address.setTextColor(Color.GREEN);

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
//                GO(googleMap, _file);
//
//                imbt_prev.setOnClickListener(new View.OnClickListener(){
//                    public void onClick (View view) {
//                        if (position > 0 && position < flist.length) {
//                            position--;
//                            GO(googleMap, flist[position]);
//                        }
//                    }
//                });
//
//                imbt_next.setOnClickListener(new View.OnClickListener(){
//                    public void onClick (View view) {
//                        File flist[] = ActivityUtil.getFiles();
//                        if (position >= 0 && position < flist.length-1) {
//                            position++;
//                            GO(googleMap, flist[position]);
//                        }
//                    }
//                });
//
//                tv_address.setOnClickListener(new View.OnClickListener(){
//                    public void onClick(View view) {
//                        Log.e(TAG, "address clocked !!");
//                        if(tog_add) {
//                            tv_address.setText("To:" +  add2);
//                            tv_address.setTextColor(Color.RED);
//                            Log.e(TAG, "To: " + add2);
//                            tog_add = false;
//                        } else {
//                            tv_address.setText("From:" +  add1);
//                            tv_address.setTextColor(Color.GREEN);
//                            Log.e(TAG, "From: " + add1);
//                            tog_add = true;
//                        }
//                    }
//                });
            } /* on  MapReady */
        });
    } /* onCreate */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_act_file, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.delete:
                File flist[] = ActivityUtil.getFiles();
                flist[position].delete();
                Toast.makeText(getApplicationContext(),"" + flist[position] + " deleted!!!", Toast.LENGTH_LONG).show();
                finish();
                return true;
            case R.id.reload:

                Intent resultIntent = new Intent();
                resultIntent.putExtra("locations",mActivityList);
                //resultIntent.putExtra("fname", fname);
                setResult(Main2Activity.REQUEST_ACTIVITY_FILE_LIST,resultIntent );
                finish();
                return true;

            case R.id.share:
                return true;
            default:
                return true;
        }
    }
}



