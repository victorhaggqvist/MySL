package com.snilius.mysl;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;
import com.snilius.mysl.util.TextValidator;

import java.util.concurrent.TimeoutException;

import timber.log.Timber;


public class CreateAccountActivity extends ActionBarActivity {

    private boolean allValid = false;

    private TextView username;
    private TextView fname;
    private TextView lname;
    private TextView email;
    private TextView pw1;
    private TextView pw2;
    private TextView ssn;
    private CheckBox terms;
    private Tracker mTracker;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        username = (TextView) findViewById(R.id.create_username);
        username.addTextChangedListener(new TextValidator(username) {
            @Override
            public void validate(TextView textView, String text) {
                validText(textView, getString(R.string.create_err_username));
            }
        });

        fname = (TextView) findViewById(R.id.create_fname);
        fname.addTextChangedListener(new TextValidator(fname) {
            @Override
            public void validate(TextView textView, String text) {
                validText(textView, getString(R.string.create_err_fname));
            }
        });

        lname = (TextView) findViewById(R.id.create_lname);
        lname.addTextChangedListener(new TextValidator(lname) {
            @Override
            public void validate(TextView textView, String text) {
                validText(textView, getString(R.string.create_err_lname));
            }
        });

        email = (TextView) findViewById(R.id.create_email);
        email.addTextChangedListener(new TextValidator(email) {
            @Override
            public void validate(TextView textView, String text) {
                validText(textView, getString(R.string.create_err_email));
            }
        });
        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && !android.util.Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
                    email.setError(getString(R.string.create_err_invalid_email));
                    allValid = false;
                }
            }
        });

        pw1 = (TextView) findViewById(R.id.create_pw1);
        pw1.addTextChangedListener(new TextValidator(pw1) {
            @Override
            public void validate(TextView textView, String text) {
                validText(textView, getString(R.string.create_err_pw));
            }
        });

        pw2 = (TextView) findViewById(R.id.create_pw2);
        pw2.addTextChangedListener(new TextValidator(pw2) {
            @Override
            public void validate(TextView textView, String text) {
                if (!text.equals(pw1.getText().toString()) && textView.getText().toString().length() < 1){
                    textView.setError(getString(R.string.create_err_pw));
                    allValid = false;
                }else if (!text.equals(pw1.getText().toString())) {
                    textView.setError(getString(R.string.create_err_pw_nomatch));
                    allValid = false;
                } else {
                    textView.setError(null);
                    allValid = true;
                }

            }
        });

        ssn = (TextView) findViewById(R.id.create_personnummer);
        ssn.addTextChangedListener(new TextValidator(ssn) {
            @Override
            public void validate(TextView textView, String text) {
                if (!text.matches("^[0-9]{8}-[0-9]{4}$") && textView.getText().toString().length() < 1){
                    textView.setError(getString(R.string.create_err_ssn));
                    allValid = false;
                }else if (!text.matches("^[0-9]{8}-[0-9]{4}$")) {
                    textView.setError(getString(R.string.create_err_invalid_ssn));
                    allValid = false;
                } else {
                    textView.setError(null);
                    allValid = true;
                }
            }
        });

        terms = (CheckBox) findViewById(R.id.create_agreeterms);

        mTracker = ((GlobalState) getApplication()).getTracker();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.create_account_creating));
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
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
        getMenuInflater().inflate(R.menu.create_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_create_account) {
            createAccount();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createAccount() {
        if (allValid && terms.isChecked()){
            JsonObject json = new JsonObject();

            JsonObject userAccount = new JsonObject();
            userAccount.addProperty("username", username.getText().toString());
            userAccount.addProperty("password", pw1.getText().toString());
            userAccount.addProperty("password_verify", pw2.getText().toString());

            JsonObject cust = new JsonObject();
            cust.addProperty("first_name", fname.getText().toString());
            cust.addProperty("last_name", lname.getText().toString());
            cust.addProperty("email", email.getText().toString());
            String[] ssnSplit = ssn.getText().toString().split("-");
            if (ssnSplit.length==2) {
                cust.addProperty("birth_date", ssnSplit[0]);
                cust.addProperty("person_number", ssnSplit[1]);
            } else {
                ssn.setError(getString(R.string.create_err_invalid_ssn));
                allValid = false;
                return;
            }

            userAccount.add("private_customer", cust);

            json.add("user_account", userAccount);

            json.addProperty("terms_accepted", terms.isChecked());
            json.addProperty("direct_advertising", false);

            SLApiProvider sl = new SLApiProvider(this);
            progressDialog.show();
            sl.createAccount(this, new CreateAccountCallback(), json);
            mTracker.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("Create account").setLabel("Valid submission").build());
        }else {
            mTracker.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("Create account").setLabel("Invalid submission").build());
        }
    }

    public void showTerms(View view) {
        mTracker.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("Create account").setLabel("View terms").build());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_terms, null))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        builder.create().show();
    }

    private class CreateAccountCallback implements FutureCallback<Response<JsonObject>> {
        @Override
        public void onCompleted(Exception e, Response<JsonObject> result) {
            progressDialog.dismiss();
            if (null == result){
                Timber.e("Create fail");
                if (e instanceof TimeoutException)
                    Toast.makeText(getApplicationContext(), getString(R.string.error_connectivity), Toast.LENGTH_LONG).show();
                mTracker.send(new HitBuilders.EventBuilder().setCategory("Network").setAction("Create account").setLabel("Network fail").build());
            }else if (result.getHeaders().getResponseCode() == 200){
                Toast.makeText(getApplicationContext(), getString(R.string.account_created), Toast.LENGTH_LONG).show();
                mTracker.send(new HitBuilders.EventBuilder().setCategory("UX").setAction("Create account").setLabel("Successfull creation").build());
                finish();
            }
        }
    }
}
