package com.snilius.mysl.model;

/**
 * DTO for GetSalesOrders
 * data->sales_order_list[]->sales_order->order_lines[]->order_lines_ext
 */
public class OrderItemChild {
    private int mQuantity;
    private String mDescription;
    private String mTotalPriceIncVat;

    public OrderItemChild(int mQuantity, String mDescription, String mTotalPriceIncVat) {
        this.mQuantity = mQuantity;
        this.mDescription = mDescription;
        this.mTotalPriceIncVat = mTotalPriceIncVat;
    }

    public int getQuantity() {
        return mQuantity;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getTotalPriceIncVat() {
        return mTotalPriceIncVat;
    }
}
