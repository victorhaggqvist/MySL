package com.snilius.mysl;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;


public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // set appname with version dynamicly
        TextView version = (TextView) findViewById(R.id.version);
        version.setText(getString(R.string.app_name) + " " + GlobalState.APP_VERSION);

        // REVIEW Maby remove SSL mention in about text, might just be confusing for user?
    }
}
