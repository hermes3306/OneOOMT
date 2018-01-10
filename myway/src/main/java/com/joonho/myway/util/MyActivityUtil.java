package com.joonho.myway.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Environment;
import android.util.Log;

import com.joonho.myway.MyActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by nice9uy on 18. 1. 9.
 */

public class MyActivityUtil {
    private static String TAG = "MyActivityUtil";
    private static File mediaStorageDir = null;
    private static File backupDir = null;
    private static String _default_extension = ".ser2";
    private static boolean _default_reverse_order = true;

    static {
        mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "OneOOMT");
        String backupdir = StringUtil.DateToString(new Date(), "yyyyMMdd");
        backupDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "OneOOMT" + backupdir);
    }

    public static File[] getFilesStartsWith(final String prefix, boolean reverserorder) {
        FilenameFilter fnf = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().startsWith(prefix);
            }
        };
        File[] flist  = mediaStorageDir.listFiles(fnf);
        if(reverserorder) Arrays.sort(flist, Collections.<File>reverseOrder());
        else Arrays.sort(flist);
        return flist;
    }

    public static File[] getFilesEndsWith(final String postfix, boolean reverserorder) {
        FilenameFilter fnf = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().endsWith(postfix);
            }
        };
        File[] flist  = mediaStorageDir.listFiles(fnf);
        if(reverserorder) Arrays.sort(flist, Collections.<File>reverseOrder());
        else Arrays.sort(flist);
        return flist;
    }

    public static File[] getFiles(final String extension, boolean reverse_order) {
        FilenameFilter fnf = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().endsWith(extension);
            }
        };

        File[] files  = mediaStorageDir.listFiles(fnf);
        if (files == null) return null;
        if(reverse_order) Arrays.sort(files, Collections.reverseOrder());
        else Arrays.sort(files);

        return files;
    }

    public static File[] getFiles() {
        return getFiles(_default_extension, _default_reverse_order);
    }

    public static final Comparator<MyActivity> ALPHA_COMPARATOR  = new Comparator<MyActivity>() {
        private final Collator sCollator = Collator.getInstance();
        public int compare(MyActivity object1, MyActivity object2) {
            return sCollator.compare(object1.added_on, object2.added_on);
        }
    };

    public static void dododo_jason() {
        Log.e(TAG, "dododo_jason");

        File files[] = getFiles(".ser2",true);
        ArrayList<MyActivity> _all_list = new ArrayList<MyActivity>();

        for(int i=0;i<files.length;i++) {
            Log.e(TAG, "*** deserialize: " + files[i].getName());
            ArrayList<MyActivity> _list = deserializeActivity(files[i]);
            _all_list.addAll(_list);
        }

        Collections.sort(_all_list,ALPHA_COMPARATOR);
        MyActivity pma = null;

//        for(int i=_all_list.size()-1;i>0;i--) {
//            MyActivity ma = _all_list.get(i);
//            if(i>0) pma = _all_list.get(i-1);
//            if(isSameActivity(ma, pma)) Log.e(TAG, "" + String.format("%04d",i) + ":" + ma.added_on + "," + ma.latitude + " [S]");
//            else  Log.e(TAG, "" + String.format("%04d",i) + ":" + ma.added_on + "," + ma.latitude + " [X]");
//            if(isSameActivity(ma, pma)) _all_list.remove(i);
//        }

        for(int i=_all_list.size()-1;i>0;i--) {
            MyActivity ma = _all_list.get(i);
            if(i>0) pma = _all_list.get(i-1);
            if(isSameActivity(ma, pma)) Log.e(TAG, "" + String.format("%04d",i) + ":" + ma.added_on + "," + ma.latitude + " [S]");
            else  Log.e(TAG, "" + String.format("%04d",i) + ":" + ma.added_on + "," + ma.latitude + " [X]");
            if(isSameActivity(ma, pma)) _all_list.remove(i);
        }

        String _cur_date = null;
        int startpos = 0;

        // By adding dummy Activity for tomorrow....
        Date tomorrow = new Date ( new Date().getTime ( ) + (long) ( 1000 * 60 * 60 * 24 ) );
        String _added_on = StringUtil.DateToString(tomorrow, "yyyy년MM월dd일_HH시mm분ss초" );
        _all_list.add(new MyActivity(-1,-1, -1,_added_on));   // dummy Activity

        for(int i=0;i<_all_list.size();i++) {
            MyActivity ma = _all_list.get(i);
            Date _date = getActivityTime(ma);

            if(i==0) _cur_date = StringUtil.DateToString(_date,"yyyyMMdd");
            else {
                String t_str = StringUtil.DateToString(_date,"yyyyMMdd");
                if(_cur_date.equalsIgnoreCase(t_str)) {
                    // pass
                }else {
                    // serialize the previous list;
                    MyActivity _pma = _all_list.get(i-1);
                    String _fname = StringUtil.DateToString(getActivityTime(_pma), Config._filename_fmt) + "(F)" + ".json";
                    Log.e(TAG,"*" + _cur_date);

                    serializeActivityIntoJsonFile(_all_list, startpos, i-1, _fname );

                    Log.e(TAG,"**** " + _fname + "created successfully!" );
                    Log.e(TAG,"**** " + "from:" + startpos);
                    Log.e(TAG,"**** " + "to:" + (i-1));
                    Log.e(TAG,"\n\n");

                    _cur_date = StringUtil.DateToString(_date,"yyyyMMdd");
                    startpos=i;
                }
            }
        }

        if(Config._trash_after_dododo) {
            for (int i = 0; i < files.length; i++) {
                if(files[i].getName().contains("(F)")) continue;
                File f = new File(mediaStorageDir, files[i].getName() + ".trash");
                files[i].renameTo(f);
                Log.e(TAG, "**** TRASH " + f .getName()+ "!");
            }
        } else {
            for (int i = 0; i < files.length; i++) {
                if(files[i].getName().contains("(F)")) continue;
                files[i].delete();
                Log.e(TAG, "**** DELETE " + files[i].getName() + " deleted!");
            }
        }
    }


    public static void dododo() {
        Log.e(TAG, "dododo");

        File files[] = getFiles(".ser2",true);
        ArrayList<MyActivity> _all_list = new ArrayList<MyActivity>();

        for(int i=0;i<files.length;i++) {
            Log.e(TAG, "*** deserialize: " + files[i].getName());
            ArrayList<MyActivity> _list = deserializeActivity(files[i]);
            _all_list.addAll(_list);
        }

        Collections.sort(_all_list,ALPHA_COMPARATOR);
        MyActivity pma = null;

//        for(int i=_all_list.size()-1;i>0;i--) {
//            MyActivity ma = _all_list.get(i);
//            if(i>0) pma = _all_list.get(i-1);
//            if(isSameActivity(ma, pma)) Log.e(TAG, "" + String.format("%04d",i) + ":" + ma.added_on + "," + ma.latitude + " [S]");
//            else  Log.e(TAG, "" + String.format("%04d",i) + ":" + ma.added_on + "," + ma.latitude + " [X]");
//            if(isSameActivity(ma, pma)) _all_list.remove(i);
//        }

        for(int i=_all_list.size()-1;i>0;i--) {
            MyActivity ma = _all_list.get(i);
            if(i>0) pma = _all_list.get(i-1);
            if(isSameActivity(ma, pma)) Log.e(TAG, "" + String.format("%04d",i) + ":" + ma.added_on + "," + ma.latitude + " [S]");
            else  Log.e(TAG, "" + String.format("%04d",i) + ":" + ma.added_on + "," + ma.latitude + " [X]");
            if(isSameActivity(ma, pma)) _all_list.remove(i);
        }

        String _cur_date = null;
        int startpos = 0;

        // By adding dummy Activity for tomorrow....
        Date tomorrow = new Date ( new Date().getTime ( ) + (long) ( 1000 * 60 * 60 * 24 ) );
        String _added_on = StringUtil.DateToString(tomorrow, "yyyy년MM월dd일_HH시mm분ss초" );
        _all_list.add(new MyActivity(-1,-1, -1,_added_on));   // dummy Activity

        for(int i=0;i<_all_list.size();i++) {
            MyActivity ma = _all_list.get(i);
            Date _date = getActivityTime(ma);

            if(i==0) _cur_date = StringUtil.DateToString(_date,"yyyyMMdd");
            else {
                String t_str = StringUtil.DateToString(_date,"yyyyMMdd");
                if(_cur_date.equalsIgnoreCase(t_str)) {
                    // pass
                }else {
                    // serialize the previous list;
                    MyActivity _pma = _all_list.get(i-1);
                    String _fname = StringUtil.DateToString(getActivityTime(_pma), Config._filename_fmt) + "(F)" + Config._default_ext;
                    Log.e(TAG,"*" + _cur_date);
                    serializeActivityIntoFile(_all_list, startpos, i-1, _fname );
                    Log.e(TAG,"**** " + _fname + "created successfully!" );
                    Log.e(TAG,"**** " + "from:" + startpos);
                    Log.e(TAG,"**** " + "to:" + (i-1));
                    Log.e(TAG,"\n\n");

                    _cur_date = StringUtil.DateToString(_date,"yyyyMMdd");
                    startpos=i;
                }
            }
        }

        if(Config._trash_after_dododo) {
            for (int i = 0; i < files.length; i++) {
                if(files[i].getName().contains("(F)")) continue;
                File f = new File(mediaStorageDir, files[i].getName() + ".trash");
                files[i].renameTo(f);
                Log.e(TAG, "**** TRASH " + f .getName()+ "!");
            }
        } else {
            for (int i = 0; i < files.length; i++) {
                if(files[i].getName().contains("(F)")) continue;
                files[i].delete();
                Log.e(TAG, "**** DELETE " + files[i].getName() + " deleted!");
            }
        }
        if(true) dododo_jason();
    }



    public static void serializeActivityIntoJsonFile(ArrayList<MyActivity> list, int start, int end, String fileName) {
        if(start <0 || end >= list.size()) return;
        if(!mediaStorageDir.exists()) mediaStorageDir.mkdirs();
        File file = new File(mediaStorageDir, fileName);
        Log.e(TAG, "**** Activity file: " + file.toString());

        try {
            JSONArray jsonArr = new JSONArray();
            for(int i=start;i<= end;i++) {
                MyActivity ma = list.get(i);
                JSONObject obj = new JSONObject();
                obj.put("lat", ma.latitude);
                obj.put("lon", ma.longitude);
                obj.put("alt", ma.altitude);
                obj.put("add", ma.added_on);
                jsonArr.put(obj);
            }
            JSONObject jobj = new JSONObject();
            jobj.put("activities", jsonArr);
            FileWriter fwriter = new FileWriter(new File(mediaStorageDir,fileName));
            fwriter.write(jobj.toString());
            Log.e(TAG, jobj.toString());
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }

    public static void serializeActivityIntoFile(ArrayList<MyActivity> list, int start, int end, String fileName) {
        if(start <0 || end >= list.size()) return;

        if(!mediaStorageDir.exists()) mediaStorageDir.mkdirs();
        File file = new File(mediaStorageDir, fileName);
        Log.e(TAG, "**** Activity file: " + file.toString());
        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream out = new ObjectOutputStream(bos);

            for(int i=start;i<= end;i++) {
                MyActivity ma = list.get(i);
                out.writeObject(ma);
            }
            out.close();
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }

    public static void serializeActivityIntoFile(ArrayList<MyActivity> list, String fileName) {
        if(list== null) return;
        if(fileName == null) return;
        if(list.size()==0) return;
        if(fileName.length() ==0 ) return;
        serializeActivityIntoFile(list,0,list.size()-1,fileName);
    }

    public static boolean isSameActivity(MyActivity a1, MyActivity a2) {
        if(a1 == null || a2 == null) return false;
        if(a1.latitude == a2.latitude && a1.longitude == a2.longitude &&
                a1.added_on.equalsIgnoreCase(a2.added_on)) return true;
        return false;
    }

    public static MyActivity deserializeFirstActivity(File file) {
        if(file == null)  {
            Log.e(TAG,"ERR] Try to deserialize null file.....");
            return null;
        }

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream in = null;

        ArrayList list = null;
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            in = new ObjectInputStream(bis);

            list = new ArrayList<MyActivity>();
            MyActivity ma = null;

            ma = (MyActivity) in.readObject();
            return ma;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static ArrayList<MyActivity> deserializeActivity(File file) {
        if(file == null)  {
            Log.e(TAG,"ERR] Try to deserialize null file.....");
            return null;
        }

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
                    if(list != null) return list;
                }
            } while(ma != null);
        } catch (Exception e) {
            e.printStackTrace();
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
        return list;
    }

    public static Date getActivityTime(MyActivity ma) {
        if(ma==null) return null;
        Date date = StringUtil.StringToDate(ma.added_on, "yyyy년MM월dd일_HH시mm분ss초");
        return date;
    }

    public static String getStartTime(ArrayList<MyActivity> list) {
        if(list == null) return null;
        if(list.size()==0) return null;

        Date date = StringUtil.StringToDate(list.get(0).added_on, "yyyy년MM월dd일_HH시mm분ss초");
        String date_str = StringUtil.DateToString(date, "M월d일(E)H시m분s초");
        return date_str;
    }

    public static String getEndTime(ArrayList<MyActivity> list) {
        if(list == null) return null;
        if(list.size()-1 <0) return null;

        Date date = StringUtil.StringToDate(list.get(list.size()-1).added_on, "yyyy년MM월dd일_HH시mm분ss초");
        String date_str = StringUtil.DateToString(date, "M월d일(E)H시m분s초");
        return date_str;
    }

    public static String getAddress(final Context _ctx, MyActivity ma) {
        Geocoder geocoder = new Geocoder(_ctx, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(ma.latitude, ma.longitude,1);
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }

        String addinfo = null;
        if(addresses == null || addresses.size() ==0) {
            Log.e(TAG, "No Addresses found !!");
        }else {
            addinfo = addresses.get(0).getAddressLine(0).toString();
//
//                Log.e(TAG + i + ":getAdminArea=", ""+addresses.get(i).getAdminArea());
//                Log.e(TAG + i + ":getSubAdminArea=", ""+addresses.get(i).getSubAdminArea());
//                Log.e(TAG + i + ":getSubLocality=", ""+addresses.get(i).getSubLocality());
//                Log.e(TAG + i + ":getSubThoroughfare=", ""+addresses.get(i).getSubThoroughfare());
//                Log.e(TAG + i + ":getFeatureName=", ""+addresses.get(i).getFeatureName());
//                Log.e(TAG + i + ":getLocality=", ""+addresses.get(i).getLocality());
//                Log.e(TAG + i + ":getPhone=", ""+addresses.get(i).getPhone());
//                Log.e(TAG + i + ":getPostalCode=", ""+addresses.get(i).getPostalCode());
//                Log.e(TAG + i + ":getPremises=", ""+addresses.get(i).getPremises());
//                Log.e(TAG + i + ":getUrl=", ""+addresses.get(i).getUrl());
//                Log.e(TAG + i + ":getAddressLine(0)=", ""+addresses.get(i).getAddressLine(0));
//                Log.e(TAG + i + ":getAddressLine(1)=", ""+addresses.get(i).getAddressLine(1));
//                Log.e(TAG + i + ":getAddressLine(2)=", ""+addresses.get(i).getAddressLine(2));
//                Log.e(TAG + i + ":getAddressLine(3)=", ""+addresses.get(i).getAddressLine(3));
//                Log.e(TAG + i + ":getThoroughfare)=", ""+addresses.get(i).getThoroughfare());
//            }
        }
        return addinfo;
    }

    public static String getAddressDong(final Context _ctx, MyActivity ma) {
        Geocoder geocoder = new Geocoder(_ctx, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(ma.latitude, ma.longitude,1);
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }

        String addinfoDong = null;
        if(addresses == null || addresses.size() ==0) {
            Log.e(TAG, "No Addresses found !!");
        }else {
            addinfoDong = addresses.get(0).getThoroughfare() +
                    (addresses.get(0).getPremises()==null?"":" " + addresses.get(0).getPremises());
        }
        return addinfoDong;
    }

    public static ArrayList<String> getAllAddresses(final Context _ctx, MyActivity ma) {
        Geocoder geocoder = new Geocoder(_ctx, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(ma.latitude, ma.longitude,1);
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }

        String addinfo = null;
        if(addresses == null || addresses.size() ==0) {
            Log.e(TAG, "No Addresses found !!");
        }else {
            ArrayList<String> list = new ArrayList<String>();
            for(int i=0;i<addresses.size();i++) {
                list.add(addresses.get(i).getAddressLine(0).toString());
            }
            return list;
        }
        return null;
    }

    public static String getTimeStr(ArrayList<MyActivity> list, int pos) {
        if(list == null) return null;
        if(list.size()-1 <0) return null;
        Date date = StringUtil.StringToDate(list.get(pos).added_on, "yyyy년MM월dd일_HH시mm분ss초");
        String date_str = StringUtil.DateToString(date, "M월 d일 (E) H시 m분");
        return date_str;
    }

    public static Date getStartTimeDate(ArrayList<MyActivity> list) {
        if(list == null) return null;
        if(list.size()==0) return null;
        Date date = StringUtil.StringToDate(list.get(0).added_on, "yyyy년MM월dd일_HH시mm분ss초");
        return date;
    }

    public static Date getEndTimeDate(ArrayList<MyActivity> list) {
        if(list == null) return null;
        if(list.size()==0) return null;
        Date date = StringUtil.StringToDate(list.get(list.size()-1).added_on, "yyyy년MM월dd일_HH시mm분ss초");
        return date;
    }

}
