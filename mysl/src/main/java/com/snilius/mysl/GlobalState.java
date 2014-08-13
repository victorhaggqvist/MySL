package com.snilius.mysl;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.JsonObject;

import java.util.Locale;

/**
 * Created by victor on 7/20/14.
 */
public class GlobalState extends Application{

    public static final String TAG = "GSMySL";
    public static String ANALYTICS_PROPERY_KEY = "UA-53767510-1";
    public static String APP_VERSION = BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")";

    private JsonObject mShoppingCart;

    private String mUsername, mPassword;

    private boolean refresh;
    private boolean addCardDialogOpen;
    private String addCardDialogMsg;

    public GlobalState() { }

    @Override
    public void onCreate() {
        super.onCreate();
        reloadLocaleForApplication();
    }

    public void reloadLocaleForApplication() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        String language = sharedPreferences.getString(getString(R.string.pref_lang), "system");
        Log.d(TAG, "Preferred language: " + language);

        Locale locale = null;
        if ("sv".equals(language)) {
            locale = new Locale("sv", "SE");
        } else if ("es".equals(language)) {
            locale = new Locale("es", "ES");
        } else if ("en".equals(language)) {
            locale = Locale.ENGLISH;
        }

        if (locale != null) {
            Log.d(TAG, "setting locale " + locale);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = locale;
            res.updateConfiguration(conf, dm);
        }
    }

    public JsonObject getmShoppingCart() {
        return mShoppingCart;
    }

    public void setmShoppingCart(JsonObject shoppingCart) {
        mShoppingCart = shoppingCart;
    }

    public void setUserinfo(String username, String password){
        mUsername = username;
        mPassword = password;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getPassword() {
        return mPassword;
    }

    public boolean isRefresh() {
        return refresh;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    public boolean isAddCardDialogOpen() {
        return addCardDialogOpen;
    }

    public void setAddCardDialogOpen(boolean addCardDialogOpen) {
        this.addCardDialogOpen = addCardDialogOpen;
    }

    public String getAddCardDialogMsg() {
        return addCardDialogMsg;
    }

    public void setAddCardDialogMsg(String addCardDialogMsg) {
        this.addCardDialogMsg = addCardDialogMsg;
    }

    /**
     * Get analytics tracker.
     * @return A Tracker
     */
    public synchronized Tracker getTracker() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        analytics.setDryRun(BuildConfig.DEBUG);
        if (BuildConfig.DEBUG) {
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
        }
        return analytics.newTracker(ANALYTICS_PROPERY_KEY);
    }
}
