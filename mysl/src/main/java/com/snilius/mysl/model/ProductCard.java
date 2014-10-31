package com.snilius.mysl.model;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.snilius.mysl.R;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Created by victor on 8/8/14.
 */
public class ProductCard extends Card {
    private String productStart;
    private int productPrice;
    private String productEnd;
    private CardHeader mCardHeader;

    public ProductCard(Context context, String productType, String productStart,
                       String productEnd, int productPrice) {
        this(context, R.layout.list_productcard);
        this.productStart = productStart;
        this.productPrice = productPrice;
        mCardHeader.setTitle(productType);
        this.productEnd = productEnd;
    }

    public ProductCard(Context context, int innerLayout) {
        super(context, innerLayout);
        mCardHeader = new CardHeader(getContext());
        addCardHeader(mCardHeader);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        TextView range = (TextView) view.findViewById(R.id.product_range);
        TextView price = (TextView) view.findViewById(R.id.product_price);

        if (range != null) {
            if (null == productEnd)
                range.setText(productStart);
            else
                range.setText(productStart + " - " + productEnd);
        }

        if (price != null)
            price.setText(Integer.toString(productPrice)+"kr");
    }
}
