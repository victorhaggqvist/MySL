package com.snilius.mysl.util;

import android.content.Context;

import com.snilius.mysl.R;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by victor on 7/22/14.
 */
public class Helper {
    public static void saveToFile(Context context, String fileName, String content) throws IOException {
        FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        outputStream.write(content.getBytes());
        outputStream.close();
    }

    public static Boolean isFileExsist(Context context, String fileName) {
        File file = context.getFileStreamPath(fileName);
        return file.exists();
    }

    public static String openFile(Context context, String fileName) throws IOException {
        String file = "";

        InputStream inputStream = new BufferedInputStream(context.openFileInput(fileName));
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line = reader.readLine();
        while (line != null){
            file +=line;
            line = reader.readLine();
        }

        reader.close();
        inputStream.close();

        return file;
    }
}
