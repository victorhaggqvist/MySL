package com.snilius.mysl.model;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.snilius.mysl.R;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;

/**
 * Created by victor on 7/27/14.
 */
public class DetailCard extends Card {
    private String mBody;
    private TextView body;
    private CardHeader cardHeader;

    public DetailCard(Context context, String header, String body) {
        this(context, R.layout.list_detailcard);
        cardHeader.setTitle(header);
        mBody = body;
    }

    public DetailCard(Context context, int innerLayout) {
        super(context, innerLayout);
        cardHeader = new CardHeader(context);
        addCardHeader(cardHeader);
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {
        body = (TextView) parent.findViewById(R.id.card_detail_text);

        if (body != null)
            body.setText(mBody);
    }
}
