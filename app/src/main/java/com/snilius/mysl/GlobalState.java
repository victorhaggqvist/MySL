package com.snilius.mysl;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by victor on 7/20/14.
 */
public class GlobalState extends Application{

    public static final String TAG = "GSMySL";

    private JsonObject userInfo;

    public GlobalState() {


    }

    public JsonObject getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(JsonObject userInfo) {
        this.userInfo = userInfo;
    }
}
