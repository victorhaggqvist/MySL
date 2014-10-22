package com.snilius.mysl;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.koushikdutta.ion.cookie.CookieMiddleware;

import org.apache.http.NameValuePair;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Created by victor on 7/19/14.
 */
public class SLApiProvider{
    public static final String API_ENDPOINT = "https://sl.se/api";
    public static final String MYSL_ENDPOINT = API_ENDPOINT+"/MySL";
    public static final String ECOM_ENDPOINT = API_ENDPOINT+"/ECommerse";

    public static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36\n";

    private static SLApiProvider mSlApiProvider;
    private Object requestGroup;
    private long sessionStart;
    private Context mContext;

    public SLApiProvider(Context context) {
        mContext = context;
        requestGroup = new Object();
//        Ion.getDefault(context).configure().proxy("192.168.1.238", 8888);
//        Ion.getDefault(context).configure().setLogging("MyLogs", Log.DEBUG);
    }

    public void authenticate(Context context, FutureCallback<Response<JsonObject>> callback, String username, String password){

        // workaround since ion just merges all cookies which results in duplicates
        new CookieMiddleware(context,"ion").clear();

        sessionStart = System.currentTimeMillis();
        JsonObject json = new JsonObject();
        json.addProperty("username", username);
        json.addProperty("password", password);
        Ion.with(context)
                .load(MYSL_ENDPOINT + "/Authenticate")
                .noCache()
                .setHeader("User-Agent", USER_AGENT)
                .setHeader("Referer", "https://sl.se/sv/mitt-sl/inloggning/")
                .setHeader("Content-Type", "application/json;charset=UTF-8")
                .setJsonObjectBody(json)
                .group(requestGroup)
                .asJsonObject()
                .withResponse()
                .setCallback(callback);
    }

    public void getShoppingCart(Context context, FutureCallback<Response<JsonObject>> callback){
        Ion.with(context)
                .load(ECOM_ENDPOINT + "/GetShoppingCart")
                .setHeader("User-Agent", USER_AGENT)
                .setHeader("Referer", "https://sl.se/sv/mitt-sl/konto/")
                .setHeader("Accept", "application/json, text/plain, */*")
                .setHeader("DNT", "1")
                .setHeader("Accept-Encoding", "gzip,deflate,sdch")
                .setHeader("Accept-Language", "en-US,en;q=0.8,sv;q=0.6")
                .setHeader("Pragma", "no-cache")
                .group(requestGroup)
                .asJsonObject()
                .withResponse()
                .setCallback(callback);
    }

    public void getTravelCardDetails(Context context, FutureCallback<Response<JsonObject>> callback, String travelCardId){
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("reference", travelCardId);
        Ion.with(context)
                .load(MYSL_ENDPOINT + "/GetTravelCardDetails")
                .setHeader("User-Agent", USER_AGENT)
                .setHeader("Referer", "https://sl.se/sv/mitt-sl/konto/")
                .setHeader("Accept", "application/json, text/plain, */*")
                .setHeader("Pragma", "no-cache")
                .setHeader("Content-Type", "application/json;charset=UTF-8")
                .setJsonObjectBody(requestBody)
                .group(requestGroup)
                .asJsonObject()
                .withResponse()
                .setCallback(callback);
    }

    public void getSalesOrders(Context context, FutureCallback<Response<JsonObject>> callback){
//        System.out.println("coookiess "+new CookieMiddleware(context,"ion").getCookieStore().getCookies());
        Ion.with(context)
                .load(MYSL_ENDPOINT + "/GetSalesOrders")
                .setHeader("User-Agent", USER_AGENT)
                .setHeader("Referer", "https://sl.se/sv/mitt-sl/konto/")
                .setHeader("Accept", "application/json, text/plain, */*")
                .setHeader("Pragma", "no-cache")
                .group(requestGroup)
                .asJsonObject()
                .withResponse()
                .setCallback(callback);
    }

    public void createAccount(Context context, FutureCallback<Response<JsonObject>> callback, JsonObject postBody){
        Ion.with(context)
                .load(MYSL_ENDPOINT + "/CreateAccount")
                .setHeader("User-Agent", USER_AGENT)
                .setHeader("Referer", "https://sl.se/sv/mitt-sl/inloggning//")
                .setHeader("Accept", "application/json, text/plain, */*")
                .setHeader("Pragma", "no-cache")
                .setHeader("Content-Type", "application/json;charset=utf-8")
                .setJsonObjectBody(postBody)
                .group(requestGroup)
                .asJsonObject()
                .withResponse()
                .setCallback(callback);
    }

    public void  registerTravelCard(Context context, FutureCallback<Response<JsonObject>> callback, JsonObject postBody){
        Ion.with(context)
                .load(MYSL_ENDPOINT + "/RegisterTravelCard")
                .setHeader("User-Agent", USER_AGENT)
                .setHeader("Referer", "https://sl.se/sv/mitt-sl/inloggning//")
                .setHeader("Accept", "application/json, text/plain, */*")
                .setHeader("Pragma", "no-cache")
                .setHeader("Content-Type", "application/json;charset=utf-8")
                .setJsonObjectBody(postBody)
                .group(requestGroup)
                .asJsonObject()
                .withResponse()
                .setCallback(callback);
    }

    public void forgotUsername(Context context, FutureCallback<Response<JsonObject>> callback, String email){
        JsonObject postBody = new JsonObject();
        JsonObject sub = new JsonObject();
        sub.addProperty("email",email);
        postBody.add("user_account",sub);
        Ion.with(context)
                .load(MYSL_ENDPOINT + "/ForgotUsername")
                .setHeader("User-Agent", USER_AGENT)
                .setHeader("Referer", "https://sl.se/sv/mitt-sl/inloggning//")
                .setHeader("Accept", "application/json, text/plain, */*")
                .setHeader("Pragma", "no-cache")
                .setHeader("Content-Type", "application/json;charset=utf-8")
                .setJsonObjectBody(postBody)
                .group(requestGroup)
                .asJsonObject()
                .withResponse()
                .setCallback(callback);
    }

    public void killRequests(){
        Ion.getDefault(mContext).cancelAll(requestGroup);
    }
}
