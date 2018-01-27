package com.joonho.myway.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jhpark on 18. 1. 27.
 */

public class AsyncService {
    private String TAG = "AsyncService";
    public void UploadAll(final Context context) {
        final String _serverUrl = Config._uploadURL;
        // Pop Up a Dialog
        new AsyncTask<Void,Void,Void>() {
            ProgressDialog asyncDialog = new ProgressDialog(context);
            HttpURLConnection urlConnection = null;
            String attachmentName = null;
            String attachmentFileName = null;
            String crlf = "\r\n";
            String twoHyphens = "--";
            String boundary =  "*****";

            @Override
            protected Void doInBackground(Void... voids) {
                File flist[] = MyActivityUtil.getFiles(Config._default_ext, false);
                asyncDialog.setMax(flist.length);

                for (int i = 0; i < flist.length; i++) {

                    File file = flist[i];
                    attachmentName = attachmentFileName = flist[i].getName();

                    try {
                        URL serverUrl = new URL(_serverUrl);
                        urlConnection = (HttpURLConnection) serverUrl.openConnection();

                        // request 준비
                        HttpURLConnection httpUrlConnection = null;
                        URL url = new URL(_serverUrl);
                        httpUrlConnection = (HttpURLConnection) url.openConnection();
                        httpUrlConnection.setUseCaches(false);
                        httpUrlConnection.setDoOutput(true);

                        httpUrlConnection.setRequestMethod("POST");
                        httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
                        httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");
                        httpUrlConnection.setRequestProperty(
                                "Content-Type", "multipart/form-data;boundary=" + this.boundary);

                        // content wrapper시작
                        DataOutputStream request = new DataOutputStream(
                                httpUrlConnection.getOutputStream());

                        request.writeBytes(this.twoHyphens + this.boundary + this.crlf);
                        request.writeBytes("Content-Disposition: form-data; name=\"" +
                                this.attachmentName + "\";filename=\"" +
                                this.attachmentFileName + "\"" + this.crlf);
                        request.writeBytes(this.crlf);

                        OutputStream out = httpUrlConnection.getOutputStream();
                        FileInputStream fis = new FileInputStream(file);
                        byte[] buffer = new byte[1024];
                        int readcount = 0;
                        while ((readcount = fis.read(buffer)) != -1) {
                            //Log.e(TAG, "readcount:" + readcount);
                            out.write(buffer, 0, readcount);
                        }
                        out.flush();

                        request.writeBytes(this.crlf);
                        request.writeBytes(this.twoHyphens + this.boundary +
                                this.twoHyphens + this.crlf);

                        request.flush();
                        request.close();

                        Log.e(TAG,"end of write to web server");

                        //==============받기===============
                        InputStream is = httpUrlConnection.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        StringBuffer sbResult = new StringBuffer();
                        String str = "";
                        while ((str = br.readLine()) != null) {
                            //Log.e(TAG, "RESPONSE:" + str);
                            sbResult.append(str);
                        }

                        asyncDialog.setProgress(i);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, e.toString());
                    }
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("Uploading...");
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                asyncDialog.dismiss();
                super.onPostExecute(aVoid);
                Toast.makeText(context, "Uploading success", Toast.LENGTH_LONG).show();
            }
        }.execute();
    }


}
