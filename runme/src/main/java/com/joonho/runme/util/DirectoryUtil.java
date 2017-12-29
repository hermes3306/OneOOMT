package com.joonho.runme.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

/**
 * Created by jhpark on 17. 12. 29.
 */

public class DirectoryUtil {
    public static String TAG="DirectoryUtil";
    public static int total_cleansing=0;

    public static ArrayList<MyActivity> deserialize(File file) {
        if(file == null)  {
            Log.e(TAG, "No File to deserialized");
            return null;
        } else Log.e(TAG, "" + file.getAbsolutePath() + " to be deserialzed");

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream in = null;

        ArrayList list = null;

        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            in = new ObjectInputStream(bis);

            list = new ArrayList<MyActivity>();
            MyActivity ma=null;

            do {
                try {
                    ma = (MyActivity) in.readObject();
                    list.add(ma);
                }catch(Exception ex) {
                    ex.printStackTrace();
                    Log.e(TAG, ex.toString());
                    break;
                }
            } while(ma != null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        } finally {
            try {
                if (in != null) in.close();
                if (bis !=null) in.close();
                if (fis !=null) fis.close();

                if(list.size()==0) {
                    Log.e(TAG, "File ("+ file.getAbsolutePath() +") corrupted !!!!");
                    file.delete();
                    Log.e(TAG, "File ("+ file.getAbsolutePath() +") deleted  !!!!");
                }
            }catch(Exception e) {}
        }

        if(list ==null) return null;
        return list;
    }

    public static void clearUserlessMobileActivities(final Context ctx, final String filepath[]) {
        total_cleansing = 0;
        AsyncTask aTask = new AsyncTask<String, Void, Boolean>() {
            ProgressDialog asyncDialog = new ProgressDialog(ctx);


            @Override
            protected Boolean doInBackground(String... filepath) {
                try {
                    asyncDialog.setMax(filepath.length);
                    for (int i = 0; i < filepath.length; i++) {
                        File file = new File(filepath[i]);
                        ArrayList<MyActivity> list = deserialize(file);
                        if(list==null) {
                            file.delete();
                            total_cleansing++;
                            continue;
                        }
                        if(list.size()<5) {
                            file.delete();
                            total_cleansing++;
                            continue;
                        }
                        asyncDialog.setProgress(i);
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                    return false;
                }
                return true;
            }

            @Override
            protected void onPreExecute() {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("Cleansing...");
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                asyncDialog.dismiss();
                super.onPostExecute(result);
                Toast.makeText(ctx, "Cleansing..." + result + "!!", Toast.LENGTH_SHORT).show();
            }
        }.execute(filepath);

        int countdown = 10;
        while (aTask.getStatus() != AsyncTask.Status.FINISHED && countdown > 0) {
            try {
                Log.e(TAG, "waiting for activities cleansing....");
                Thread.sleep(100); //0.1초 기다림
                countdown--;
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }

        Toast.makeText(ctx, "Total (" + total_cleansing + ") activities deleted !!!", Toast.LENGTH_LONG).show();

    }
}
