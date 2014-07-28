package com.snilius.mysl.util;

import com.google.gson.JsonObject;

/**
 * Created by victor on 7/27/14.
 */
public class DetailCardHelper {
    private static DetailCardHelper holder;
    private JsonObject data;

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
}
