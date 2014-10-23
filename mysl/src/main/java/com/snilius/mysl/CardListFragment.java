package com.snilius.mysl;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.snilius.mysl.model.AccessCard;
import com.snilius.mysl.model.CardBarebone;
import com.snilius.mysl.model.CardProduct;
import com.snilius.mysl.util.DetailCardHelper;
import com.snilius.mysl.util.Helper;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CardListFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class CardListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final String TAG = "CardListFragment";

//    private OnFragmentInteractionListener mListener;

    private String mUsername, mPassword;
    private boolean mDoRefresh;

    private CardListView cardListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArrayList<Card> cards;
    private CardArrayAdapter cardAdapter;
    private ArrayList<CardBarebone> cardBarebones;
    private Gson gson;
    private Queue<GetTravelCardDetailsCallback> cardDetailsFetchQueue;
    private int cardsFetched, detailsApplyed;
    private SLApiProvider sl;
    private GlobalState gs;
    private boolean initWasRefresh;
    private SharedPreferences pref;

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment CardListFragment.
     */
    public static CardListFragment newInstance() {
        CardListFragment fragment = new CardListFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    public CardListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        gs = (GlobalState) getActivity().getApplication();
        mUsername = gs.getUsername();
        mPassword = gs.getPassword();
        mDoRefresh = gs.isRefresh();

        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        Tracker t = ((GlobalState) getActivity().getApplication()).getTracker();
        t.setScreenName("CardListView");
        t.send(new HitBuilders.AppViewBuilder().build());
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_card_list, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.ptr_cardlist);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorScheme(android.R.color.holo_blue_dark,
                android.R.color.holo_blue_light,
                android.R.color.holo_blue_dark,
                android.R.color.holo_blue_light);
        cardListView = (CardListView) rootView.findViewById(R.id.cardlist);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setup();
    }

    /**
     * Initial fragment setup
     */
    private void setup() {
        JsonArray travelCardsList;

        initWasRefresh = false;
        if (gs.getmShoppingCart() != null) {
            travelCardsList = gs.getmShoppingCart().getAsJsonArray("UserTravelCards");
            cardBarebones = gson.fromJson(travelCardsList, new TypeToken<ArrayList<CardBarebone>>(){}.getType());
            populateList();
        }else {
            refreshData();
            initWasRefresh = true;
        }
    }

    /**
     * Get new data from sl
     */
    private void refreshData() {
        mSwipeRefreshLayout.setRefreshing(true);
        if (null == sl)
            sl = new SLApiProvider(getActivity());
        sl.authenticate(getActivity(), new LoginCallback(), mUsername, mPassword);
    }

    /**
     * Populate list with available data
     */
    private void populateList(){
        cards = new ArrayList<Card>();
        cardDetailsFetchQueue = new ArrayDeque<GetTravelCardDetailsCallback>();
        cardAdapter = new CardArrayAdapter(getActivity(), cards);
        cardListView.setAdapter(cardAdapter);

        if (null != cardBarebones){
            for (final CardBarebone bbc : cardBarebones){
                AccessCard card = new AccessCard(getActivity(),bbc.getName(), bbc.getSerial());
//                if (bbc.getPassengerTypeStringId() != -1)
//                    card.setPassengerType(getString(bbc.getPassengerTypeStringId()));

                if (bbc.isPurseEnabled() && bbc.getPurseValue()>0) {
                    card.setPurseEnabled(bbc.isPurseEnabled());
                    card.setPurseSubtitle(getString(bbc.getPassengerTypeStringId()), bbc.getCouponCount());
                    card.setPurseValue(Integer.toString(bbc.getPurseValue()));
                    card.setInfoLoaded(true);
                }

                card.setOnClickListener(new Card.OnCardClickListener() {
                    @Override
                    public void onClick(Card card, View view) {
                        if(((AccessCard) card).isCardEmpty()) {
                            Toast.makeText(getActivity(), getString(R.string.card_empty), Toast.LENGTH_LONG).show();
                            return;
                        }
                        Intent i = new Intent(getActivity(), CardActivity.class);
                        i.putExtra(CardActivity.EXTRA_CARD_SERIAL, bbc.getSerial());
                        startActivity(i);
                    }
                });
                cards.add(card);
                cardAdapter.notifyDataSetChanged();
                cardDetailsFetchQueue.add(new GetTravelCardDetailsCallback(card, bbc));
            }
        }

        if (null == sl) { // if first call, this says refrash on start is NOT on
            sl = new SLApiProvider(getActivity());
            sl.authenticate(getActivity(), new LoginCallback(true), mUsername, mPassword);
        }else
            processCardQueue();
    }

    /**
     * Process the queue of details reqests
     */
    private void processCardQueue() {
        if (null == cardBarebones)
            return;
        cardsFetched = 0;
        detailsApplyed = 0;
        while (cardsFetched < cardBarebones.size()){
            GetTravelCardDetailsCallback item = cardDetailsFetchQueue.poll();
            boolean initWasRefresh = false;
            if (Helper.isFileExsist(getActivity(), item.getFileName())){
                try {
                    String file = Helper.openFile(getActivity(), item.getFileName());
                    JsonObject json = new JsonParser().parse(file).getAsJsonObject();
                    applyCardDetail(item.getListCard(), json);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                if (null != getActivity())
                    sl.getTravelCardDetails(getActivity(), item, item.getCardId());
                initWasRefresh = true;
            }

            if (mDoRefresh && !initWasRefresh) {
                if (null != getActivity())
                    sl.getTravelCardDetails(getActivity(), item, item.getCardId());
            }
            cardsFetched++;
        }
        Log.i(TAG, "Cards loaded");
    }

    /**
     * Apply detailed info from data on listCard
     * TODO to be extended for period tickets
     * @param listCard
     * @param data
     */
    private void applyCardDetail(AccessCard listCard, JsonObject data) {
        listCard.setInfoLoaded(true);
        if (DetailCardHelper.with(data).hasPurse())
            listCard.setPurseValue(DetailCardHelper.with(data).getPurseValueExt());

//        JsonObject travelCard = data.getAsJsonObject("travel_card");
//        String purseValueExt = travelCard.getAsJsonObject("detail").get("purse_value_ext").getAsString();
//        listCard.setPurseValue(purseValueExt);

        // set period cards
        if (DetailCardHelper.with(data).hasProcucts()) {
            ArrayList<CardProduct> products = DetailCardHelper.with(data).getProducts();

            // maby make this a list with every product a list item...
            // but will mostly be only one item anyway...
            String productString = "";
            for (CardProduct cp : products) {
                productString += cp.getProductType() + "\n";
                if (null == cp.getEndDate())
                    productString += gs.getString(R.string.inactive) + "\n";
                else {
                    String lang = pref.getString(getString(R.string.pref_lang),"en");
                    String date = cp.getEndDate();

                    String formatted = Helper.localizeAndFormatDate(date, lang);

                    productString += formatted + "\n";
                }
            }
            productString = productString.substring(0, productString.length() - 1);
            listCard.setCardProducts(productString);
        }

        cardAdapter.notifyDataSetChanged();


        if (++detailsApplyed == cardBarebones.size()) {
            mSwipeRefreshLayout.setRefreshing(false);
            Log.i(TAG, "All details applyed");
            if (mDoRefresh && !initWasRefresh) {
                mDoRefresh = false;
                refreshData();
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(1);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != sl)
            sl.killRequests();
    }

    @Override
    public void onRefresh() {
        mDoRefresh = true;
        refreshData();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        public void onFragmentInteraction(Uri uri);
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.card_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_show_add_card:
                startActivity(new Intent(getActivity(), AddCardActivity.class));
                break;
        }
        return true;
//        return super.onOptionsItemSelected(item);
    }

    private class LoginCallback implements FutureCallback<Response<JsonObject>> {

        private boolean justLogin;

        public LoginCallback(){
            justLogin = false;
        }

        public LoginCallback(boolean justLogin){

            this.justLogin = justLogin;
        }

        @Override
        public void onCompleted(Exception e, Response<JsonObject> result) {
            if (null == result) {
                if (e instanceof TimeoutException) {
                    Log.w(TAG, "Login, Connection Timeout");
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getActivity(), getString(R.string.error_connectivity), Toast.LENGTH_LONG).show();
                }else
                    Log.i(TAG, "Login Canceled");
            }else if (result.getHeaders().getResponseCode() == 200){
                Log.i(TAG, "Login successfull: " + result.getHeaders().getResponseCode() + result.getHeaders().getResponseMessage());

                if (justLogin)
                    processCardQueue();
                else
                    populateList();
            }else {
                Log.w(TAG, "Login failed: " + result.getHeaders().getResponseCode() + result.getHeaders().getResponseMessage());
            }
        }
    }

    private class GetTravelCardDetailsCallback implements FutureCallback<Response<JsonObject>> {

        private AccessCard mCard;
        private CardBarebone mCardBarebone;

        public GetTravelCardDetailsCallback(AccessCard card, CardBarebone cardBarebone) {
            mCard = card;
            mCardBarebone = cardBarebone;
        }

        public String getCardId(){
            return mCardBarebone.getId();
        }

        @Override
        public void onCompleted(Exception e, Response<JsonObject> result) {
            if (null == result) {
                if (e instanceof TimeoutException) {
                    Log.i(TAG, "GetTravelCardDetails, Connection Timeout");
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getActivity(), getString(R.string.error_connectivity), Toast.LENGTH_LONG).show();
                }else
                    Log.i(TAG, "GetTravelCardDetails Canceled");
                mSwipeRefreshLayout.setRefreshing(false);
            } else if (result.getHeaders().getResponseCode() == 200){
                Log.d(TAG, "GetTravelCardDetails successfull: " + result.getHeaders().getResponseCode() + result.getHeaders().getResponseMessage());

                if (null == result.getResult()) // if request was likly killed
                    return;

                JsonObject data = result.getResult().getAsJsonObject("data");
                try {
                    Helper.saveToFile(getActivity(), getFileName(), data.toString());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                applyCardDetail(mCard, data);
            }else{
                Log.e(TAG, "Failed to get details for card: " + getCardId());
            }

        }

        public String getFileName() {
            return mCardBarebone.getDetailStoreFileName();
        }

        public AccessCard getListCard() {
            return mCard;
        }
    }
}
