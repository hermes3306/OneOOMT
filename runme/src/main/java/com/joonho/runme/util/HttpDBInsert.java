package com.joonho.runme.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.joonho.runme.util.MyActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by joonhopark on 2018. 1. 1..
 */

public class HttpDBInsert extends AsyncTask<MyActivity, Void, Void> {
    private static Context ctx;
    private static String TAG = "HttpDBInsert";
    public void setContext(Context _ctx) {
        ctx = _ctx;
    }

    ProgressDialog asyncDialog = null;

    public void reqGET(URL url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String line;
                StringBuffer response = new StringBuffer();
                while((line = br.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                br.close();
                String res = response.toString();
            } finally {
                conn.disconnect();
            }
        } catch(Exception e){
            e.printStackTrace();
            Log.e("HttpRequest", e.toString());
        }
        Log.e("HttpRequest", "conn");
    }

    @Override
    protected Void doInBackground(MyActivity... myActivities) {

        asyncDialog.setMax(myActivities.length);
        for(int i=0;i<myActivities.length;i++) {

            String urlstr = "http://180.69.217.73/OneOOMT/insert.php?";
            urlstr += "latitude=" + myActivities[i].latitude;
            urlstr += "&longitude=" + myActivities[i].longitude;
            urlstr += "&altitude=" + myActivities[i].altitude;
            urlstr += "&added_on=" + myActivities[i].added_on;
            try {
                new HttpRequest().execute(new URL(urlstr));
                Log.e(TAG,"DB update... OK");
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            asyncDialog.setProgress(i);
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        asyncDialog =  new ProgressDialog(ctx);
        asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        asyncDialog.setMessage("Database Update...");
        asyncDialog.show();
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void result) {
        asyncDialog.dismiss();
    }


}
