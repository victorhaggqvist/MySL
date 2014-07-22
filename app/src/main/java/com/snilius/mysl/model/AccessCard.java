package com.snilius.mysl.model;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.snilius.mysl.R;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Created by victor on 7/21/14.
 */
public class AccessCard extends Card {

    protected TextView number, type, expire, price, purse, passengerType;
    protected String mSerialNumber, mType, mExpire, mPrice;
    protected boolean hasPurse, mIsPassengerTypeReduced;
    protected int mPurseValue, mPurseCoupons;
    private CardHeader header;

    public AccessCard(Context context, String headerText, String serialNumber) {
        this(context, R.layout.list_accesscard);
        this.mSerialNumber = serialNumber;
        header.setTitle(headerText);
    }

//    public AccessCard(Context context, String serialNumber, String type, String expire) {
//        this(context, R.layout.card_content);
//        this.mSerialNumber = serialNumber;
//        this.mType = type;
//        this.mExpire = expire;
//    }

    public AccessCard(Context context) {
        this(context, R.layout.list_accesscard);
    }

    public AccessCard(Context context, int innerLayout) {
        super(context, innerLayout);
        init();
    }

    /**
     * Init
     */
    private void init(){

        header = new CardHeader(getContext());
        addCardHeader(header);

        //Set a OnClickListener listener
        setOnClickListener(new OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                Toast.makeText(getContext(), "Click Listener card=", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        number = (TextView) parent.findViewById(R.id.card_number);
        type = (TextView) parent.findViewById(R.id.card_type);
        expire = (TextView) parent.findViewById(R.id.card_expire);
        price = (TextView) parent.findViewById(R.id.card_price);
        purse = (TextView) parent.findViewById(R.id.card_purse);
        passengerType = (TextView) parent.findViewById(R.id.card_passenger_type);

        Resources res = getContext().getResources();

        if (number != null)
            number.setText(mSerialNumber);

        if (passengerType != null && mIsPassengerTypeReduced){
            passengerType.setText(res.getString(R.string.reduced_price));
            passengerType.setVisibility(View.VISIBLE);
        }

        if (type != null)
            type.setText(mType);

        if (expire != null)
            expire.setText(mExpire);

        if (price != null)
            price.setText(mPrice);

        if (purse != null && hasPurse){
            purse.setVisibility(View.VISIBLE);
            purse.setText(String.format(res.getString(R.string.list_card_purse), mPurseValue, mPurseCoupons));
        }
    }

    public void setType(String type) {
        mType = type;
        if (this.type != null)
            this.type.setText(type);
    }

    public void setExpire(String expire) {
        mExpire = expire;
        if (this.expire != null)
            this.expire.setText(expire);
    }

    public void setPrice(String price) {
        mPrice = price;
        if (this.price != null)
            this.price.setText(price);
    }

    public boolean isHasPurse() {
        return hasPurse;
    }

    public void setHasPurse(boolean hasPurse) {
        this.hasPurse = hasPurse;
    }

    public void setPurseCoupons(int mPurseCoupons) {
        this.mPurseCoupons = mPurseCoupons;
    }

    public void setPurseValue(int mPurseValue) {
        this.mPurseValue = mPurseValue;
    }

    public void setIsPassengerTypeReduced(boolean isPassengerTypeReduced) {
        mIsPassengerTypeReduced = isPassengerTypeReduced;
    }

    @Override
    public String toString() {
        if (expire != null) {
            return "AccessCard{" +
                    "TVexpire=" + expire.getText().toString() +
                    ", TVtype=" + type.getText().toString() +
                    ", TVnumber=" + number.getText().toString() +
                    ", TVprice=" + price.getText().toString() +
                    '}';
        }else{
            return "AccessCard{" +
                    "expire=" + mExpire +
                    ", type=" + mType +
                    ", number=" + mSerialNumber +
                    ", price=" + mPrice +
                    '}';
        }
    }
}