package com.joonho.runme.util;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by joonhopark on 2017. 12. 31..
 */

public class HttpRequest extends AsyncTask<URL, Void, Void> {

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
    protected Void doInBackground(URL... urls) {
        reqGET(urls[0]);
        return null;
    }

}
