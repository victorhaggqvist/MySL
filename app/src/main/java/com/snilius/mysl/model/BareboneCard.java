package com.snilius.mysl.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by victor on 7/21/14.
 */
public class BareboneCard {
    @SerializedName("ProductCount")
    @Expose
    private int productCount;
    @SerializedName("TravelPurseCount")
    @Expose
    private int travelPurseCount;
    @SerializedName("CanAddProduct")
    @Expose
    private boolean canAddProduct;
    @SerializedName("CanAddTravelPurse")
    @Expose
    private boolean canAddTravelPurse;
    @SerializedName("Info")
    @Expose
    private String info;
    @SerializedName("Serial")
    @Expose
    private String serial;
    @SerializedName("IsNew")
    @Expose
    private boolean isNew;
    @SerializedName("IsBlocked")
    @Expose
    private boolean isBlocked;
    @SerializedName("PurseValue")
    @Expose
    private int purseValue;
    @SerializedName("PurseValueFull")
    @Expose
    private boolean purseValueFull;
    @SerializedName("PurseEnabled")
    @Expose
    private boolean purseEnabled;
    @SerializedName("CouponCount")
    @Expose
    private int couponCount;
    @SerializedName("PassengerType")
    @Expose
    private String passengerType;
    @SerializedName("Expires")
    @Expose
    private String expires;
    @SerializedName("Blocked")
    @Expose
    private Object blocked;
    @SerializedName("ProductObject")
    @Expose
    private Object productObject;
    @SerializedName("Id")
    @Expose
    private String id;
    @SerializedName("Name")
    @Expose
    private String name;
    @SerializedName("PriceIncVat")
    @Expose
    private int priceIncVat;
    @SerializedName("Description")
    @Expose
    private Object description;

    public String getSerial() {
        return serial;
    }

    public String getName() {
        return name;
    }

    public boolean isPurseEnabled() {
        return purseEnabled;
    }

    public int getPurseValue() {
        return purseValue;
    }

    public boolean isPurseValueFull() {
        return purseValueFull;
    }

    public int getCouponCount() {
        return couponCount;
    }

    public String getPassengerType() {
        return passengerType;
    }

    public String getExpires() {
        return expires;
    }

    public String getId() {
        return id;
    }
}
