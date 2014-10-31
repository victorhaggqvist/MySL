package com.snilius.mysl;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.JsonObject;

import java.util.Locale;

import timber.log.Timber;

/**
 * @author Victor Häggqvist
 * @since 7/20/14
 */
public class GlobalState extends Application{
    public static String APP_VERSION = BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + (BuildConfig.DEBUG?"-dev":"") + ")";

    private JsonObject mShoppingCart;

    private String mUsername, mPassword;

    private boolean refresh;
    private boolean addCardDialogOpen;
    private String addCardDialogMsg;
    private Tracker mTracker;

    public GlobalState() { }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());
        else
            Timber.plant(new CrashReportingTree());

        reloadLocaleForApplication();
        mTracker = setupTracker();
    }

    public void reloadLocaleForApplication() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        String language = sharedPreferences.getString(getString(R.string.pref_lang), "system");
        Timber.d("Preferred language: " + language);

        Locale locale = null;
        if ("sv".equals(language)) {
            locale = new Locale("sv", "SE");
        } else if ("es".equals(language)) {
            locale = new Locale("es", "ES");
        } else if ("en".equals(language)) {
            locale = Locale.ENGLISH;
        }

        if (locale != null) {
            Timber.d("setting locale " + locale);
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
    private synchronized Tracker setupTracker() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        analytics.enableAutoActivityReports(this);
        analytics.setDryRun(BuildConfig.DEBUG);
        if (BuildConfig.DEBUG) {
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            analytics.setLocalDispatchPeriod(10);
        }
        return analytics.newTracker(R.xml.tracker);
    }

    public synchronized Tracker getTracker() {
        if (mTracker == null)
            mTracker = setupTracker();
        return mTracker;
    }

    private static class CrashReportingTree extends Timber.HollowTree {
        @Override public void i(String message, Object... args) {
            log(message, args);
        }

        @Override public void i(Throwable t, String message, Object... args) {
            log(message, args);
        }

        @Override public void e(String message, Object... args) {
            log("ERROR: " + message, args);
        }

        @Override public void e(Throwable t, String message, Object... args) {
            log(message, args);
            Crashlytics.logException(t);
        }

        private void log(String msg, Object... args){
            Crashlytics.log(String.format(msg, args));
        }
    }
}
