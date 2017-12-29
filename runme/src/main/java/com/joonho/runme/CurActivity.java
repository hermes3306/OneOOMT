package com.joonho.runme;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
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
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.joonho.runme.util.ActivityStat;
import com.joonho.runme.util.ActivityUtil;
import com.joonho.runme.util.CalDistance;
import com.joonho.runme.util.CalTime;
import com.joonho.runme.util.MapUtil;
import com.joonho.runme.util.MyActivity;
import com.joonho.runme.util.MyNotifier;
import com.joonho.runme.util.StringUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CurActivity extends AppCompatActivity {
    public static String TAG = "CurActivity";
    static ArrayList<MyActivity> mActivityList = null;
    public static ArrayList<Marker> markers = null;
    public float myzoom = 16f;
    public static Marker last_marker=null;
    public static Marker bef_last_marker=null;

    public static String add1 = null;
    public static String add2 = null;

    public static int marker_pos = 0;
    public static int marker_pos_prev =0;
    public static Polyline line_prev = null;

    public static boolean tog_add = false;
    static MapView mMapView = null;
    public static final int REQUEST_ACTIVITY_FILE_LIST = 0x0001;

    public static boolean nomarker = true;
    public static boolean notrack = false;
    public static boolean satellite = false;

    public static int navigation_gap = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cur);
        mMapView = (MapView) findViewById(R.id.mapView);

        MapsInitializer.initialize(this);
        mMapView.onCreate(savedInstanceState);  // check required ....
    }

    @Override
    protected void onStart() {
        super.onStart();

        final Context _ctx = this;
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
            final SeekBar  seekBar = (SeekBar) findViewById(R.id.seekBar);

            @Override
            public void onMapReady(final GoogleMap googleMap) {

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    public void onProgressChanged(SeekBar seekBar, int progress,
                                                  boolean fromUser) {

                        if(mActivityList == null) return;
                        marker_pos_prev = marker_pos;
                        marker_pos = seekBar.getProgress();

                        LatLng nextpos = new LatLng(mActivityList.get(marker_pos).latitude,
                                mActivityList.get(marker_pos).longitude);
                        LatLng prevpos = new LatLng(mActivityList.get(0).latitude,
                                mActivityList.get(0).longitude);

                        CalDistance cd =  new CalDistance(prevpos, nextpos);
                        double dist = cd.getDistance();

                        String diststr = null;
                        String elapsedstr=null;
                        if(dist > 1000.0f) diststr = cd.getDistanceKmStr();
                        else diststr = cd.getDistanceMStr();

                        CalTime ct = new CalTime(mActivityList.get(marker_pos_prev), mActivityList.get(marker_pos));
                        long elapsed = ct.getElapsed();

                        if(elapsed > 60*60000) elapsedstr = ct.getElapsedHourStr();
                        else if(elapsed > 60000) elapsedstr = ct.getElapsedMinStr();
                        else elapsedstr = ct.getElapsedSecStr();

                        //moveCamera(googleMap, nextpos);

                        float color = (marker_pos==0?BitmapDescriptorFactory.HUE_ROSE:BitmapDescriptorFactory.HUE_CYAN);
                        Marker marker = googleMap.addMarker(new MarkerOptions().position(nextpos).title(MapUtil.getAddressDong(_ctx, mActivityList.get(marker_pos)))
                                .icon(BitmapDescriptorFactory.defaultMarker(color))
                                .draggable(true)
                                .visible(true)
                                .snippet(elapsedstr + " ("+diststr+")"));

                        if(bef_last_marker!=null) bef_last_marker.remove();
                        if(last_marker!=null) last_marker.remove();
                        last_marker = marker;

                        marker.showInfoWindow();

                        drawTrack(googleMap,mActivityList,0,marker_pos);

                        tv_heading.setText(ActivityUtil.getTimeStr(mActivityList, marker_pos));
                        tv_address.setText(MapUtil.getAddress(_ctx, mActivityList.get(marker_pos)));
                        tv_cursor.setText("" + marker_pos + "/" + mActivityList.size());
                    }
                });

                imbt_prev.setOnClickListener(new View.OnClickListener(){
                    public void onClick (View view) {
                        if(marker_pos == 0) {
                            Toast.makeText(CurActivity.this, "Start Postion!!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Date st_date = ActivityUtil.getStartTimeDate(mActivityList);
                        Date ed_date = ActivityUtil.getEndTimeDate(mActivityList);

                        if(st_date==null || ed_date==null) return;
                        long st_date_l = st_date.getTime();
                        long ed_date_l = ed_date.getTime();
                        long duration = ed_date_l - st_date_l;

                        int dis_type = 0;  /* 0: 1시간 이상 1: 10분이상 2: 10분 이하 */

                        if(duration > 5 * 60 * 60 * 1000) { // 5시간 이상
                            dis_type = 0;
                        } else if(duration > 1 * 60 * 60 * 1000) {  // 1시간 이상
                            dis_type =1;
                        }else if(duration > 1 * 10 * 60 * 1000) { // 10분 이상
                            dis_type = 2;
                        }else {
                            dis_type=3;
                        }

                        int gap = 10;
                        if(dis_type==0) gap = 100;
                        if(dis_type==1) gap = 50;
                        if(dis_type==2) gap =10;
                        if(dis_type==3) gap =1;

                        marker_pos_prev = marker_pos;
                        marker_pos = marker_pos - gap;

                        if (marker_pos < 0)
                            marker_pos = 0;
                        LatLng nextpos = new LatLng(mActivityList.get(marker_pos).latitude,
                                mActivityList.get(marker_pos).longitude);
                        LatLng prevpos = new LatLng(mActivityList.get(marker_pos_prev).latitude,
                                mActivityList.get(marker_pos_prev).longitude);

                        CalDistance cd =  new CalDistance(prevpos, nextpos);
                        double dist = cd.getDistance();

                        String diststr = null;
                        String elapsedstr=null;
                        if(dist > 1000.0f) diststr = cd.getDistanceKmStr();
                        else diststr = cd.getDistanceMStr();

                        CalTime ct = new CalTime(mActivityList.get(marker_pos_prev), mActivityList.get(marker_pos));
                        long elapsed = ct.getElapsed();

                        if(elapsed > 60*60000) elapsedstr = ct.getElapsedHourStr();
                        else if(elapsed > 60000) elapsedstr = ct.getElapsedMinStr();
                        else elapsedstr = ct.getElapsedSecStr();

                        //moveCamera(googleMap, nextpos);

                        float color = (marker_pos==0?BitmapDescriptorFactory.HUE_ROSE:BitmapDescriptorFactory.HUE_BLUE);
                        Marker marker = googleMap.addMarker(new MarkerOptions().position(nextpos).title(MapUtil.getAddressDong(_ctx, mActivityList.get(marker_pos)))
                                .icon(BitmapDescriptorFactory.defaultMarker(color))
                                .draggable(true)
                                .visible(true)
                                .snippet(elapsedstr + " ("+diststr+")"));

                        if(bef_last_marker!=null) bef_last_marker.remove();
                        bef_last_marker = last_marker;
                        last_marker = marker;
                        if(bef_last_marker!=null) {
                            bef_last_marker.setTitle("<");
                            bef_last_marker.setTitle(">");
                            bef_last_marker.showInfoWindow();
                        }

                        marker.showInfoWindow();

                        tv_heading.setText(ActivityUtil.getTimeStr(mActivityList, marker_pos));
                        tv_address.setText(MapUtil.getAddress(_ctx, mActivityList.get(marker_pos)));
                        tv_cursor.setText("" + marker_pos + "/" + mActivityList.size());
                    }
                });

                imbt_next.setOnClickListener(new View.OnClickListener(){
                    public void onClick (View view) {
                        int l_pos = mActivityList.size()-1;
                        if(marker_pos == l_pos) {
                            Toast.makeText(CurActivity.this, "End Postion!!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Date st_date = ActivityUtil.getStartTimeDate(mActivityList);
                        Date ed_date = ActivityUtil.getEndTimeDate(mActivityList);

                        if(st_date==null || ed_date==null) return;
                        long st_date_l = st_date.getTime();
                        long ed_date_l = ed_date.getTime();
                        long duration = ed_date_l - st_date_l;

                        int dis_type = 0;  /* 0: 1시간 이상 1: 10분이상 2: 10분 이하 */

                        if(duration > 5 * 60 * 60 * 1000) { // 5시간 이상
                            dis_type = 0;
                        } else if(duration > 1 * 60 * 60 * 1000) {  // 1시간 이상
                            dis_type =1;
                        }else if(duration > 1 * 10 * 60 * 1000) { // 10분 이상
                            dis_type = 2;
                        }else {
                            dis_type=3;
                        }

                        int gap = 10;
                        if(dis_type==0) gap = 100;
                        if(dis_type==1) gap = 50;
                        if(dis_type==2) gap =10;
                        if(dis_type==3) gap =1;

                        marker_pos_prev = marker_pos;
                        marker_pos = marker_pos + gap;

                        if (marker_pos > l_pos)
                            marker_pos = l_pos;
                        LatLng nextpos = new LatLng(mActivityList.get(marker_pos).latitude,
                                mActivityList.get(marker_pos).longitude);
                        LatLng prevpos = new LatLng(mActivityList.get(marker_pos_prev).latitude,
                                mActivityList.get(marker_pos_prev).longitude);

                        CalDistance cd =  new CalDistance(prevpos, nextpos);
                        double dist = cd.getDistance();

                        String diststr = null;
                        String elapsedstr=null;
                        if(dist > 1000.0f) diststr = cd.getDistanceKmStr();
                        else diststr = cd.getDistanceMStr();

                        CalTime ct = new CalTime(mActivityList.get(marker_pos_prev), mActivityList.get(marker_pos));
                        long elapsed = ct.getElapsed();

                        if(elapsed > 60*60000) elapsedstr = ct.getElapsedHourStr();
                        else if(elapsed > 60000) elapsedstr = ct.getElapsedMinStr();
                        else elapsedstr = ct.getElapsedSecStr();

                        //moveCamera(googleMap, nextpos);

                        float color = (marker_pos==0?BitmapDescriptorFactory.HUE_VIOLET:BitmapDescriptorFactory.HUE_ROSE);
                        Marker marker = googleMap.addMarker(new MarkerOptions().position(nextpos).title(MapUtil.getAddressDong(_ctx, mActivityList.get(marker_pos)))
                                .icon(BitmapDescriptorFactory.defaultMarker(color))
                                .draggable(true)
                                .visible(true)
                                .snippet(elapsedstr + " ("+diststr+")"));

                        if(bef_last_marker!=null) bef_last_marker.remove();
                        bef_last_marker = last_marker;
                        last_marker = marker;
                        if(bef_last_marker!=null) {
                            bef_last_marker.setTitle("<");
                            bef_last_marker.setTitle(">");
                            bef_last_marker.showInfoWindow();
                        }

                        marker.showInfoWindow();

                        tv_heading.setText(ActivityUtil.getTimeStr(mActivityList, marker_pos));
                        tv_address.setText(MapUtil.getAddress(_ctx, mActivityList.get(marker_pos)));
                        tv_cursor.setText("" + marker_pos + "/" + mActivityList.size());
                    }
                });

                tv_address.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View view) {
                        myzoom = googleMap.getCameraPosition().zoom;

                        Log.e(TAG, "address clocked !!");
                        if(tog_add) {
                            tv_address.setText("To:" +  add2);
                            tv_address.setTextColor(Color.RED);
                            tv_heading.setText(ActivityUtil.getEndTime(mActivityList));
                            Log.e(TAG, "To: " + add2);
                            tog_add = false;
                            LatLng lastpos = new LatLng(mActivityList.get(mActivityList.size()-1).latitude,
                                    mActivityList.get(mActivityList.size()-1).longitude);
                            moveCamera(googleMap,lastpos);
                            marker_pos=mActivityList.size()-1;


                        } else {
                            tv_address.setText("From:" +  add1);
                            tv_address.setTextColor(Color.GREEN);
                            tv_heading.setText(ActivityUtil.getStartTime(mActivityList));
                            Log.e(TAG, "From: " + add1);
                            LatLng lastpos = new LatLng(mActivityList.get(0).latitude,
                                    mActivityList.get(0).longitude);
                            moveCamera(googleMap,lastpos);
                            tog_add = true;
                            marker_pos = 0;
                        }
                        if(bef_last_marker!=null) bef_last_marker.remove();
                        if(last_marker!=null) last_marker.remove();
                    }
                });

                tv_heading.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {


                    }
                });

                GO(googleMap);
            } /* on  MapReady */



            public void GO(final GoogleMap googleMap) {
                googleMap.clear();
                markers = new ArrayList<Marker>();

//                imbt_prev.setVisibility(View.INVISIBLE);
//                imbt_next.setVisibility(View.INVISIBLE);

                Intent intent = getIntent();
                final String fname = intent.getStringExtra("file");
                ActivityStat activityStat = null;

                if(fname != null) mActivityList = deserialize(new File(fname));
                else mActivityList = (ArrayList<MyActivity>)intent.getSerializableExtra("locations");
                if(mActivityList==null) {
                    Log.e(TAG, "No Activiies...");
                    return;
                }

                if(mActivityList.size()>1) {
                    add1 = MapUtil.getAddress(_ctx, mActivityList.get(0));
                    add2 = MapUtil.getAddress(_ctx, mActivityList.get(mActivityList.size()-1));
                    marker_pos = mActivityList.size()-1;
                    seekBar.setMax(mActivityList.size()-1);
                }

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
                activityStat= getActivityStat(mActivityList);

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

                if(!nomarker) drawMarkers(googleMap,mActivityList);
                if(!notrack) drawTrack(googleMap,mActivityList);
                if(!satellite) googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                else googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

                if(nomarker || notrack) {
                    drawStartMarker(googleMap,mActivityList);
                    drawEndMarker(googleMap,mActivityList);
                }


                Display display = getWindowManager().getDefaultDisplay();
                DisplayMetrics metrics = new DisplayMetrics();
                display.getMetrics( metrics );
                int width = metrics.widthPixels;
                int height = metrics.heightPixels;

                boolean got_bound_wo_error = false;
                int try_cnt = 0;

                do {
                    try {
                        Log.e(TAG, "Tying to get Bound with width:" + width + ", height:" + height);
                        doBoundBuild(googleMap, width, height);
                        got_bound_wo_error = true;
                    } catch (Exception e) {
                        try_cnt++;
                        Log.e(TAG, e.toString() + "Trying to get again... (try_cnt:" +try_cnt+")");
                    }
                }while(!got_bound_wo_error && try_cnt < 3);
                if(!got_bound_wo_error) { myzoom = 16; moveCamera(googleMap, myzoom); }
            }
        });
    }


    public static ActivityStat getActivityStat(ArrayList <MyActivity> list) {
        if(list == null) {
            Log.e(TAG,"Activity List null");
            return null;
        }
        if(list.size() < 2) {
            Log.e(TAG,"Activity size < 2");
            return null;
        }

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

    public static void drawTrack(GoogleMap map, ArrayList<MyActivity> list, int start, int end) {
        if(list == null) return;
        ArrayList<LatLng> l = new ArrayList<>();
        for(int i=start; i < end; i++) {
           l.add(new LatLng(list.get(i).latitude, list.get(i).longitude));
        }

        PolylineOptions plo = new PolylineOptions();
        plo.color(Color.BLACK);
        Polyline line = map.addPolyline(plo);
        line.setWidth(20);
        line.setPoints(l);

        if(line_prev!=null) line_prev.remove();
        line_prev = line;
    }

    public static void drawStartMarker(GoogleMap gmap, ArrayList<MyActivity> list) {
        if(list.size()==0) return;
        LatLng ll = new LatLng(list.get(0).latitude, list.get(0).longitude);
        Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title("출발")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .draggable(true)
                .visible(true)
                .snippet("출발"));
        markers.add(marker);
    }

    public static void drawEndMarker(GoogleMap gmap, ArrayList<MyActivity> list) {
        if(list.size()==0) return;
        LatLng ll = new LatLng(list.get(list.size()-1).latitude, list.get(list.size()-1).longitude);
        Marker marker = gmap.addMarker(new MarkerOptions().position(ll).title("종료")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .draggable(true)
                .visible(true)
                .snippet("종료"));
        markers.add(marker);
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
            if(i==0) drawStartMarker(gmap,list);
            else if(i==list.size()-1) drawEndMarker(gmap,list);
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

    public static void doBoundBuild(GoogleMap gmap, int width, int height) throws Exception {
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
            throw e;
        }
    }

    public void moveCamera(GoogleMap googleMap, float _zoom) {
        if(mActivityList==null) return;
        if(mActivityList.size()==0) return;

        LatLng curloc = new LatLng(mActivityList.get(mActivityList.size()-1).latitude,
                                 mActivityList.get(mActivityList.size()-1).longitude);
        myzoom = _zoom;
        CameraPosition cameraPosition = new CameraPosition.Builder().target(curloc).zoom(_zoom).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    public void moveCamera(GoogleMap googleMap) {
        if(mActivityList==null) return;
        if(mActivityList.size()==0) return;

        myzoom = googleMap.getCameraPosition().zoom;

        LatLng curloc = new LatLng(mActivityList.get(mActivityList.size()-1).latitude,
                mActivityList.get(mActivityList.size()-1).longitude);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(curloc).zoom(myzoom).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    public void moveCamera(GoogleMap googleMap, LatLng loc, float _zoom) {
        myzoom = _zoom;
        myzoom = googleMap.getCameraPosition().zoom;
        CameraPosition cameraPosition = new CameraPosition.Builder().target(loc).zoom(myzoom).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    public void moveCamera(GoogleMap googleMap, LatLng loc) {
        myzoom = googleMap.getCameraPosition().zoom;
        CameraPosition cameraPosition = new CameraPosition.Builder().target(loc).zoom(myzoom).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public ArrayList<MyActivity> deserialize(File file) {
        if(file == null)  {
            Log.e(TAG, "No File to deserialized");
            return null;
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

        if(list ==null) return null;
        return list;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_curact, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.save:
                String fname = ActivityUtil.serializeWithCurrentTime(mActivityList);
                Toast.makeText(CurActivity.this,"Activity saved to file(" + fname + ")",Toast.LENGTH_LONG).show();
                return true;
            case R.id.nomarker:
                nomarker = !nomarker;
                onStart();
                return true;
            case R.id.notrack:
                notrack = !notrack;
                onStart();
                return true;
            case R.id.satellite:
                satellite = !satellite;
                onStart();
                return true;
            case R.id.web:
                Intent wintent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://180.69.217.73:8080/OneOOMT"));
                startActivity(wintent);
                return true;

            case R.id.webupload:
//                last_fname = ActivityUtil.serializeWithCurrentTime(mList);
//                doHttpFileUpload3(Main2Activity.this, last_fname);
                return true;

            case R.id.uploadall2:
//                alertUploadServerChoice();
                return true;

            case R.id.uploadall:
//                doHttpFileUploadAll(Main2Activity.this, null);
                return true;

            case R.id.map:
////                Intent intent = new Intent(Main2Activity.this, CurActivity.class);
////                intent.putExtra("locations",mList);
//
//                startActivity(intent);
                return true;

            case R.id.files_d:
//                ActivityUtil._default_ext = ".ser";
//                File list[] = ActivityUtil.getFiles();
//
//                if(list == null) {
//                    Toast.makeText(getApplicationContext(), "ERR: No Activities to show !", Toast.LENGTH_LONG).show();
//                    return false;
//                }
//
//                int msize = list.length;
//
//                final CharSequence items[] = new CharSequence[msize];
//                final String filepath[] = new String[msize];
//
//                for(int i=0;i<msize;i++) {
//                    items[i] = list[i].getName();
//                    filepath[i] = list[i].getAbsolutePath();
//                }
//                //final CharSequence items[] = {" A "," B "};
//
//                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//                //alertDialog.setIcon(R.drawable.window);
//                alertDialog.setTitle("Select An Activity");
//                alertDialog.setItems(items, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int index) {
//                        File afile = new File(filepath[index]);
//                        ActivityUtil.showActivityAlertDialog(Main2Activity.this, afile, index);
//                    }
//                });
//                alertDialog.setNegativeButton("Back",null);
//                AlertDialog alert = alertDialog.create();
//                alert.show();
                return true;

            case R.id.files_a:

                ActivityUtil._default_ext = ".ser";
                File list2[] = ActivityUtil.getFiles();
                if(list2 == null) {
                    Toast.makeText(getApplicationContext(), "ERR: No Activities to show !", Toast.LENGTH_LONG).show();
                    return false;
                }

                int msize2 = list2.length;

                final CharSequence items2[] = new CharSequence[msize2];
                final String filepath2[] = new String[msize2];

                for(int i=0;i<msize2;i++) {
                    items2[i] = list2[i].getName();
                    filepath2[i] = list2[i].getAbsolutePath();
                }
                //final CharSequence items[] = {" A "," B "};

                AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(this);
                //alertDialog.setIcon(R.drawable.window);
                alertDialog2.setTitle("Select An Activity");
                alertDialog2.setItems(items2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int index) {
                        File afile = new File(filepath2[index]);
                        Intent intent = new Intent(CurActivity.this, ActFileActivity.class);
                        intent.putExtra("file", afile.getAbsolutePath());
                        intent.putExtra("pos", index);
                        startActivityForResult(intent, REQUEST_ACTIVITY_FILE_LIST);
                    }
                });
                alertDialog2.setNegativeButton("Back",null);
                AlertDialog alert2 = alertDialog2.create();
                alert2.show();
                return true;

            case R.id.upgrade:
                ActivityUtil._default_ext = ".ser";
                File uplist[] = ActivityUtil.getFiles();
                if(uplist == null) {
                    Toast.makeText(getApplicationContext(), "ERR: No Activities to show !", Toast.LENGTH_LONG).show();
                    return false;
                }

                int upsize = uplist.length;
                for(int i=0;i<upsize;i++) ActivityUtil.upgrade(uplist[i]);
                return true;

            case R.id.notify:
//                mode_noti = !mode_noti;
//                if(mode_noti) MyNotifier.go(Main2Activity.this, "100대명산알람설정", "알람설정이 켜졌습니다.");
//                else MyNotifier.go(Main2Activity.this, "100대명산알람설정", "알람설정이 껴졌습니다.");
                return true;

            case R.id.lowbattery:
//                mode_low_battery = ! mode_low_battery;
//                if(mode_low_battery) MyNotifier.go(Main2Activity.this, "100대명산알람설정", "Low Battery Mode..");
//                else MyNotifier.go(Main2Activity.this, "100대명산알람설정", "Low Battery Mode Off...");
//                return true;

            case R.id.weather:
//                if(mList.size()>0) {
//                    getMyWeather();
//                    while(cur_myweather == null) {}
//                    Toast.makeText(Main2Activity.this, cur_myweather.toString(), Toast.LENGTH_LONG).show();
//                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode,resultCode,intent);
        switch(requestCode) {
            case REQUEST_ACTIVITY_FILE_LIST:
                if(intent == null) return;
                String tmpFname = (String)intent.getStringExtra("fname");
                if(tmpFname==null) return;

                ArrayList<MyActivity> tmpActList =(ArrayList<MyActivity>)intent.getSerializableExtra("locations");
                ActivityStat as = ActivityUtil.getActivityStat(tmpActList);

                long endTime, startTime;
                startTime = as.start.getTime();
                endTime = new Date().getTime();
                long duration = endTime - startTime;
                long dur_sec = duration / 1000;
                long duration_r;
                int dur_hour_Int = (int)(dur_sec/3600);
                duration_r = dur_sec - (long)(dur_hour_Int) * 60 * 60;
                int dur_min_Int = (int)(duration_r / 60);
                duration_r = duration_r  - (long)(dur_min_Int) * 60;
                int dur_sec_Int = (int)(duration_r);

//                initSharedPreferences(tmpActList,
//                        as.distanceM,
//                        true,
//                        as.start.getTime(),
//                        tmpFname,
//                        (int)as.distanceKm,
//                        dur_min_Int,
//                        dur_hour_Int
//                );
//                Toast.makeText(Main2Activity.this,"Activity Reloaded ..... ", Toast.LENGTH_LONG).show();
        }
    }


}
