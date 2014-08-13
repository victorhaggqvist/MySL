package com.snilius.mysl.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.snilius.mysl.model.CardProduct;

import java.util.ArrayList;

/**
 * Created by victor on 7/27/14.
 */
public class DetailCardHelper {
    private static DetailCardHelper holder;
    private JsonObject data;
    private ArrayList<CardProduct> products;

    private DetailCardHelper(){}

    public static DetailCardHelper with(JsonObject jsonObject){
        holder = new DetailCardHelper();
        holder.setData(jsonObject);
        return holder;
    }

    private void setData(JsonObject data) {
        this.data = data;
    }

    public String getPurseValueExt(){
        return data.getAsJsonObject("travel_card")
                .getAsJsonObject("detail")
                .get("purse_value_ext").getAsString();
    }

    public ArrayList<CardProduct> getProducts() {
        JsonArray productList = data.getAsJsonObject("travel_card").getAsJsonArray("products");
        products = new Gson().fromJson(productList, new TypeToken<ArrayList<CardProduct>>(){}.getType());
        return products;
    }

    public boolean hasProcucts(){
        return data.getAsJsonObject("travel_card").has("products");
    }

    public boolean hasPurse() {
        return data.getAsJsonObject("travel_card").getAsJsonObject("detail").get("purse_valid").getAsBoolean() != false;
    }
}
