package com.snilius.mysl.model;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.snilius.mysl.R;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Created by victor on 7/21/14.
 */
public class AccessCard extends Card {

    protected TextView number;
    protected TextView purse;
    protected TextView cardEmptyMsg;
    protected TextView cardProducts;
    protected TextView purseHeader;
    protected TextView purseSubtitle;
    protected TextView purseValue;
    protected String mSerialNumber;
    protected String mType;
    protected String mExpire;
    protected String mPrice;
    protected String mPurseValue;
    protected String mPurseType;
    protected String mCardProducts;
    protected int mPurseCoupons;
    protected boolean mPurseEnabled, mCardEmpty;

    private CardHeader header;
    private Resources res;

    public AccessCard(Context context, String headerText, String serialNumber) {
        this(context, R.layout.list_accesscard);
        this.mSerialNumber = serialNumber;
        header.setTitle(headerText);
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
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        number = (TextView) parent.findViewById(R.id.card_number);
        cardProducts = (TextView) parent.findViewById(R.id.card_period_products);

        purseHeader = (TextView) view.findViewById(R.id.card_purse_header);
        purseSubtitle = (TextView) view.findViewById(R.id.card_purse_subtitle);

        cardEmptyMsg = (TextView) parent.findViewById(R.id.card_empty);

        if (number != null)
            number.setText(mSerialNumber);

        setTVtext(cardProducts, mCardProducts);

        if (mPurseEnabled) {
            setTVtext(purseHeader, String.format(res.getString(R.string.list_card_purse_title), mPurseValue));
            setTVtext(purseSubtitle, String.format(res.getString(R.string.list_card_purse_subtitle), mPurseType, mPurseCoupons));
        }else {
            purseHeader.setVisibility(View.GONE);
            setTVtext(purseSubtitle, null); // something in view recyceling is not right so need to make sure this is really GONE
            setTVtext(purseHeader, null);
        }

        if (!mCardEmpty && cardEmptyMsg != null){
            cardEmptyMsg.setVisibility(View.GONE);
        }else if (cardEmptyMsg != null){
            cardEmptyMsg.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Assingn string to TextView and tell that card is NOT empty
     * Or IS empty if text is null
     * @param textView
     * @param text
     */
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

    public void setCardProducts(String cardProducts) {
        mCardProducts = cardProducts;
        setTVtext(this.cardProducts, mCardProducts);
    }

    public void setPurseEnabled(boolean purseEnabled) {
        this.mPurseEnabled = purseEnabled;
    }

    public void setPurseValue(String purseValue) {
        mPurseValue = purseValue;
        setTVtext(purseHeader, String.format(res.getString(R.string.list_card_purse_title), mPurseValue));
    }

    public void setPurseSubtitle(String purseType, int purseCoupons) {
        mPurseType = purseType;
        mPurseCoupons = purseCoupons;
        if (mPurseEnabled)
            setTVtext(purseSubtitle, String.format(res.getString(R.string.list_card_purse_subtitle), mPurseType, mPurseCoupons));
    }

    public boolean isCardEmpty(){
        return cardEmptyMsg.getVisibility()==View.VISIBLE;
    }

    @Override
    public String toString() {
        return "AccessCard{" +
                "mSerialNumber='" + mSerialNumber + '\'' +
                ", mType='" + mType + '\'' +
                ", mExpire='" + mExpire + '\'' +
                ", mPrice='" + mPrice + '\'' +
                ", mPurseValue='" + mPurseValue + '\'' +
                ", mPurseType='" + mPurseType + '\'' +
                ", mCardProducts='" + mCardProducts + '\'' +
                ", mPurseCoupons=" + mPurseCoupons +
                ", mPurseEnabled=" + mPurseEnabled +
                ", mCardEmpty=" + mCardEmpty +
                ", header=" + header +
                '}';
    }
}