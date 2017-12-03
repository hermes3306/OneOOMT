package com.joonho.runme.util;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by joonhopark on 2017. 10. 15..
 */

public class FileUtil {
    public static void copyFileUsingFileStreams(File source, File dest) throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while((bytesRead = input.read(buf)) > 0) {
                output.write(buf,0,bytesRead);
            }
        }finally {
            input.close();
            output.close();
        }
    }

    public static ArrayList<MyPicture> myPictureList = new ArrayList<MyPicture>();
    public static ArrayList<MyPicture> loadMyPictueList(File file) {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream in = null;

        ArrayList<MyPicture> list = new ArrayList<MyPicture>();
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            in = new ObjectInputStream(bis);
            list = new ArrayList<MyPicture>();
            MyPicture mp=null;

            int i=0;
            do {
                try {


                    mp = (MyPicture) in.readObject();
                    System.out.println("" + i + "th Picture(" + mp.toString() + ")" );
                    list.add(mp);
                    i++;
                }catch(Exception ex) {
                    ex.printStackTrace();
                    break;
                }
            } while(mp != null);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
                if (bis !=null) in.close();
                if (fis !=null) fis.close();
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }


    public static void main(String args[]) {
        File f = new File("/Users/joonhopark/Downloads/myPicture.master");
        myPictureList = loadMyPictueList(f);
        for(int i=0;i<myPictureList.size();i++) {
            MyPicture mp = myPictureList.get(i);
            System.out.println("" + i + ":" + mp.picname + "(" + mp.filepath + ")");
            System.out.println("     " + mp.myactivity.latitude + " " + mp.myactivity.longitude);
            System.out.println("     " + mp.myactivity.added_on);
            System.out.println("");

        }
    }


    public static void doHttpFileUpload() {
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
