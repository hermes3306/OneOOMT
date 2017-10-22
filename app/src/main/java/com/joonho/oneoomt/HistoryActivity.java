package com.joonho.oneoomt;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.joonho.oneoomt.db.DBGateway;
import com.joonho.oneoomt.file.myActivity;
import com.joonho.oneoomt.util.modifiedDate;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


//public class HistoryActivity extends FragmentActivity implements OnMapReadyCallback {

public class HistoryActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "HistoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Intent i = getIntent();
        Boolean mode = i.getBooleanExtra("historymode", true);
        if(mode) showhistory(); else show_pictures();
        btn_event();
    }

    public void btn_event() {
        final Button bt_refresh = (Button)findViewById(R.id.bt_refresh);
        final Button bt_pics = (Button)findViewById(R.id.bt_pics);
        final Button bt_back = (Button)findViewById(R.id.bt_back);

        bt_refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showhistory();
            }
        });
        bt_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        bt_pics.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                show_pictures();
            }
        });
    }

    public void show_pictures() {
        final File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//        Log.e(TAG,"Environment.DIRECTORY_PICTURES " + Environment.DIRECTORY_PICTURES);
        //final File storageDir = new File(Environment.getExternalStorageDirectory(), "MyCameraApp");

        final File f[] = storageDir.listFiles();

        int cnt = f.length;
        if(cnt==0) return;

        Button bt[] = new Button[cnt];
        int bt_id[] = new int[cnt];

        LinearLayout l = (LinearLayout)findViewById(R.id.linearlayout0);
        l.removeAllViews();

        TextView tv= new TextView(getApplicationContext());
        tv.setText("Click a picture below to view !!!!");
        tv.setTextSize(16);
        tv.setPadding(10,10,0,10);

        final String fnamelist[] = new String[cnt];
        for(int i=0;i<cnt;i++) {
            fnamelist[i] = new String(f[i].getAbsolutePath());
        }

        tv.setTextColor(Color.GREEN);
        l.addView(tv);


        for(int i=0;i<cnt;i++) {
            bt[i] = new Button(HistoryActivity.this);
            bt[i].setText(f[i].getAbsolutePath());

            l.addView(bt[i]);
            final int _idx = i;
            bt[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Button bt = (Button) v;
                    String str = (String)bt.getText();

                    //Toast.makeText(getApplicationContext(),"PIC (" + str + ") choosed!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(HistoryActivity.this, ViewCameraPicActivity.class);
                    intent.putExtra("fname", str);
                    intent.putExtra("fnamelist", fnamelist);
                    intent.putExtra("index", _idx);
                    startActivity(intent);
                }
            });
        }
    }


    public void showhistory() {
        final DBGateway dbgateway = new DBGateway();
        final File f[] = dbgateway.getallActivities(HistoryActivity.this);

        int cnt = f.length;
        if(cnt==0) return;

        Button bt[] = new Button[cnt];
        int bt_id[] = new int[cnt];

        // Dynamic view
        LinearLayout l=(LinearLayout)findViewById(R.id.linearlayout0);
        l.removeAllViews();


        TextView tv= new TextView(this);
        tv.setText("Click an activity below to view !!!!");
        tv.setTextSize(16);
        tv.setPadding(10,10,0,10);

        tv.setTextColor(Color.CYAN);
        l.addView(tv);

        for(int i=0;i<cnt;i++) {
            bt[i] = new Button(this);
            bt[i].setText(f[i].getName());
            l.addView(bt[i]);

            bt[i].setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Button bt = (Button) v;
                    String str = (String)bt.getText();

                    ArrayList<myActivity> list = dbgateway.deserializeActivities(HistoryActivity.this, str);
                    if(list.size()==0) {return;}
                    if(list == null) {return;}

                    double lat[] = new double[list.size()];
                    double lon[] = new double[list.size()];
                    ArrayList<String> ado = new ArrayList<String>();

                    for(int i=0;i<list.size();i++) {
                        lat[i] = ((myActivity)(list.get(i))).latitude;
                        lon[i] = ((myActivity)(list.get(i))).longitude;
                        ado.add(((myActivity)(list.get(i))).added_on);
                    }

                    Log.e(TAG, ">>>> History Activity will call HistoryGoogleActivity ! ");

                    Intent intent = new Intent(HistoryActivity.this, HistoryGoogleMapsActivity.class);
                    intent.putExtra("latitudes", lat);
                    intent.putExtra("longitudes", lon);
                    intent.putStringArrayListExtra("added_ons", ado);
                    intent.putExtra("fname",str);
                    startActivity(intent);
                }
            });
        }

//        /* Test for Fragment */
//        MapFragment mMapFragment1 = MapFragment.newInstance();
//        FragmentTransaction fragmentTransaction1 =
//                getFragmentManager().beginTransaction();
//        fragmentTransaction1.add(R.id.linearlayout0, mMapFragment1);
//        fragmentTransaction1.commit();
//        mMapFragment1.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.histmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.item_hist_back) {
            finish();
            return true;
        }

        if (id == R.id.item_hist_pictures) {
            show_pictures();
            return true;
        }

        if (id == R.id.item_hist_refresh) {
            showhistory();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private GoogleMap mMap;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(getApplicationContext(), "onMapReady called back!", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(128, 37))
                .title("Marker"));
    }
}
