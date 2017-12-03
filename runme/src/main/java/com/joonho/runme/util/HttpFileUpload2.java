package com.joonho.runme.util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.io.File;

/**
 * Created by joonhopark on 2017. 12. 4..
 */

public class HttpFileUpload2 {
    static String TAG = "HttpFileUpload2";
    final static String serverUrl = "http://180.69.217.73:8080/OneOOMT/upload";
    final static File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "OneOOMT");
    final static String fname = "";

    public static void go() throws Exception {

        //==============환경===============
        File file = mediaStorageDir.listFiles()[3];
        Log.e(TAG, "filename to uplad: " + file.getAbsolutePath());


        URL url = new URL(serverUrl);
        URLConnection httpConn = url.openConnection();
        httpConn.setDoOutput(true);
        httpConn.setUseCaches(false);
        httpConn.setRequestProperty("Content-type", "application/octet-stream");
        httpConn.setRequestProperty("Content-Length", String.valueOf(file.length()));


        //==============보내기===============
        OutputStream out = httpConn.getOutputStream();
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int readcount = 0;
        while ((readcount = fis.read(buffer)) != -1) {
            out.write(buffer, 0, readcount);
        }
        out.flush();

        //==============받기===============
        InputStream is = httpConn.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuffer sbResult = new StringBuffer();
        String str = "";
        while ((str = br.readLine()) != null) {
            sbResult.append(str);
        }
    }


}
