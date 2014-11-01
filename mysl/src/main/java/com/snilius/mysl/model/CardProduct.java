package com.snilius.mysl.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by victor on 8/8/14.
 */
public class CardProduct {
    @Expose
    private boolean blocked;
    @SerializedName("end_date")
    @Expose
    private String endDate;
    @Expose
    private String id;
    @SerializedName("product_id")
    @Expose
    private String productId;
    @SerializedName("product_type")
    @Expose
    private String productType;
    @SerializedName("product_price")
    @Expose
    private int productPrice;
    @SerializedName("start_date")
    @Expose
    private String startDate;
    @Expose
    private Object zones;
    @Expose
    private Object routes;
    @Expose
    private boolean active;
    @SerializedName("start_date_ext")
    @Expose
    private String startDateExt;
    @SerializedName("end_date_ext")
    @Expose
    private String endDateExt;

    public String getEndDate() {
        return endDate;
    }

    public String getEndDateExt() {
        return endDateExt;
    }

    public String getProductType() {
        return productType;
    }

    public int getProductPrice() {
        return productPrice;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getStartDateExt() {
        return startDateExt;
    }

    public int getProductIdHash() {
        return productId.hashCode();
    }
}
