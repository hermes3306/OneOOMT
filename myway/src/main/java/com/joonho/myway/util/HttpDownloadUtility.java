package com.joonho.myway.util;

/**
 * Created by nice9 on 2017-12-07.
 */
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpDownloadUtility {
    private static final int BUFFER_SIZE = 4096;
    private static String TAG = "HttpDownloadUtility";

    /**
     * Downloads a file from a URL
     * @param fileURL HTTP URL of the file to be downloaded
     * @param saveDir path of the directory to save the file
     * @throws IOException
     */
    public static void downloadFile(String fileURL, String saveDir)
            throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();

            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());
            }

            Log.e(TAG,"Content-Type = " + contentType);
            Log.e(TAG,"Content-Disposition = " + disposition);
            Log.e(TAG,"Content-Length = " + contentLength);
            Log.e(TAG,"fileName = " + fileName);

            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = saveDir + File.separator + fileName;

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            Log.e(TAG,"File downloaded");
        } else {
            Log.e(TAG,"No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }

    public static void downloadFileAsync(final Context ctx, final String fileURL[], final String saveDir) {
        AsyncTask aTask = new AsyncTask<String, Void, Boolean>() {
            ProgressDialog asyncDialog = new ProgressDialog(ctx);
            @Override
            protected Boolean doInBackground(String... url) {
                try {
                    asyncDialog.setMax(fileURL.length);
                    for(int i=0;i<fileURL.length;i++) {
                        downloadFile(url[i], saveDir);
                        asyncDialog.setProgress(i);
                    }
                }catch(Exception e) {
                    Log.e(TAG, e.toString());
                    return false;
                }
                return true;
            }

            @Override
            protected void onPreExecute() {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("Downloading...");
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                asyncDialog.dismiss();
                super.onPostExecute(result);
                Toast.makeText(ctx, "file download " + result + "!!", Toast.LENGTH_SHORT).show();
            }
        }.execute(fileURL);

        int countdown = 10;
        while (aTask.getStatus() != AsyncTask.Status.FINISHED && countdown >0) {
            try {
                Log.e(TAG, "waiting for file download....");
                Thread.sleep(100); //0.1초 기다림
                countdown--;
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
    }


    static String filesOnCloud[] = null;
    static Long filesizeOnCloud[] = null;

    private String[] getFilesOnCloud(final Context context, String url[]) {
        AsyncTask aTask = new AsyncTask<String, Void, Boolean>() {
            ProgressDialog asyncDialog = new ProgressDialog(context);
            @Override
            protected Boolean doInBackground(String... url) {
                JSONObject jObj = JSONUtil.getJSONFromUrl(url[0]);
                try {
                    JSONArray arr = (JSONArray) jObj.get("files");
                    filesOnCloud = new String[arr.length()];
                    filesizeOnCloud = new Long[arr.length()];

                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject j = (JSONObject) arr.get(i);
                        filesOnCloud[i] = (String) j.get("name");
                        filesizeOnCloud[i] = Long.parseLong((String) j.get("size"));
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                return true;
            }

            @Override
            protected void onPreExecute() {
                filesOnCloud = null;
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("Downloading...");
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                asyncDialog.dismiss();
                super.onPostExecute(result);
                Toast.makeText(context, "list download " + result + "!!", Toast.LENGTH_SHORT).show();
            }

        }.execute(url);

        int countdown = 10;
        while (aTask.getStatus() != AsyncTask.Status.FINISHED && countdown > 0) {
            try {
                Log.e(TAG, "waiting for get filesOnCloud....");
                Thread.sleep(100); //0.1초 기다림
                countdown--;
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
        return filesOnCloud;
    }


}
