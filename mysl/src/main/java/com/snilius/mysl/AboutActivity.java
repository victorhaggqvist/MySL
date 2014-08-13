package com.snilius.mysl;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;


public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo("com.snilius.mysl",0);
        } catch (PackageManager.NameNotFoundException e) {
            //nop
        }

        // set appname with version dynamicly
        TextView version = (TextView) findViewById(R.id.version);
        version.setText(getString(R.string.app_name)+((info!=null)?" v"+info.versionName+" ("+info.versionCode+")":""));
        Tracker t = ((GlobalState) getApplication()).getTracker();
        t.setScreenName("AboutView");
        t.send(new HitBuilders.AppViewBuilder().build());
    }
}
