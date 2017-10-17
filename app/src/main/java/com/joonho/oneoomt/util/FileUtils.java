package com.joonho.oneoomt.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
}
