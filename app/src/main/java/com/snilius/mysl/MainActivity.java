package com.snilius.mysl;

import android.app.Activity;

import android.app.ActionBar;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.snilius.mysl.util.Helper;

import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends Activity
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

//    public static final String TRANSFER_KEY = "com.snilius.mysl.TRANSFER_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        username = preferences.getString(getString(R.string.pref_user_username), "");
        password = preferences.getString(getString(R.string.pref_user_password), "");
        if (password.length()<1) {
            Log.d(TAG, "Login start flow");
            startActivityForResult(new Intent(this, LoginActivity.class), LoginActivity.REQUEST_CODE);
        }else {
            Log.d(TAG, "Regular start flow");
            loadUserInfoFile();
        }

    }

    private void loadUserInfoFile() {
        InputStream inputStream;
        String userinfoFile= null;
        try {
            userinfoFile = Helper.openFile(this, getString(R.string.file_shoppingcart));
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            FileInputStream fis = openFileInput(getString(R.string.file_userinfo));
//            inputStream = new BufferedInputStream(fis);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//
//            String line = reader.readLine();
//            while (line != null){
//                userinfoFile +=line;
//                line = reader.readLine();
//            }
//
//            reader.close();
//            inputStream.close();
//        } catch (FileNotFoundException e) {
//            // user not logged in yet
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

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

        FragmentManager fragmentManager = getFragmentManager();
        if (password.length()<1)
            return;
        switch (position) {
            case 0:
                fragmentManager.beginTransaction()
                    .replace(R.id.container, CardListFragment.newInstance(username, password, refresh))
                    .commit();
                break;
            case 1:
                fragmentManager.beginTransaction()
                    .replace(R.id.container, OnlineOrdersFragment.newInstance())
                    .commit();
                break;
            case 3:
                signOutUser();
                break;
        }
    }

    private void signOutUser() {
        deleteFile(getString(R.string.file_shoppingcart));
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
        Log.i(TAG, "User signout, everything cleanup");
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
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LoginActivity.REQUEST_CODE){
//            Toast.makeText(this,"Login done", Toast.LENGTH_SHORT).show();
            mNavigationDrawerFragment.selectItem(0);
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

//    /**
//     * A placeholder fragment containing a simple view.
//     */
//    public static class PlaceholderFragment extends Fragment {
//        /**
//         * The fragment argument representing the section number for this
//         * fragment.
//         */
//        private static final String ARG_SECTION_NUMBER = "section_number";
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static PlaceholderFragment newInstance(int sectionNumber) {
//            PlaceholderFragment fragment = new PlaceholderFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
//            return rootView;
//        }
//
//        @Override
//        public void onAttach(Activity activity) {
//            super.onAttach(activity);
//            ((MainActivity) activity).onSectionAttached(
//                    getArguments().getInt(ARG_SECTION_NUMBER));
//        }
//    }

}
