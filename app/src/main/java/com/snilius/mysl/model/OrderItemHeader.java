package com.snilius.mysl.model;

/**
 * DTO for GetSalesOrders
 * data->sales_order_list[]->sales_order
 */
public class OrderItemHeader {
    // rdesc
    private int mOrderNumber;

    // order_date_ext
    private String mDate;

    private String mAmountIncVat;

    public OrderItemHeader(int mOrderNumber, String mDate, String mAmountIncVat) {
        this.mOrderNumber = mOrderNumber;
        this.mDate = mDate;
        this.mAmountIncVat = mAmountIncVat;
    }

    public int getOrderNumber() {
        return mOrderNumber;
    }

    public String getDate() {
        return mDate;
    }

    public String getAmountIncVat() {
        return mAmountIncVat;
    }
}
