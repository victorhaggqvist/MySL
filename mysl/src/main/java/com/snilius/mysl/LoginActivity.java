package com.snilius.mysl;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;
import com.snilius.mysl.util.LoginDefinitions;
import com.snilius.mysl.util.TextValidator;

import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends ActionBarActivity implements View.OnClickListener {

    public static final String TAG = "LoginActivity";
    public static final int REQUEST_CODE = 1;

    private Button login, create;
    private EditText username, password;
    private TextView login_error;
    private boolean mLoginInprogress = false;

    private boolean allValid = false;
    private ProgressDialog mProgressDialog;
    private SLApiProvider sl;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(this);
        create = (Button) findViewById(R.id.createAccount);
        create.setOnClickListener(this);
        login_error = (TextView) findViewById(R.id.login_error);

        username.addTextChangedListener(new TextValidator(username) {
            @Override
            public void validate(TextView textView, String text) {
                if (textView.getText().toString().length()<1) {
                    textView.setError("Don't forget username!");
                    allValid = false;
                }else {
                    textView.setError(null);
                    allValid = true;
                }
            }
        });

        password.addTextChangedListener(new TextValidator(password) {
            @Override
            public void validate(TextView textView, String text) {
                if (textView.getText().toString().length()<1) {
                    textView.setError("Please enter your password");
                    allValid = false;
                }else {
                    textView.setError(null);
                    allValid = true;
                }
            }
        });

        //debug stuff
        if (BuildConfig.DEBUG){
            // FIXME Uncomment block in release build
            LoginDefinitions ld = new LoginDefinitions(1);
            username.setText(ld.getUser());
            password.setText(ld.getPass());
        }

        mTracker = ((GlobalState) getApplication()).getTracker();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.login_load_dialog));;
        mProgressDialog.setIndeterminate(true);
    }

    @Override
    public void onClick(View v) {
        if (login.equals(v))
            doLogin();
        else if (create.equals(v))
            startActivity(new Intent(this, CreateAccountActivity.class));
    }

    @Override
    public void onBackPressed() {
        // moves app to end of application stack instead of going to main view
        moveTaskToBack(true);
    }

    private void doLogin() {
        if (mLoginInprogress)
            return;

        if (!allValid) {
            System.out.println("All NOT valid");
            return;
        }

        mProgressDialog.show();
        sl = new SLApiProvider(this);
        String user = username.getText().toString();
        String passwd = password.getText().toString();

        mLoginInprogress = true;
        sl.authenticate(this, new LoginCallback(), user, passwd);
    }

    private class LoginCallback implements FutureCallback<Response<JsonObject>>{

        @Override
        public void onCompleted(Exception e, Response<JsonObject> result) {
            if (e != null) {
                Log.i(TAG, e.toString());
                Toast.makeText(getApplication(), getString(R.string.login_fail), Toast.LENGTH_LONG).show();
                login_error.setText(getString(R.string.error_connectivity));
                login_error.setVisibility(View.VISIBLE);
                mProgressDialog.dismiss();
                mLoginInprogress = false;
                mTracker.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("SL Connection Issue").build());
                return;
            }

            if (result.getHeaders().getResponseCode() != 200){
                if (result.getHeaders().getResponseCode() == 403)
                    login_error.setText(getString(R.string.error_login_credentials));
                else
                    login_error.setText(getString(R.string.error_connectivity));
                login_error.setVisibility(View.VISIBLE);

                mProgressDialog.dismiss();
                Crashlytics.setString("Login response: ", result.getHeaders().getStatusLine());
//                Log.i(TAG, result.getResult().toString());

                mLoginInprogress = false;
                mTracker.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("User Sign In Fail").build());
                return;
            }

            Log.i(TAG, "Auth successfull: " + result.getHeaders().getResponseCode() + result.getHeaders().getResponseMessage());
            sl.getShoppingCart(getApplication(), new GetShoopingCartCallback());
        }
    }

    private class GetShoopingCartCallback implements FutureCallback<Response<JsonObject>>{

        @Override
        public void onCompleted(Exception e, Response<JsonObject> result) {
//            System.out.println(result.getResult());
            JsonObject response = result.getResult();
//            System.out.println("data: " + response.getAsJsonObject("data"));
            boolean doesSLThinkImAutenticated = response.getAsJsonObject("data").get("UserAutenticated").getAsBoolean();
            Log.i(TAG, "SL like us: " + doesSLThinkImAutenticated);

            if (doesSLThinkImAutenticated){
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplication()).edit();
                JsonObject data = response.getAsJsonObject("data");
                JsonObject userSession = data.getAsJsonObject("UserSession");

                editor.putString(getString(R.string.pref_user_fullname), userSession.get("FullName").getAsString());
                editor.putString(getString(R.string.pref_user_firstname), userSession.get("FirstName").getAsString());
                editor.putString(getString(R.string.pref_user_lastname), userSession.get("LastName").getAsString());
                editor.putString(getString(R.string.pref_user_email), userSession.get("Email").getAsString());
                editor.putString(getString(R.string.pref_user_username), username.getText().toString());
                editor.putString(getString(R.string.pref_user_password), password.getText().toString());
                editor.commit();

                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    byte[] hash = digest.digest(userSession.get("Email").getAsString().getBytes("UTF-8"));
                    editor.putString(getString(R.string.pref_user_uid), hash.toString()).commit();
                    mTracker.set("&uid", hash.toString());
                } catch (NoSuchAlgorithmException e1) {
                    e1.printStackTrace();
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }

                mTracker.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("User Sign In").build());

                ((GlobalState) getApplication()).setmShoppingCart(data);

                FileOutputStream outputStream;
                try {
                    outputStream = openFileOutput(getString(R.string.file_shoppingcart), Context.MODE_PRIVATE);
                    outputStream.write(data.toString().getBytes());
                    outputStream.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                finish();
            }

//            System.out.println(response.getAsJsonObject("data").get("UserAutenticated").getAsBoolean());
            mProgressDialog.dismiss();
            mLoginInprogress = false;
        }
    }
}
