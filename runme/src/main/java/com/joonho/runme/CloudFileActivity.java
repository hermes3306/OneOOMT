package com.joonho.runme;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Environment;
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
import com.joonho.runme.util.HttpDownloadUtility;
import com.joonho.runme.util.MapUtil;
import com.joonho.runme.util.MyActivity;
import com.joonho.runme.util.StringUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CloudFileActivity extends AppCompatActivity {
    public static String TAG = "CloudFileActivity";
    final File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "OneOOMT");
    public static ArrayList<Marker> markers = null;
    public static int position = 0;
    public static ArrayList<MyActivity> mActivityList = new ArrayList<MyActivity>();
    public static String add1 = null;
    public static String add2 = null;
    public static boolean tog_add = true;
    public static String fnames[] = null;
    public static int marker_pos = 0;
    public static int marker_pos_prev =0;
    public static Polyline line_prev = null;
    public float myzoom = 16f;
    public static Marker last_marker=null;
    public static Marker bef_last_marker=null;

    public static final int REQUEST_ACTIVITY_FILE_LIST = 0x0001;
    public static boolean nomarker = true;
    public static boolean notrack = false;
    public static boolean satellite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_file);

        Intent intent = getIntent();
        fnames = intent.getExtras().getStringArray("files");
        position = intent.getExtras().getInt("pos");

        Log.e(TAG, "CloudFileName:" + fnames[position]);

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
            final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);

            public void GO2(final GoogleMap googleMap) {
                googleMap.clear();
                markers = new ArrayList<Marker>();
                String _url = "http://180.69.217.73:8080/OneOOMT/filedown.jsp?name=" + fnames[position];
                String urls[] = new String[1];
                urls[0] = _url;
                try {
                    HttpDownloadUtility.downloadFileAsync(CloudFileActivity.this, urls, mediaStorageDir.getAbsolutePath());
                }catch(Exception e) {
                    Log.e(TAG, e.toString());
                }

                File downfile = new File(mediaStorageDir, fnames[position]);
                if(!downfile.exists()) {
                    Toast.makeText(_ctx, "file("+downfile.getName()+") download failed!", Toast.LENGTH_SHORT).show();
                    return;
                }

                ActivityStat activityStat = null;

                if(downfile != null) mActivityList = deserialize(downfile);
                if(mActivityList==null) {
                    Log.e(TAG, "No Activities...");
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

                String inx_str = "\n" + (position+1)  + "/" + fnames.length + "\n" + "Total " + mActivityList.size() + " locations";
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

            public void GO(final GoogleMap googleMap) {
                Display display = getWindowManager().getDefaultDisplay();
                DisplayMetrics metrics = new DisplayMetrics();
                display.getMetrics( metrics );
                int width = metrics.widthPixels;
                int height = metrics.heightPixels;

                String _url = "http://180.69.217.73:8080/OneOOMT/filedown.jsp?name=" + fnames[position];
                try {
                    HttpDownloadUtility.downloadFile(_url, mediaStorageDir.getAbsolutePath());
                }catch(Exception e) {
                    Log.e(TAG, e.toString());
                }

                File downfile = new File(mediaStorageDir, fnames[position]);
                if(!downfile.exists()) {
                    Toast.makeText(_ctx, "file download failed!", Toast.LENGTH_SHORT).show();
                    return;
                }

                ActivityUtil.deserializeIntoMap(_ctx, new File(mediaStorageDir, fnames[position]), googleMap, width, height, false );
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
                GO2(googleMap);

                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

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

                        float color = (marker_pos==0? BitmapDescriptorFactory.HUE_ROSE:BitmapDescriptorFactory.HUE_CYAN);
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
                        String inx_str = "\n" + (position+1)  + "/" + fnames.length + "\n" + "" + marker_pos + "/" + mActivityList.size();
                        tv_cursor.setText(inx_str);
                    }
                });

                imbt_prev.setOnClickListener(new View.OnClickListener(){
                    public void onClick (View view) {
                        if (position > 0 && position < fnames.length) {
                            position--;
                            GO2(googleMap);
                        }
                    }
                });

                imbt_next.setOnClickListener(new View.OnClickListener(){
                    public void onClick (View view) {
                        if (position >= 0 && position < fnames.length-1) {
                            position++;
                            GO2(googleMap);
                        }
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
                setResult(Main2Activity.REQUEST_ACTIVITY_FILE_LIST,resultIntent );
                finish();
                return true;

            case R.id.share:
                return true;
            default:
                return true;
        }
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

}



