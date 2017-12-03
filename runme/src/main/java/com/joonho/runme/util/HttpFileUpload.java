package com.joonho.runme.util;

/**
 * Created by joonhopark on 2017. 12. 3..
 */

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpFileUpload extends AsyncTask<String, Void, Void> {
    @Override
    public Void doInBackground(String... params) {
        try {
            URL serverUrl =
                    new URL("http://180.69.217.73:8080/OneOOMT/upload");
            HttpURLConnection urlConnection = (HttpURLConnection) serverUrl.openConnection();

            String boundaryString = "----SomeRandomText";

            // Activity File 첫번째 값
            File list[] = ActivityUtil.getFiles();
            File logFileToUpload = list[0];

            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);

            urlConnection.setDoOutput(true);
            OutputStream outputStreamToRequestBody = urlConnection.getOutputStream();
            BufferedWriter httpRequestBodyWriter =
                    new BufferedWriter(new OutputStreamWriter(outputStreamToRequestBody));

            // Include value from the myFileDescription text area in the post data
            //httpRequestBodyWriter.write("\n\n--" + boundaryString + "\n");
            //httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"myFileDescription\"");
            //httpRequestBodyWriter.write("\n\n");
            //httpRequestBodyWriter.write("Log file for 20150208");

            // Include the section to describe the file
            httpRequestBodyWriter.write("\n--" + boundaryString + "\n");
            httpRequestBodyWriter.write("Content-Disposition: form-data;"
                    + "name=\"file\";"
                    + "filename=\""+ logFileToUpload.getName() +"\""
                    + "\nContent-Type: text/plain\n\n");
            httpRequestBodyWriter.flush();

            // Write the actual file contents
            FileInputStream inputStreamToLogFile = new FileInputStream(logFileToUpload);

            int bytesRead;
            byte[] dataBuffer = new byte[1024];
            while((bytesRead = inputStreamToLogFile.read(dataBuffer)) != -1) {
                outputStreamToRequestBody.write(dataBuffer, 0, bytesRead);
            }
            outputStreamToRequestBody.flush();

            // Mark the end of the multipart http request
            httpRequestBodyWriter.write("\n--" + boundaryString + "--\n");
            httpRequestBodyWriter.flush();

            // Close the streams
            outputStreamToRequestBody.close();
            httpRequestBodyWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("HttpFileUpload", e.toString());
        }
        return null;
    }


    public static void doFileUpload() {
        try {
            URL serverUrl =
                    new URL("http://180.69.217.73:8080/OneOOMT/upload");
            HttpURLConnection urlConnection = (HttpURLConnection) serverUrl.openConnection();

            String boundaryString = "----SomeRandomText";

            // Activity File 첫번째 값
            File list[] = ActivityUtil.getFiles();
            File logFileToUpload = list[0];

            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundaryString);

            urlConnection.setDoOutput(true);
            OutputStream outputStreamToRequestBody = urlConnection.getOutputStream();
            BufferedWriter httpRequestBodyWriter =
                    new BufferedWriter(new OutputStreamWriter(outputStreamToRequestBody));

            // Include value from the myFileDescription text area in the post data
            //httpRequestBodyWriter.write("\n\n--" + boundaryString + "\n");
            //httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"myFileDescription\"");
            //httpRequestBodyWriter.write("\n\n");
            //httpRequestBodyWriter.write("Log file for 20150208");

            // Include the section to describe the file
            httpRequestBodyWriter.write("\n--" + boundaryString + "\n");
            httpRequestBodyWriter.write("Content-Disposition: form-data;"
                    + "name=\"file\";"
                    + "filename=\""+ logFileToUpload.getName() +"\""
                    + "\nContent-Type: text/plain\n\n");
            httpRequestBodyWriter.flush();

            // Write the actual file contents
            FileInputStream inputStreamToLogFile = new FileInputStream(logFileToUpload);

            int bytesRead;
            byte[] dataBuffer = new byte[1024];
            while((bytesRead = inputStreamToLogFile.read(dataBuffer)) != -1) {
                outputStreamToRequestBody.write(dataBuffer, 0, bytesRead);
            }
            outputStreamToRequestBody.flush();

            // Mark the end of the multipart http request
            httpRequestBodyWriter.write("\n--" + boundaryString + "--\n");
            httpRequestBodyWriter.flush();

            // Close the streams
            outputStreamToRequestBody.close();
            httpRequestBodyWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("HttpFileUpload", e.toString());
        }

    }

}
