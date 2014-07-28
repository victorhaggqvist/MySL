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

    protected TextView number, type, expire, price, purse, passengerType, cardEmptyMsg;
    protected String mSerialNumber, mType, mExpire, mPrice, mPurseValue, mPassengerType;
    protected boolean mPurseEnabled, mCardEmpty;
//    protected int mPurseCoupons;
    private CardHeader header;
    private Resources res;

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
        res = getContext().getResources();
        header = new CardHeader(getContext());
        addCardHeader(header);

        // set defaults
        mCardEmpty = true;
        mPurseEnabled = false;

        //Set a OnClickListener listener
//        setOnClickListener(new OnCardClickListener() {
//            @Override
//            public void onClick(Card card, View view) {
//                Toast.makeText(getContext(), "Click Listener card=", Toast.LENGTH_LONG).show();
//            }
//        });
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        number = (TextView) parent.findViewById(R.id.card_number);
        type = (TextView) parent.findViewById(R.id.card_period_type);
        expire = (TextView) parent.findViewById(R.id.card_period_expire);
        price = (TextView) parent.findViewById(R.id.card_period_price);
        purse = (TextView) parent.findViewById(R.id.card_purse);
        passengerType = (TextView) parent.findViewById(R.id.card_passenger_type);
        cardEmptyMsg = (TextView) parent.findViewById(R.id.card_empty);

        if (number != null) {
            number.setText(mSerialNumber);
        }

        setTVtext(passengerType, mPassengerType);
        setTVtext(type, mType);
        setTVtext(expire, mExpire);
        setTVtext(price, mPrice);

        if (mPurseEnabled)
            setTVtext(purse, String.format(res.getString(R.string.list_card_purse), mPurseValue));
        else
            setTVtext(purse, null); // something in view recyceling is not right so need to make sure this is really GONE

        if (!mCardEmpty && cardEmptyMsg != null){
            cardEmptyMsg.setVisibility(View.GONE);
        }else if (cardEmptyMsg != null){
            cardEmptyMsg.setVisibility(View.VISIBLE);
        }
    }

    private void setTVtext(TextView textView, String text){
        if (null != textView && textView instanceof TextView){
            if (null == text){
                textView.setText(null);
                textView.setVisibility(View.GONE);
            }else {
                textView.setVisibility(View.VISIBLE);
                textView.setText(text);
                cardNotEmpty();
            }
        }
    }

    private void cardNotEmpty(){
        mCardEmpty = false;
        if (cardEmptyMsg != null && cardEmptyMsg.getVisibility() != View.GONE)
            cardEmptyMsg.setVisibility(View.GONE);
    }

    public void setType(String type) {
        mType = type;
        setTVtext(this.type, mType);
    }

    public void setExpire(String expire) {
        mExpire = expire;
        setTVtext(this.expire, mExpire);
    }

    public void setPrice(String price) {
        mPrice = price;
        setTVtext(this.price, mPrice);
    }

    public void setPurseEnabled(boolean purseEnabled) {
        this.mPurseEnabled = purseEnabled;
    }

//    public void setPurseCoupons(int purseCoupons) {
//        mPurseCoupons = purseCoupons;
////        setTVtext(purse,);
////        if (purse != null){
////            purse.setVisibility(View.VISIBLE);
////            purse.setText(String.format(res.getString(R.string.list_card_purse), mPurseValue));
////        }
//    }

    public void setPurseValue(String purseValue) {
        mPurseValue = purseValue;
        if (mPurseEnabled)
            setTVtext(purse, String.format(res.getString(R.string.list_card_purse), mPurseValue));
    }

    public void setPassengerType(String type) {
        mPassengerType = type;
        setTVtext(passengerType, mPassengerType);
    }

    @Override
    public String toString() {
        return "AccessCard{" +
                "name='" + header.getTitle()+'\''+
                ", mSerialNumber='" + mSerialNumber + '\'' +
                ", mType='" + mType + '\'' +
                ", mExpire='" + mExpire + '\'' +
                ", mPrice='" + mPrice + '\'' +
                ", mPurseValue='" + mPurseValue + '\'' +
                ", mPassengerType='" + mPassengerType + '\'' +
                ", mPurseEnabled=" + mPurseEnabled +
//                ", mPurseCoupons=" + mPurseCoupons +
                '}';
    }

    //    @Override
//    public String toString() {
//        if (expire != null) {
//            return "AccessCard{" +
//                    "TVexpire=" + expire.getText().toString() +
//                    ", TVtype=" + type.getText().toString() +
//                    ", TVnumber=" + number.getText().toString() +
//                    ", TVprice=" + price.getText().toString() +
//                    '}';
//        }else{
//            return "AccessCard{" +
//                    "expire=" + mExpire +
//                    ", type=" + mType +
//                    ", number=" + mSerialNumber +
//                    ", price=" + mPrice +
//                    '}';
//        }
//    }
}