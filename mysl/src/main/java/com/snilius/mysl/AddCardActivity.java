package com.snilius.mysl;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;
import com.snilius.mysl.util.TextValidator;

import java.util.concurrent.TimeoutException;


public class AddCardActivity extends Activity {
    public static final String TAG = "AddCardActivity";

    private TextView num1, num2, name;
    private boolean allValid;
    private JsonObject postBody;
    private SLApiProvider sl;
    private ProgressBar progressBar;
    private AlertDialog alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        num1 = (TextView) findViewById(R.id.add_cardnum1);
        num1.addTextChangedListener(new TextValidator(num1) {
            @Override
            public void validate(TextView textView, String text) {
                if (textView.getText().toString().length() != 10) {
                    textView.setError("Number should be 10 digits");
                    allValid = false;
                }else {
                    textView.setError(null);
                    allValid = true;
                }
            }
        });
        num2 = (TextView) findViewById(R.id.add_cardnum2);
        num2.addTextChangedListener(new TextValidator(num2) {
            @Override
            public void validate(TextView textView, String text) {
                if (!text.equals(num1.getText().toString()) || textView.getText().toString().length() != 10){
                    textView.setError("Number should be 10 digits");
                    allValid = false;
                }else if (!text.equals(num1.getText().toString())) {
                    textView.setError("Numbers don't match");
                    allValid = false;
                } else {
                    textView.setError(null);
                    allValid = true;
                }
            }
        });
        name = (TextView) findViewById(R.id.add_cardname);
        name.addTextChangedListener(new TextValidator(name) {
            @Override
            public void validate(TextView textView, String text) {
                validText(textView, "Specify a name");
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.add_progress);
        progressBar.setIndeterminate(true);

        if (((GlobalState) getApplication()).isAddCardDialogOpen()){
            failPopup(((GlobalState) getApplication()).getAddCardDialogMsg());
        }
    }

    private void validText(TextView textView, String msg) {
        if (textView.getText().toString().length()<1) {
            textView.setError(msg);
            allValid = false;
        }else {
            textView.setError(null);
            allValid = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_card, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add_card) {
            addCard();
        }
        return super.onOptionsItemSelected(item);
    }

    private void addCard() {
        if (allValid) {
            postBody = new JsonObject();
            Gson gson = new Gson();
            JsonObject card = new JsonObject();
            String number[] = new String[]{num1.getText().toString().substring(0,5), num1.getText().toString().substring(5,10)};

            card.add("serial_number", gson.toJsonTree(number));
            card.add("serial_number_verify", gson.toJsonTree(number));
            card.addProperty("name", name.getText().toString());
            postBody.add("travel_card", card);


            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String username = preferences.getString(getString(R.string.pref_user_username), "");
            String password = preferences.getString(getString(R.string.pref_user_password),"");
            sl = new SLApiProvider(this);
            progressBar.setVisibility(View.VISIBLE);
            System.out.println(postBody);
            sl.authenticate(this, new LoginCallback(), username, password);

            Toast.makeText(this, "wop", Toast.LENGTH_SHORT).show();
        }else
            Toast.makeText(this, getString(R.string.fill_in_all), Toast.LENGTH_SHORT).show();
    }

    private void failPopup(String msg){
        ((GlobalState) getApplication()).setAddCardDialogMsg(msg);
        alert = new AlertDialog.Builder(this)
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                ((GlobalState) getApplication()).setAddCardDialogOpen(false);
            }
        });
        ((GlobalState) getApplication()).setAddCardDialogOpen(true);
        alert.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != alert && alert.isShowing()) {
            alert.dismiss();
            ((GlobalState) getApplication()).setAddCardDialogOpen(true);
        }
    }

    private class LoginCallback implements FutureCallback<Response<JsonObject>> {
        @Override
        public void onCompleted(Exception e, Response<JsonObject> result) {
            if (null == result) {
                if (e instanceof TimeoutException) {
                    Log.i(TAG, "Login, Connection Timeout");
                    Toast.makeText(getApplicationContext(), getString(R.string.error_connectivity), Toast.LENGTH_LONG).show();
                }else
                    Log.i(TAG, "Login Canceled");
            }else if (result.getHeaders().getResponseCode() == 200){
                Log.d(TAG, "Login successfull: " + result.getHeaders().getResponseCode() + result.getHeaders().getResponseMessage());
                if (postBody != null)
                    sl.registerTravelCard(getApplicationContext(), new RegisterTravelCardCallback(), postBody);
            }else {
                Log.d(TAG, "Login failed: " + result.getHeaders().getResponseCode() + result.getHeaders().getResponseMessage());
            }

        }

        private class RegisterTravelCardCallback implements FutureCallback<Response<JsonObject>> {
            @Override
            public void onCompleted(Exception e, Response<JsonObject> result) {
                if (null == result) {
                    if (e instanceof TimeoutException) {
                        Log.i(TAG, "RegisterTravelCard, Connection Timeout");
                        Toast.makeText(getApplicationContext(), getString(R.string.error_connectivity), Toast.LENGTH_LONG).show();
                    }else
                        Log.i(TAG, "RegisterTravelCard Canceled");
                }else if (result.getHeaders().getResponseCode() != 200) {
                    JsonObject jsonResult = result.getResult();
                    if (jsonResult.get("status").getAsString().equals("error")){
                        final String msg = jsonResult.getAsJsonObject("data").getAsJsonArray("ResultErrors").get(0).getAsString();
                        failPopup(msg);
                    }
                }else if (result.getHeaders().getResponseCode() == 200){
                    Log.d(TAG, "RegisterTravelCard successfull: " + result.getHeaders().getResponseCode() + result.getHeaders().getResponseMessage());
                    Toast.makeText(getApplicationContext(), getString(R.string.card_registered_success), Toast.LENGTH_LONG).show();
                }else {
                    Log.d(TAG, "RegisterTravelCard failed: " + result.getHeaders().getResponseCode() + result.getHeaders().getResponseMessage());
                }
                progressBar.setVisibility(View.GONE);
            }
        }
    }
}
