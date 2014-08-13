package com.snilius.mysl;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;
import com.snilius.mysl.model.CardBarebone;
import com.snilius.mysl.model.CardProduct;
import com.snilius.mysl.model.DetailCard;
import com.snilius.mysl.model.ProductCard;
import com.snilius.mysl.util.DetailCardHelper;
import com.snilius.mysl.util.Helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;


public class CardActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener{

    public static final String EXTRA_CARD_SERIAL = "CardActivity.CardSerial";

    // used only for alarm identification atm, check AlarmReceiver::registerNotification
    public static final String EXTRA_CARD_PRODUCTHASH = "CardActivity.CardProductHash";
    public static final String EXTRA_CARD_PRODUCTNAME = "CardActivity.CardProductName";

    public static final String TAG = "CardActivity";

    private SwipeRefreshLayout mSwipeRefreshLayout;
//    private TextView mNameHeader;
    private CardListView mCardList;
    private CardArrayAdapter mCardArrayAdapter;

    private String mSerial;
    private Gson gson;
    private CardBarebone card;
    private JsonObject cardDetail;
    private ArrayList<Card> mCards;
    private SLApiProvider sl;
    private ArrayList<String> mCardNames;
    private ArrayList<String> mCardEpireDate;
    private ArrayList<Integer> mCardIdHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_CARD_SERIAL))
            mSerial = intent.getStringExtra(EXTRA_CARD_SERIAL);
        else
            finish();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.card_detail_ptr);
        mSwipeRefreshLayout.setColorScheme(android.R.color.holo_blue_dark,
                android.R.color.holo_blue_light,
                android.R.color.holo_blue_dark,
                android.R.color.holo_blue_light);
        mSwipeRefreshLayout.setOnRefreshListener(this);

//        mNameHeader = (TextView) findViewById(R.id.card_detail_name);
        mCardList = (CardListView) findViewById(R.id.card_detail_list);
        mCardNames = new ArrayList<String>();
        mCardEpireDate = new ArrayList<String>();
        mCardIdHash = new ArrayList<Integer>();
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        setup();
        Tracker t = ((GlobalState) getApplication()).getTracker();
        t.setScreenName("CardView");
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    private void setup() {
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

        card = gson.fromJson(foundCard, new TypeToken<CardBarebone>(){}.getType());

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

        // if there is a purse active on card
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

        // loop through procucts if there is any
        if (DetailCardHelper.with(cardDetail).hasProcucts()){
            ArrayList<CardProduct> products = DetailCardHelper.with(cardDetail).getProducts();

            for (CardProduct p:products){
                ProductCard card;
                if (null != p.getEndDate()) {
                    mCardNames.add(p.getProductType());
                    mCardEpireDate.add(p.getEndDate().split("T")[0]);
                    mCardIdHash.add(p.getProductIdHash());

                    card = new ProductCard(this, p.getProductType(), p.getStartDateExt(),
                            p.getEndDateExt(), p.getProductPrice());
                }else {
                    card = new ProductCard(this, p.getProductType(), getString(R.string.inactive), null, p.getProductPrice());
                }
                mCards.add(card);
            }
        }
        Log.i(TAG, "UI cards laid out");
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        refreshData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.card, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_notification:
                addNotification();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Choose card to add notification on
     */
    private void addNotification() {
        if (mCardNames.size()>1) {
            String[] names = mCardNames.toArray(new String[mCardNames.size()]);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.select_notify_card))
                        .setItems(names, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                selectNotificationTime(which);
                            }
                        });
            builder.create().show();
        }else
            selectNotificationTime(0);
    }

    /**
     * Select when to notify
     * @param cardIndex
     */
    private void selectNotificationTime(final int cardIndex) {
        if (mCardNames.size()<1)
            return;
        Log.i(TAG,mCardNames.get(cardIndex)+" selected");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_numberpicker, null);
        final NumberPicker np = (NumberPicker) view.findViewById(R.id.dialog_np_picker);
        np.setMaxValue(30);
        np.setMinValue(1);
        builder.setView(view);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int daysBefore = np.getValue();
                String resp = AlarmReceiver.registerNotification(getApplicationContext(),
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()),
                        mCardNames.get(cardIndex), mSerial, mCardEpireDate.get(cardIndex), mCardIdHash.get(cardIndex), daysBefore);
                Toast.makeText(getApplicationContext(), resp, Toast.LENGTH_LONG).show();
            }
        });
        builder.create().show();
    }

    private class GetTravelCardDetailsCallback implements FutureCallback<Response<JsonObject>> {

        @Override
        public void onCompleted(Exception e, Response<JsonObject> result) {
            if (null == result) {
                if (e instanceof TimeoutException) {
                    Log.w(TAG, "GetTravelCardDetails, Connection Timeout");
                }else
                    Log.i(TAG, "GetTravelCardDetails Canceled");
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getApplicationContext(), getString(R.string.error_connectivity), Toast.LENGTH_LONG).show();
            }else if (result.getHeaders().getResponseCode() == 200){
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
