package com.joonho.oneoomt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.joonho.oneoomt.db.PropsDB;

public class PropsActivity extends AppCompatActivity {
    private final String TAG="PropsActivity";
    private static PropsDB pdb = new PropsDB();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_props);

        final ToggleButton tg_up_menu = (ToggleButton) findViewById(R.id.tgb_upper_btn_menu);
        final ToggleButton tg = (ToggleButton) findViewById(R.id.tgb_ToastLocChanged);
        final ToggleButton tg_gps = (ToggleButton) findViewById(R.id.tgb_gps_lsnr);
        final ToggleButton tg_net = (ToggleButton) findViewById(R.id.tgb_net_lsnr);
        final ToggleButton tg_delete_act_save = (ToggleButton) findViewById(R.id.tgb_deleteOnactivitySave);
        final ToggleButton tg_drawer = (ToggleButton) findViewById(R.id.tgb_DRAW_MARKERS_ON_HISTORY);
        final EditText et_marker_cnt = (EditText) findViewById(R.id.et_marker_cnt);

        final Switch debug_switch = (Switch) findViewById(R.id.sw_debug);


        String upmenu = pdb.getProperty(getApplicationContext(), "Up_Button_Menu");
        Log.e(TAG, "****Up_Button_Menu: " + upmenu);
        if(upmenu==null) tg_up_menu.setChecked(false);
        else {
            if (upmenu.equalsIgnoreCase("true")) tg_up_menu.setChecked(true);
            else tg_up_menu.setChecked(false);
        }

        String toastlc = pdb.getProperty(getApplicationContext(), "Toast_LocationChanged");
        Log.e(TAG, "****Toast_LocationChanged: " + toastlc);
        if(toastlc==null) tg.setChecked(false);
        else {
            if (toastlc.equalsIgnoreCase("true")) tg.setChecked(true);
            else tg.setChecked(false);
        }

        String tg_gps_val = pdb.getProperty(getApplicationContext(), "GPS_Listener");
        String tg_net_val = pdb.getProperty(getApplicationContext(), "NETWORK_Listener");
        String tg_delete_act_save_val = pdb.getProperty(getApplicationContext(), "DELETE_WHEN_SAVE_ACT");
        String tg_marker_val = pdb.getProperty(getApplicationContext(), "DRAW_MARKERS_ON_HISTORY");
        String debug_switch_val = pdb.getProperty(getApplicationContext(), "DEBUG_ENABLED");
        String et_marker_cnt_val = pdb.getProperty(getApplicationContext(), "MARKER INTERVAL");

        if(tg_gps_val==null) tg_gps.setChecked(false);
        else if(tg_gps_val.equalsIgnoreCase("true")) tg_gps.setChecked(true);
        else tg_gps.setChecked(false);

        if(tg_net_val==null) tg_net.setChecked(false);
        else if(tg_net_val.equalsIgnoreCase("true")) tg_net.setChecked(true);
        else tg_net.setChecked(false);

        if(tg_delete_act_save_val==null) tg_delete_act_save.setChecked(false);
        else if(tg_delete_act_save_val.equalsIgnoreCase("true")) tg_delete_act_save.setChecked(true);
        else tg_delete_act_save.setChecked(false);

        if(tg_marker_val==null) tg_drawer.setChecked(false);
        else if(tg_marker_val.equalsIgnoreCase("true")) tg_drawer.setChecked(true);
        else tg_drawer.setChecked(false);


        if(et_marker_cnt_val==null) et_marker_cnt.setText("" + 1);
        else {
            int mc = Integer.parseInt(et_marker_cnt_val);
            et_marker_cnt.setText(et_marker_cnt_val);
        }

        if(debug_switch_val ==null) { debug_switch.setChecked(false); debug_switch.setText("Off"); }
        else if(debug_switch_val.equalsIgnoreCase("true")) {debug_switch.setChecked(true); debug_switch.setText("On");}
        else {debug_switch.setChecked(false); debug_switch.setText("Off");}

        btn_event();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.propsmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.item_props_quit) {
            Toast.makeText(getApplicationContext(),"Discard updates.... ",Toast.LENGTH_SHORT).show();
            finish();
            return true;
        }

        if (id == R.id.item_props_save) {
            save_props();
            Toast.makeText(getApplicationContext(),"Save updates.... ",Toast.LENGTH_SHORT).show();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void save_props() {

        pdb.deleteAll();

        final ToggleButton tg_up_menu = (ToggleButton) findViewById(R.id.tgb_upper_btn_menu);
        final ToggleButton tg = (ToggleButton) findViewById(R.id.tgb_ToastLocChanged);
        final ToggleButton tg_gps = (ToggleButton) findViewById(R.id.tgb_gps_lsnr);
        final ToggleButton tg_net = (ToggleButton) findViewById(R.id.tgb_net_lsnr);
        final ToggleButton tg_delete_act = (ToggleButton) findViewById(R.id.tgb_deleteOnactivitySave);
        final Switch switch_debug = (Switch) findViewById(R.id.sw_debug);
        final ToggleButton tg_marker = (ToggleButton) findViewById(R.id.tgb_DRAW_MARKERS_ON_HISTORY);
        final EditText et_marker_interval = (EditText) findViewById(R.id.et_marker_cnt);

        if(tg_up_menu.isChecked()) pdb.addProperty(PropsActivity.this, "Up_Button_Menu","true");
        else pdb.addProperty(PropsActivity.this, "Up_Button_Menu","false");

        if(tg.isChecked()) pdb.addProperty(PropsActivity.this, "Toast_LocationChanged","true");
        else pdb.addProperty(PropsActivity.this, "Toast_LocationChanged","false");

        if(tg_gps.isChecked()) pdb.addProperty(PropsActivity.this, "GPS_Listener", "true");
        else pdb.addProperty(PropsActivity.this, "GPS_Listener", "false");

        if(tg_net.isChecked()) pdb.addProperty(PropsActivity.this, "NETWORK_Listener", "true");
        else pdb.addProperty(PropsActivity.this, "NETWORK_Listener", "false");

        if(tg_delete_act.isChecked()) pdb.addProperty(PropsActivity.this, "DELETE_WHEN_SAVE_ACT", "true");
        else pdb.addProperty(PropsActivity.this, "DELETE_WHEN_SAVE_ACT", "false");

        if(switch_debug.isChecked()) pdb.addProperty(PropsActivity.this, "DEBUG_ENABLED", "true");
        else pdb.addProperty(PropsActivity.this, "DEBUG_ENABLED", "false");

        if(tg_marker.isChecked()) pdb.addProperty(PropsActivity.this, "DRAW_MARKERS_ON_HISTORY", "true");
        else pdb.addProperty(PropsActivity.this, "DRAW_MARKERS_ON_HISTORY", "false");

        String interval = et_marker_interval.getText().toString();
        if(interval != null) {
            pdb.addProperty(PropsActivity.this, "MARKER INTERVAL", interval);
        } else pdb.addProperty(PropsActivity.this, "MARKER INTERVAL", "1");
    }


    public void btn_event () {

        final Button btn_save = (Button)findViewById(R.id.btn_save);

        btn_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                save_props();
                finish();
            }
        });


    }


}
