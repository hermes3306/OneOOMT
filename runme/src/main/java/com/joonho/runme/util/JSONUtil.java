package com.joonho.runme.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
            Log.e(TAG, json);
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

}
