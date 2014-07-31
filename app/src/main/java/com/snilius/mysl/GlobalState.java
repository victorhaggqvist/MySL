package com.snilius.mysl;

import android.app.Application;

import com.google.gson.JsonObject;

/**
 * Created by victor on 7/20/14.
 */
public class GlobalState extends Application{

    public static final String TAG = "GSMySL";

    private JsonObject mShoppingCart;

    private String mUsername, mPassword;

    private boolean refresh;
    private boolean addCardDialogOpen;
    private String addCardDialogMsg;

    public GlobalState() { }

    public JsonObject getmShoppingCart() {
        return mShoppingCart;
    }

    public void setmShoppingCart(JsonObject shoppingCart) {
        mShoppingCart = shoppingCart;
    }

    public void setUserinfo(String username, String password){
        mUsername = username;
        mPassword = password;
    }

    public String getUsername() {
        return mUsername;
    }

    public String getPassword() {
        return mPassword;
    }

    public boolean isRefresh() {
        return refresh;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    public boolean isAddCardDialogOpen() {
        return addCardDialogOpen;
    }

    public void setAddCardDialogOpen(boolean addCardDialogOpen) {
        this.addCardDialogOpen = addCardDialogOpen;
    }

    public String getAddCardDialogMsg() {
        return addCardDialogMsg;
    }

    public void setAddCardDialogMsg(String addCardDialogMsg) {
        this.addCardDialogMsg = addCardDialogMsg;
    }
}
