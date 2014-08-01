package com.snilius.mysl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;
import com.snilius.mysl.model.AccessCard;
import com.snilius.mysl.model.BareboneCard;
import com.snilius.mysl.model.DetailCard;
import com.snilius.mysl.util.DetailCardHelper;
import com.snilius.mysl.util.Helper;

import java.io.IOException;
import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;


public class CardActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener{

    public static final String TAG = "CardActivity";

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mNameHeader;
    private CardListView mCardList;
    private CardArrayAdapter mCardArrayAdapter;

    private String mSerial;
    private Gson gson;
    private BareboneCard card;
    private JsonObject cardDetail;
    private ArrayList<Card> mCards;
    private SLApiProvider sl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        Intent intent = getIntent();
        mSerial = intent.getStringExtra(CardListFragment.EXTRA_CARD_SERIAL);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.card_detail_ptr);
        mSwipeRefreshLayout.setColorScheme(android.R.color.holo_blue_dark,
                android.R.color.holo_blue_light,
                android.R.color.holo_blue_dark,
                android.R.color.holo_blue_light);
        mSwipeRefreshLayout.setOnRefreshListener(this);

//        mNameHeader = (TextView) findViewById(R.id.card_detail_name);
        mCardList = (CardListView) findViewById(R.id.card_detail_list);

        setup();
    }

    private void setup() {
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        GlobalState gs = (GlobalState) getApplication();

        if (null == gs.getmShoppingCart())
            finish();

        JsonObject userInfo = gs.getmShoppingCart();
        JsonArray travelCards = userInfo.getAsJsonArray("TravelCards");

        JsonObject foundCard = null;
        for (int i = 0; i < travelCards.size(); i++)
            if (travelCards.get(i).getAsJsonObject().get("Serial").getAsString().equals(mSerial))
                foundCard = travelCards.get(i).getAsJsonObject();

        if (null == foundCard)
            finish();

        card = gson.fromJson(foundCard, new TypeToken<BareboneCard>(){}.getType());

        setTitle(card.getName());

        if (Helper.isFileExsist(this, card.getDetailStoreFileName())){
            String detailFile = null;
            try {
                detailFile = Helper.openFile(this, card.getDetailStoreFileName());
            } catch (IOException e) {
                Log.i(TAG, "Detail file "+ card.getDetailStoreFileName()+" not found");
                refreshData();
            }

            if (null != detailFile) {
                cardDetail = new JsonParser().parse(detailFile).getAsJsonObject();
            }
            setupUICards();
        }else {
            refreshData();
        }
    }

    private void refreshData() {
        if (null == sl)
            sl = new SLApiProvider(this);
        sl.getTravelCardDetails(this, new GetTravelCardDetailsCallback(), card.getId());
    }

    private void setupUICards(){
        mCards = new ArrayList<Card>();
        mCardArrayAdapter = new CardArrayAdapter(this, mCards);
        mCardList.setAdapter(mCardArrayAdapter);
        if (card.isPurseEnabled()){
            String passengerType = card.getPassengerTypeStringId() != -1?getString(card.getPassengerTypeStringId())+"\n":"";
            String purseValue = String.format(
                    getString(R.string.card_detail_purse_value),
                    DetailCardHelper.with(cardDetail).getPurseValueExt()
            )+"\n";
            String defaultRide = String.format(getString(R.string.card_detail_purse_default_ride), card.getCouponCount());

            String purseBody = passengerType+ purseValue + defaultRide;
            DetailCard purseCard = new DetailCard(this, getString(R.string.sl_access_credit), purseBody);

            mCards.add(purseCard);
        }
        Log.i(TAG, "UI cards laid out");
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        refreshData();
    }

    private class GetTravelCardDetailsCallback implements FutureCallback<Response<JsonObject>> {

        @Override
        public void onCompleted(Exception e, Response<JsonObject> result) {
            if (result.getHeaders().getResponseCode() == 200){
                Log.d(TAG, "GetTravelCardDetails successfull: " + result.getHeaders().getResponseCode() + result.getHeaders().getResponseMessage());
                JsonObject data = result.getResult().getAsJsonObject("data");
                try {
                    Helper.saveToFile(getApplicationContext(), card.getDetailStoreFileName(), data.toString());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                cardDetail = data;
                setupUICards();
                Log.i(TAG, "Card details refreshed for card: " + card.getId());
                mSwipeRefreshLayout.setRefreshing(false);
            }else{
                Log.w(TAG, "Failed to get details for card: " + card.getId());
            }

        }
    }
}
