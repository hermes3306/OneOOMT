package com.joonho.oneoomt.util;

import com.joonho.oneoomt.file.myPicture;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by joonhopark on 2017. 10. 15..
 */

public class FileUtils {
    public static void copyFileUsingFileStreams(File source, File dest) throws IOException{
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

    public static ArrayList<myPicture> myPictureList = new ArrayList<myPicture>();
    public static ArrayList<myPicture> loadMyPictueList(File file) {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream in = null;

        ArrayList<myPicture> list = new ArrayList<myPicture>();
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            in = new ObjectInputStream(bis);
            list = new ArrayList<myPicture>();
            myPicture mp=null;

            int i=0;
            do {
                try {




















                    mp = (myPicture) in.readObject();
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
            myPicture mp = myPictureList.get(i);
            System.out.println("" + i + ":" + mp.picname + "(" + mp.filepath + ")");
            System.out.println("     " + mp.myactivity.latitude + " " + mp.myactivity.longitude);
            System.out.println("     " + mp.myactivity.added_on);
            System.out.println("");

        }
    }
}
