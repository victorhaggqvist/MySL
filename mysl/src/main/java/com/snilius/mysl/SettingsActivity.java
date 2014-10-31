package com.snilius.mysl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */

    private int fool = 0;
    private Tracker mTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTracker = ((GlobalState) getApplication()).getTracker();
        setupSimplePreferencesScreen();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            // TODO: If Settings has multiple levels, Up should navigate up
            // that hierarchy.
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


//    @Override
//    protected void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//
//
//    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        addPreferencesFromResource(R.xml.prefs);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference("lang"));
        loadVersioninfo();
    }

    private void loadVersioninfo(){
        Preference about_version = findPreference("about_version");
        about_version.setSummary("Version " + GlobalState.APP_VERSION);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals("about_legal")){
            startActivity(new Intent(this,AboutActivity.class));
        }else if (preference.getKey().equals("about_contact")){
            mTracker.send(new HitBuilders.EventBuilder().setCategory("About").setAction("Email").build());
            final Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            emailIntent.putExtra(
                    android.content.Intent.EXTRA_EMAIL,
                    new String[]{getString(R.string.send_feedback_email_emailaddress)});
            emailIntent.putExtra(
                    android.content.Intent.EXTRA_SUBJECT,
                    getText(R.string.send_feedback_email_title));
            startActivity(Intent.createChooser(emailIntent, getText(R.string.send_email)));
        }else if (preference.getKey().equals("about_rate")) {
            try {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("About").setAction("Rate").build());
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.snilius.mysl")));
            } catch (android.content.ActivityNotFoundException anfe) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("About").setAction("Rate").setLabel("No Google Play").build());
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.snilius.mysl")));
            }
        }else if (preference.getKey().equals("about_version")){
            fool++;
            if (fool == 15) {
                Toast.makeText(this, "Die in 5", Toast.LENGTH_SHORT).show();
                mTracker.send(new HitBuilders.EventBuilder().setCategory("About").setAction("About to die").build());
            }else if (fool == 20) {
                mTracker.send(new HitBuilders.EventBuilder().setCategory("About").setAction("Intentional death").build());
                int die = 1/0;
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference.getKey().equals(preference.getContext().getString(R.string.pref_lang))){
                String key = PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), "");

                String language = value.toString();
                if (language.equals("en"))
                    preference.setSummary(preference.getContext().getString(R.string.pref_lang_en));
                else if (language.equals("sv"))
                    preference.setSummary(preference.getContext().getString(R.string.pref_lang_sv));
            }else if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("lang")){
            ((GlobalState) getApplication()).reloadLocaleForApplication();
            String lang = sharedPreferences.getString(getString(R.string.pref_lang),"");
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Settings")
                    .setAction("Language").setLabel(lang).build());
            Toast.makeText(this, getString(R.string.lang_change_app_restart), Toast.LENGTH_LONG).show();
        }else {
            String refreshPref = sharedPreferences.getBoolean(getString(R.string.pref_refresh_on_start), true)?"1":"0";
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Settings")
                    .setAction("Refresh on start").setLabel(refreshPref).build());
        }
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        NavUtils.navigateUpFromSameTask(this);
//    }
}
