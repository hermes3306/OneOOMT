package com.joonho.myway;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.joonho.myway.util.Config;
import com.joonho.myway.util.MyActivityUtil;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static String TAG = "MainActivity";
    private boolean     __svc_started = false;
    private Intent      __svc_Intent = null;
    MyLocationService   mMyLocationService;

    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyLocationService.MyBinder mb = (MyLocationService.MyBinder) service;
            mMyLocationService = mb.getService();
            __svc_started = true;
        }

        public void onServiceDisconnected(ComponentName name) {
            __svc_started = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            }, 50);
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {

        super.onStart();
        if(__svc_started) {
            Toast.makeText(MainActivity.this, "SERVICE ALREADY STARTED", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(MainActivity.this,"START SERVICE", Toast.LENGTH_SHORT).show();
        Intent myI = new Intent(this, MyLocationService.class);
        bindService(myI, conn, Context.BIND_AUTO_CREATE);
        __svc_started = true;

    }

    @Override
    protected void onStop() {
        Toast.makeText(MainActivity.this,"onStop()", Toast.LENGTH_SHORT).show();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Log.e(TAG,"------- onBackPressed() called");
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_start) {
            if(__svc_started) {
                Toast.makeText(MainActivity.this, "SERVICE ALREADY STARTED", Toast.LENGTH_SHORT).show();
                return true;
            }

            Toast.makeText(MainActivity.this,"START SERVICE", Toast.LENGTH_SHORT).show();
            Intent myI = new Intent(this, MyLocationService.class);
            bindService(myI,conn, Context.BIND_AUTO_CREATE);
            __svc_started = true;

        } else if (id == R.id.nav_list) {
            File files_nav_list[] = MyActivityUtil.getFiles();
            if(files_nav_list == null) {
                Toast.makeText(getApplicationContext(), "ERR: No Activities to show !", Toast.LENGTH_SHORT).show();
                return false;
            }

            int files_nav_list_size = files_nav_list.length;
            final CharSequence items2[] = new CharSequence[files_nav_list_size];
            final String filepath2[] = new String[files_nav_list_size];

            for(int i=0;i<files_nav_list_size;i++) {
                long sz = files_nav_list
                        [i].length();
                String _sz=null;
                if(sz > 1024*1024) _sz = "" + sz / (1024 * 1024) + "MB";
                else if(sz > 1024) _sz = "" + sz / (1024) + "KB";
                else _sz = "" + sz + "B";
                items2[i] = files_nav_list[i].getName() + "(" + _sz + ")" ;
                filepath2[i] = files_nav_list[i].getAbsolutePath();
            }

            AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(this);
            alertDialog2.setTitle("Select an activity on the mobile("+files_nav_list_size+")");
            alertDialog2.setItems(items2, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int index) {
                    File afile = new File(filepath2[index]);
                    Intent intent = new Intent(MainActivity.this, FileActivity.class);
                    intent.putExtra("file", afile.getAbsolutePath());
                    intent.putExtra("pos", index);
                    startActivity(intent);
                }
            });
            alertDialog2.setNegativeButton("Back",null);
            AlertDialog alert2 = alertDialog2.create();
            alert2.show();
            return true;

        } else if (id == R.id.nav_map) {
            ArrayList<MyActivity> mlist = mMyLocationService.getMyAcitivityList();
            String fname = Config.get_filename();
            MyActivityUtil.serializeActivityIntoFile(mlist, fname);
            Intent intent = new Intent(MainActivity.this, FileActivity.class);
            intent.putExtra("file", Config.getAbsolutePath(fname));
            intent.putExtra("pos", 0);
            startActivity(intent);
            return true;

        } else if (id == R.id.nav_manage) {
            MyActivityUtil.dododo();
            return true;
        } else if (id == R.id.nav_cloud_list) {

        } else if (id == R.id.nav_cloud_sync) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
