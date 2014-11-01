package com.snilius.mysl;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";
    private static final String EXPIRE_DATE = "ExpireDate";

    public AlarmReceiver(){}

    @Override
    public void onReceive(Context context, Intent intent) {
        // if on boot
        if (null != intent.getAction() && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.i(TAG, "Running on boot");
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            Set<String> stringSet = preferences.getStringSet(context.getString(R.string.pref_alarms), new HashSet<String>());

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            for (String s:stringSet){
                Log.i(TAG,s);
                String ss[] = s.split("::");

                Intent aIntent = new Intent(context, AlarmReceiver.class);
                aIntent.putExtra(CardActivity.EXTRA_CARD_SERIAL, ss[0]);
                aIntent.putExtra(CardActivity.EXTRA_CARD_PRODUCTHASH, ss[1]);
                aIntent.putExtra(CardActivity.EXTRA_CARD_PRODUCTNAME, ss[4]);
                aIntent.putExtra(EXPIRE_DATE, ss[3]);
                PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

                alarmManager.set(AlarmManager.RTC, Long.parseLong(ss[2]), alarmIntent);
            }
        }
        // else notification time
        else {
            Log.i(TAG, "fire in tha HOUSE");

            String productName = intent.getStringExtra(CardActivity.EXTRA_CARD_PRODUCTNAME);
            String expireDate = intent.getStringExtra(EXPIRE_DATE);
            String cardSerial = intent.getStringExtra(CardActivity.EXTRA_CARD_SERIAL);
            Log.i(TAG,productName+" "+expireDate);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_launcher_white)
                            .setContentTitle(productName)
                            .setContentText(String.format(context.getString(R.string.notification_subtitle),expireDate));

            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(context, CardActivity.class);
            resultIntent.putExtra(CardActivity.EXTRA_CARD_SERIAL, cardSerial);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                // Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(MainActivity.class);
                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);
            }else {
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, 0);
                mBuilder.setContentIntent(pendingIntent);
            }
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // mId allows you to update the notification later on.
            mNotificationManager.notify(42, mBuilder.build());
        }
    }

    public static String registerNotification(Context context, SharedPreferences preferences,
                                              String productName, String mSerial, String expireDate, Integer productHash,
                                              Integer daysBeforeNotification){

        // Enable start on boot to set alarams after device reboot
        ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        // Create the date for alram
        String[] dateParts = expireDate.split("-");
        GregorianCalendar alarmDate= new GregorianCalendar(Integer.parseInt(dateParts[0]),
                Integer.parseInt(dateParts[1]), Integer.parseInt(dateParts[2]));
        alarmDate.setTimeZone(TimeZone.getDefault());
        alarmDate.add(Calendar.MONTH,-1); // why is it a month off?
        alarmDate.add(Calendar.DAY_OF_YEAR, -1*daysBeforeNotification);
        long alarmTime = alarmDate.getTimeInMillis();

        // save alarm prefs
        Set<String> alarmSet = preferences.getStringSet(context.getString(R.string.pref_alarms), new HashSet<String>());
        alarmSet.add(mSerial+"::"+productHash+"::"+alarmTime+"::"+expireDate+"::"+productName);
        preferences.edit().putStringSet(context.getString(R.string.pref_alarms), alarmSet).commit();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(CardActivity.EXTRA_CARD_SERIAL, mSerial);
        intent.putExtra(CardActivity.EXTRA_CARD_PRODUCTHASH, productHash);
        intent.putExtra(CardActivity.EXTRA_CARD_PRODUCTNAME, productName);
        intent.putExtra(EXPIRE_DATE, expireDate);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmManager.set(AlarmManager.RTC, alarmTime, alarmIntent);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(TimeZone.getDefault());
        df.setCalendar(alarmDate);

        return "Notification on "+df.format(alarmDate.getTime());
    }
}
