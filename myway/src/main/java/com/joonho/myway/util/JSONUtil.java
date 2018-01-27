package com.joonho.myway.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jhpark on 17. 12. 28.
 */

public class JSONUtil {
    public static String TAG = "JSONUtil";
    public static String httpParser(String _url) {
        JSONObject json = getJSONFromUrl(_url);
        return json.toString();
    }

    public static JSONObject getJSONFromUrl(String _url) {
        // Making HTTP request
        StringBuilder sb = new StringBuilder();
        String json=null;
        JSONObject jObj=null;

        HttpURLConnection connection;
        try {
            URL url = new URL(_url);
            connection = (HttpURLConnection)url.openConnection();
            if(connection!=null) {
                connection.setConnectTimeout(1000*10); // 10sec
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
            } else return null;
        } catch (Exception e) {
            Log.e(TAG,e.toString());
            return null;
        }

        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            reader.close();
            json = sb.toString();
            json.trim();
            //Log.e(TAG, json);
            System.out.println(json);
        } catch (Exception e) {
            Log.e(TAG, "Error converting result " + e.toString());
        }
        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing data " + e.toString());
        }
        // return JSON String
        return jObj;
    }

    public static void postJSON(String _url, JSONObject jsonObject) {
        HttpURLConnection connection;
        Log.e(TAG, "url:" + _url);
        try {
            URL url = new URL(_url);
            connection = (HttpURLConnection)url.openConnection();
            if(connection!=null) {
                connection.setReadTimeout(15000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
            } else return;

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(jsonObject.toString());
            writer.flush();
            writer.close();
            os.close();
            Log.e(TAG, "JSON Post OK!");
        } catch (Exception e) {
            Log.e(TAG, "JSON Post Error!");
            Log.e(TAG,e.toString());
            e.printStackTrace();
            return;
        }

        StringBuffer sb = new StringBuffer();
        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            reader.close();
            Log.e(TAG, "Result:" + sb.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error:" + e.toString());
        }
    }

    private void postAnJSONAsync(final Context context, final String url, JSONObject jobj) {
        new AsyncTask<JSONObject,Void,Void>() {
            ProgressDialog asyncDialog = new ProgressDialog(context);

            @Override
            protected Void doInBackground(JSONObject... jobj) {
                postJSON(url,jobj[0]);
                return null;
            }

            @Override
            protected void onPreExecute() {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("postJSON...");
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void result) {
                asyncDialog.dismiss();
                super.onPostExecute(result);
            }

        }.execute(jobj);
    }


}
