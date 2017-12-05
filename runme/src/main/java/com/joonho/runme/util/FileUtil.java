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

}
