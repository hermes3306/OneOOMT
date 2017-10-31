package com.joonho.oneoomt;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.joonho.oneoomt.file.myActivity;
import com.joonho.oneoomt.util.ActivityUtil;
import com.joonho.oneoomt.util.CalDistance;
import com.joonho.oneoomt.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.joonho.oneoomt.RunningActivity.pTimerPeriod;

public class StartRunActivity extends AppCompatActivity {
    private static String TAG = "StartRunActivity";

    private static long start_time, end_time;
    private static TextView tv_time_elapsed01 = null;
    private static TextView tv_total_distance01 = null;
    private static TextView tv_avg_pace01 = null;
    private static TextView tv_cur_pace01 = null;
    private static ImageButton imb_stop_timer = null;
    private static TextView tv_time_elapsed02 = null;
    private static TextView tv_total_distance02 = null;
    private static TextView tv_avg_pace02 = null;
    private static TextView tv_cur_pace02 = null;
    private static ImageButton imb_stop_timer01 = null;
    private static ImageButton imb_stop_timer02 = null;

    private static TimerTask mTask = null;
    private static Timer mTimer = null;

    private static boolean isStarted = true;

    private static double total_distance = 0;
    private static Location start_loc;
    private static ArrayList<Location> mList = new ArrayList<Location>();


    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_run);

        initialize_Location_Manager();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        Fragment fragment1 = mSectionsPagerAdapter.getItem(1);
        Log.e(TAG, "mViewPager.getCurrentItem:" + mViewPager.getCurrentItem());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//
                alertDialogChoice();

            }
        });
        doMyTimeTask();
    }

    LocationManager locationManager = null;
    Boolean isGPSEnabled = null;
    Boolean isNetworkEnabled = null;

    public void initialize_Location_Manager() {
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Log.d(TAG, "isGPSEnabled=" + isGPSEnabled);
        Log.d(TAG, "isNetworkEnabled=" + isNetworkEnabled);
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
//                Log.e(TAG,"New Loc " + lat + " " + lng);
//                //Toast.makeText(StartRunning2Activity.this, lat + " " + lng, Toast.LENGTH_SHORT).show();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        if ((ContextCompat.checkSelfPermission(StartRunActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(StartRunActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(StartRunActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(StartRunActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(StartRunActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(StartRunActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            }, 50);
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

    }

    public Location getLocation() {
        // 수동으로 위치 구하기
        String locationProvider = LocationManager.GPS_PROVIDER;
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "no Permission"); // but never occur!
                return null;
            }

            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            if (lastKnownLocation != null) {
                double lng = lastKnownLocation.getLatitude();
                double lat = lastKnownLocation.getLatitude();
                Log.d(TAG, "GPS,  longtitude=" + lng + ", latitude=" + lat);
                return lastKnownLocation;
            }
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start_run, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class MyTimerTask extends java.util.TimerTask {
        public void run() {
            if (!isStarted) return;
            final long start = System.currentTimeMillis();
            StartRunActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    // duration
                    end_time = new Date().getTime();
                    long elapsed_time = end_time - start_time;
                    String duration = StringUtil.Duration(new Date(start_time), new Date(end_time));
                    if(tv_time_elapsed01 != null) tv_time_elapsed01.setText(duration);
                    if(tv_time_elapsed02 != null) tv_time_elapsed02.setText(duration);

                    Log.e(TAG, "" + duration + "");

                    // distance
                    Location cur_loc = getLocation();
                    if (cur_loc == null) {

                    } else {
                        int msize = mList.size();
                        if (msize == 0) {
                            mList.add(cur_loc);
                            return;
                        }

                        Location last_loc = mList.get(msize - 1);
                        CalDistance cd = new CalDistance(last_loc.getLatitude(), last_loc.getLongitude(), cur_loc.getLatitude(), cur_loc.getLongitude());
                        double dist = cd.getDistance();
                        if (Double.isNaN(dist)) dist = 0;

                        if (cur_loc == last_loc) {
                            Log.e(TAG, "same location");
                        } else if (Double.isNaN(dist)) {
                            Log.e(TAG, "dist is NAN");
                        } else if (dist < 0.1d) { // 10 cm 이하 변동 사항
                            Log.e(TAG, "distance < 0.1meter discarded");
                        } else {
                            mList.add(cur_loc);
                        }

                        total_distance = total_distance + dist;
                        double dist_kilo = total_distance / 1000f;
                        String distance_str = String.format("%.2f", dist_kilo);
                        if(tv_total_distance01 != null) tv_total_distance01.setText(distance_str);
                        if(tv_total_distance02 != null) tv_total_distance02.setText(distance_str);
                        double mpk = ActivityUtil.getMinPerKm(start_time, end_time, dist_kilo);
                        String mstr = String.format("%.2f", mpk);
                        if(tv_avg_pace01 != null) tv_avg_pace01.setText(mstr);  //  00:00로 변경 필요
                        if(tv_cur_pace02 != null) tv_cur_pace02.setText(mstr);
                    }
                }
            });
        }
    }

    public void doTimerPause() {
        isStarted = !isStarted;
        Log.e(TAG, "isStarted: " + isStarted);
        Toast.makeText(StartRunActivity.this, "isStarted = " + isStarted, Toast.LENGTH_LONG).show();
    }

    public  void doMyTimeTask() {
        mTask =new MyTimerTask();
        mTimer = new Timer();
        mTimer.schedule(mTask, 1000, pTimerPeriod); //delaytime(10sec), period(1sec)
        total_distance = 0;
        isStarted = true;
        start_time = new Date().getTime();
        mList = new ArrayList<Location>();
        start_loc = getLocation();
        mList.add(start_loc);
    }

    public void alertDialogChoice() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(StartRunActivity.this);
        alertDialog.setTitle("운동모드 선택");
        alertDialog.setMessage("운동모드를 선택해주십시요.");

        String n_text = null;
        if(isStarted) n_text = "일시정지"; else n_text ="계속";

        alertDialog.setNeutralButton(n_text, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doTimerPause();
            }
        });

        alertDialog.setPositiveButton("새로운시작", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ArrayList<myActivity> mylist = ActivityUtil.Loc2Activity(mList);
                String fname = ActivityUtil.serializeWithCurrentTime(mylist);
                Toast.makeText(StartRunActivity.this, "" + fname + " 으로 저장후 다시 시작합니다.", Toast.LENGTH_LONG).show();

                doMyTimeTask();
            }
        });

        alertDialog.setNegativeButton("종료", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isStarted = false;
                ArrayList<myActivity> mylist = ActivityUtil.Loc2Activity(mList);
                String fname = ActivityUtil.serializeWithCurrentTime(mylist);
                Toast.makeText(StartRunActivity.this, "" + fname + " 으로 저장후 종료합니다.", Toast.LENGTH_LONG).show();

                mTask.cancel();
                finish();
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment{
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int _section_number = getArguments().getInt(ARG_SECTION_NUMBER);
            View rootView = null;
            Log.e(TAG, "Section_Number:" + _section_number);

            switch(_section_number) {
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_start_run_dashboard001, container, false);
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_start_run_dashboard002, container, false);
                    break;
                default:
                    break;
            }

            if(getArguments().getInt(ARG_SECTION_NUMBER) ==1) {
                tv_time_elapsed01 = (TextView) rootView.findViewById(R.id.tv_time_elapsed01);
                tv_total_distance01 = (TextView) rootView.findViewById(R.id.tv_total_distance01);
                tv_avg_pace01 = (TextView) rootView.findViewById(R.id.tv_avg_pace01);
                tv_cur_pace01 = (TextView) rootView.findViewById(R.id.tv_cur_pace01);
                imb_stop_timer01 = (ImageButton) rootView.findViewById(R.id.imb_stop_timer01);
                imb_stop_timer01.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //alertDialogChoice();
                        Toast.makeText(view.getContext(), "Image Button 1 On Click !", Toast.LENGTH_LONG).show();
                    }
                });
            }

            if(getArguments().getInt(ARG_SECTION_NUMBER) ==2) {
                tv_time_elapsed02 = (TextView) rootView.findViewById(R.id.tv_time_elapsed02);
                //tv_total_distance = (TextView) rootView.findViewById(R.id.tv_total_distance);
                tv_avg_pace02 = (TextView) rootView.findViewById(R.id.tv_avg_pace02);
                tv_cur_pace02 = (TextView) rootView.findViewById(R.id.tv_cur_pace02);
                imb_stop_timer02 = (ImageButton) rootView.findViewById(R.id.imb_stop_timer02);
                imb_stop_timer02.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //alertDialogChoice();
                        Toast.makeText(view.getContext(), "Image Button 2 On Click !", Toast.LENGTH_LONG).show();
                    }
                });
            }
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }
    }
}
