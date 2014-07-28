package com.snilius.mysl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        Intent intent = getIntent();
        mSerial = intent.getStringExtra(CardListFragment.EXTRA_CARD_SERIAL);
        System.out.println("serial "+mSerial);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.card_detail_ptr);
        mSwipeRefreshLayout.setColorScheme(android.R.color.holo_blue_dark, android.R.color.holo_blue_light, android.R.color.holo_blue_dark, android.R.color.holo_blue_light);
        mSwipeRefreshLayout.setOnRefreshListener(this);

//        mNameHeader = (TextView) findViewById(R.id.card_detail_name);
        mCardList = (CardListView) findViewById(R.id.card_detail_list);

        setup();
        setupCards();
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

        String detailFile = null;
        try {
            detailFile = Helper.openFile(this, card.getDetailStoreFileName());
        } catch (IOException e) {
            Log.i(TAG, "Detail file "+ card.getDetailStoreFileName()+" not found");
        }

        if (null != detailFile) {
            cardDetail = new JsonParser().parse(detailFile).getAsJsonObject();
        }
        mCards = new ArrayList<Card>();
        mCardArrayAdapter = new CardArrayAdapter(this, mCards);
        mCardList.setAdapter(mCardArrayAdapter);
    }

    private void setupCards(){
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.card, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        System.out.println("refresh");
//        Toast.makeText(this, "foundCard", Toast.LENGTH_SHORT).show();
        mSwipeRefreshLayout.setRefreshing(true);

        ( new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 3000);
    }
}
