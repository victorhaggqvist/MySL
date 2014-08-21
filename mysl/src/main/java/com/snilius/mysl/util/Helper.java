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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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
        File file = null;
        try {
            file = context.getFileStreamPath(fileName);
        } catch (Exception e) {
            return false;
        }
        return file.exists();
    }

    /**
     * Open a file
     * @param context
     * @param fileName
     * @return
     * @throws IOException
     */
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

    /**
     * Format and localize given date, expects date on format 2014-08-12T21:59:58+00:0
     * @param rawDate date on format 2014-08-12T21:59:58+00:0
     * @param locale two char locale id
     * @return formatted date
     */
    public static String localizeAndFormatDate(String rawDate, String locale){
        SimpleDateFormat formatter = new SimpleDateFormat("EEE d, MMM", new Locale(locale));

        String date = rawDate.split("T")[0];

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(date.split("-")[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(date.split("-")[1])-1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date.split("-")[2]));

        return formatter.format(calendar.getTime());
    }
}
