package com.joonho.myway.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
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

    public static File getMediaStorageDirectory() {
        return mediaStorageDir;
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

    public static String getAddress(final Context _ctx, LatLng ll) {
        Geocoder geocoder = new Geocoder(_ctx, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(ll.latitude, ll.longitude,1);
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }

        String addinfo = null;
        if(addresses == null || addresses.size() ==0) {
            Log.e(TAG, "No Addresses found !!");
        }else {
            addinfo = addresses.get(0).getAddressLine(0).toString();
        }
        return addinfo;
    }

    public static String getAddress(final Context _ctx, MyActivity ma) {
        LatLng ll = new LatLng(ma.latitude, ma.longitude);
        return getAddress(_ctx, ll);
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
        if(addinfoDong.contains("null")) {
            addinfoDong = addresses.get(0).getAddressLine(0).toString();
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


    public static void dododo() {
        Date today = new Date();
        Date day = today;
        String dododo_str = StringUtil.DateToString(today, "yyyyMMdd");
        int cnt=0;

        do {
            Log.e(TAG, ">>>>>>>>>>>>>>>>>> " + String.format("%2d일전:",cnt)  + dododo_str + " >>>>>>>>   START ");
            dododo(dododo_str);
            day.setTime(day.getTime() - (1000 * 60 * 60 * 24));
            dododo_str = StringUtil.DateToString(day, "yyyyMMdd");
            Log.e(TAG, ">>>>>>>>>>>>>>>>>> " + dododo_str + " >>>>>>>>   END \n\n\n");
            cnt++;
        } while(cnt < 2); // 2days check
    }

    public static File[] getFilesStartsWithEndWith(final String prefix, final String postfix, boolean reverseorder) {
        FilenameFilter fnf = new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.toLowerCase().startsWith(prefix) && s.toLowerCase().endsWith(postfix);
            }
        };
        File[] flist  = mediaStorageDir.listFiles(fnf);
        if(reverseorder) Arrays.sort(flist, Collections.<File>reverseOrder());
        else Arrays.sort(flist);
        return flist;
    }


    public static void dododo(String day) {  // fmt: 20180101
        File files[] = getFilesStartsWithEndWith(day, ".ser2", false);
        ArrayList<String> fnamelist = dododo(files);
        for(int i=0;i<files.length;i++) {
            if(fnamelist.contains(files[i].getName() )) continue;
            else {
                Log.e(TAG, "*** removed : " + files[i].getName());
                files[i].delete();
            }
        }
        Log.e(TAG, "\n\n\n\n Done. ["+ day +"]");
    }

    public static ArrayList<String>  dododo(File files[]) {
        Log.e(TAG, "dododo");
        ArrayList<MyActivity> _all_list = new ArrayList<MyActivity>();

        for(int i=0;i<files.length;i++) {
            Log.e(TAG, "*** deserialize: " + files[i].getName());
            //if(files[i].getName().contains("(F)")) continue;
            ArrayList<MyActivity> _list = deserializeActivity(files[i]);
            _all_list.addAll(_list);
            Log.e(TAG, "*** # of _all_list:" + _all_list.size());
        }

        Collections.sort(_all_list,ALPHA_COMPARATOR);
        MyActivity pma = null;

        int rmv_cnt = 0;
        for(int i=_all_list.size()-1;i>0;i--) {
            MyActivity ma = _all_list.get(i);
            if(i>0) pma = _all_list.get(i-1);
            if(isSameActivity(ma, pma)) Log.e(TAG, "" + String.format("%04d",i) + ":" + ma.added_on + "," + ma.latitude + " [S]");
            else  Log.e(TAG, "" + String.format("%04d",i) + ":" + ma.added_on + "," + ma.latitude + " [X]");
            if(isSameActivity(ma, pma)) {
                _all_list.remove(i);
                rmv_cnt++;
            }
        }
        Log.e(TAG, "**** # of Duplicates(removed): " + rmv_cnt);
        Log.e(TAG, "**** # of Remains: " + _all_list.size());


        String _cur_date = null;
        int startpos = 0;

        // By adding dummy Activity for tomorrow....
        Date tomorrow = new Date ( new Date().getTime ( ) + (long) ( 1000 * 60 * 60 * 24 ) );
        String _added_on = StringUtil.DateToString(tomorrow, "yyyy년MM월dd일_HH시mm분ss초" );
        _all_list.add(new MyActivity(-1,-1, -1,_added_on));   // dummy Activity


        ArrayList <String> fnamelist = new ArrayList<>();

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
                    fnamelist.add(_fname);

                    Log.e(TAG,"**** " + _fname + "created successfully!" );
                    Log.e(TAG,"**** " + "from:" + startpos);
                    Log.e(TAG,"**** " + "to:" + (i-1));
                    Log.e(TAG,"\n\n");

                    _cur_date = t_str;
                    startpos=i;
                }
            }
        }
        return fnamelist;
    }

}
