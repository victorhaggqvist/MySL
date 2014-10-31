package com.snilius.mysl;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.snilius.mysl.util.Helper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final String TAG = "MainActivity";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private String username, password;
    private SharedPreferences preferences;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (!BuildConfig.DEBUG)
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        username = preferences.getString(getString(R.string.pref_user_username), "");
        password = preferences.getString(getString(R.string.pref_user_password), "");

        mTracker = ((GlobalState) getApplication()).getTracker();

        if (password.length()<1) {
            Log.d(TAG, "Login start flow");
            startActivityForResult(new Intent(this, LoginActivity.class), LoginActivity.REQUEST_CODE);
        }else {
            String fullName = preferences.getString(getString(R.string.pref_user_fullname),"");
            String email = preferences.getString(getString(R.string.pref_user_email),"");
            String uid = preferences.getString(getString(R.string.pref_user_uid),"");
            mNavigationDrawerFragment.setUserIfno(fullName, email);

            if (uid.length()<1){
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] hash = digest.digest(email.getBytes("UTF-8"));
                    uid = hash.toString();
                    preferences.edit().putString(getString(R.string.pref_user_uid), uid).commit();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            mTracker.set("&uid", uid);
            Log.d(TAG, "Regular start flow");
            loadUserInfoFile();
        }
    }

    private void loadUserInfoFile() {
        String userinfoFile= null;
        try {
            userinfoFile = Helper.openFile(this, getString(R.string.file_shoppingcart));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (userinfoFile.length()>0) {
            Log.i(TAG, "Userinfo file loaded");

            try {
                JsonObject userInfo = new JsonParser().parse(userinfoFile).getAsJsonObject();
                ((GlobalState) getApplication()).setmShoppingCart(userInfo);
            } catch (JsonSyntaxException e) {
                System.out.println("User info file has broken json");
//            e.printStackTrace();
            }
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        username = preferences.getString(getString(R.string.pref_user_username), "");
        password = preferences.getString(getString(R.string.pref_user_password), "");
        ((GlobalState) getApplication()).setUserinfo(username, password);

        boolean refresh = preferences.getBoolean(getString(R.string.pref_refresh_on_start),true);
        ((GlobalState) getApplication()).setRefresh(refresh);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (password.length()<1)
            return;
        switch (position) {
            case 0:
                fragmentManager.beginTransaction()
                    .replace(R.id.container, CardListFragment.newInstance())
                    .commitAllowingStateLoss();
                break;
            case 1:
                fragmentManager.beginTransaction()
                    .replace(R.id.container, OnlineOrdersFragment.newInstance())
                    .commit();
                break;
            case 2:
                signOutUser();
                break;
        }
    }

    private void signOutUser() {
        deleteFile(getString(R.string.file_shoppingcart));
        deleteFile(getString(R.string.file_orders));
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
        Log.i(TAG, "User signout, everything cleanup");
        if (null == mTracker)
            mTracker = ((GlobalState) getApplication()).getTracker();
        mTracker.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("User Sign Out").build());
        startActivityForResult(new Intent(this, LoginActivity.class), LoginActivity.REQUEST_CODE);
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section_mycards);
                break;
            case 2:
                mTitle = getString(R.string.title_section_onlineorders);
                break;
            case 3:
                mTitle = getString(R.string.title_section_signout);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LoginActivity.REQUEST_CODE){
            mNavigationDrawerFragment.selectItem(0);
            String fullName = preferences.getString(getString(R.string.pref_user_fullname),"");
            String email = preferences.getString(getString(R.string.pref_user_email),"");
            mNavigationDrawerFragment.setUserIfno(fullName, email);
//            onNavigationDrawerItemSelected(0);
        }
//        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
