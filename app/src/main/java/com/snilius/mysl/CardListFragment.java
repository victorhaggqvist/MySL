package com.snilius.mysl;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.snilius.mysl.util.Helper;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.TimeoutException;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CardListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CardListFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class CardListFragment extends Fragment {

//    private static final String ARG_USERNAME = "CardList.Username";
//    private static final String ARG_PASSWORD = "CardList.Password";
//    private static final String ARG_DOREFRESH = "CardList.doRefresh";

    public static final String EXTRA_CARD_SERIAL = "CardActivity.CardSerial";

    public static final String TAG = "CardListFragment";

    private OnFragmentInteractionListener mListener;

    private String mUsername, mPassword;
    private boolean mDoRefresh;

    private CardListView cardListView;
    private PullToRefreshLayout pullToRefreshLayout;
    private ArrayList<Card> cards;
    private CardArrayAdapter cardAdapter;
    private ArrayList<BareboneCard> bareboneCards;
    private Gson gson;
    private boolean authenticated = false;
    private Queue<GetTravelCardDetailsCallback> cardDetailsFetchQueue;
    private int cardsFetched = 0;
    private SLApiProvider sl;

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

        GlobalState gs = (GlobalState) getActivity().getApplication();
        mUsername = gs.getUsername();
        mPassword = gs.getPassword();
        mDoRefresh = gs.isRefresh();

        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        JsonArray travelCardsList;
        GlobalState app = ((GlobalState) getActivity().getApplication());
        if (app.getmShoppingCart() != null) {
            travelCardsList = app.getmShoppingCart().getAsJsonArray("TravelCards");
            bareboneCards = gson.fromJson(travelCardsList, new TypeToken<ArrayList<BareboneCard>>(){}.getType());
        }
        cards = new ArrayList<Card>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_card_list, container, false);
        pullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.ptr_cardlist);
        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .listener(new OnRefreshListener() {
                    @Override
                    public void onRefreshStarted(View view) {
                        Toast.makeText(getActivity(), "refreshed", Toast.LENGTH_SHORT).show();
                        pullToRefreshLayout.setRefreshComplete();
                    }
                }).setup(pullToRefreshLayout);
        cardListView = (CardListView) rootView.findViewById(R.id.cardlist);
        cardAdapter = new CardArrayAdapter(getActivity(), cards);

        cardListView.setAdapter(cardAdapter);
        return rootView;
    }

    /**
     * Initial fragment setup
     */
    private void setupCards() {
        if (mDoRefresh) {
            sl = new SLApiProvider(getActivity());
            sl.authenticate(getActivity(), new LoginCallback(), mUsername, mPassword);
        }

        cardDetailsFetchQueue = new ArrayDeque<GetTravelCardDetailsCallback>();
//        Collections.reverse(bareboneCards);
        if (null != bareboneCards){
            for (final BareboneCard bbc :bareboneCards){
                AccessCard card = new AccessCard(getActivity(),bbc.getName(), bbc.getSerial());
                if (bbc.getPassengerTypeStringId() != -1)
                    card.setPassengerType(getString(bbc.getPassengerTypeStringId()));

                if (bbc.isPurseEnabled() && bbc.getPurseValue()>0) {
                    card.setPurseEnabled(bbc.isPurseEnabled());
                    card.setPurseValue(Integer.toString(bbc.getPurseValue()));
//                    card.setPurseCoupons(bbc.getCouponCount());
                }

                card.setOnClickListener(new Card.OnCardClickListener() {
                    @Override
                    public void onClick(Card card, View view) {
                        if(((AccessCard) card).isCardEmpty()) {
                            Toast.makeText(getActivity(), getString(R.string.card_empty), Toast.LENGTH_LONG).show();
                            return;
                        }
                        Intent i = new Intent(getActivity(), CardActivity.class);
                        i.putExtra(EXTRA_CARD_SERIAL, bbc.getSerial());
                        startActivity(i);
                    }
                });
                cards.add(card);
                cardAdapter.notifyDataSetChanged();
                cardDetailsFetchQueue.add(new GetTravelCardDetailsCallback(card, bbc));
            }
        }

        if (!mDoRefresh)
            processCardQueue();
    }

    /**
     * Process the queue of details reqests
     * To be called when login is done
     */
    private void processCardQueue() {
        if (null == bareboneCards)
            return;
        while (cardsFetched < bareboneCards.size()){
            GetTravelCardDetailsCallback item = cardDetailsFetchQueue.poll();
            boolean dataFetched = false;
            if (Helper.isFileExsist(getActivity(), item.getFileName())){
                String file = "";
                try {
                    file = Helper.openFile(getActivity(), item.getFileName());
                    dataFetched = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (file.length()>0){
                    JsonObject json = new JsonParser().parse(file).getAsJsonObject();
                    applyCardDetail(item.getListCard(), json);
                }
            }else {
                if (null != getActivity())
                    sl.getTravelCardDetails(getActivity(), item, item.getCardId());
                dataFetched = true;
            }

            if (mDoRefresh && !dataFetched) {
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
        JsonObject travelCard = data.getAsJsonObject("travel_card");
        String purseValueExt = travelCard.getAsJsonObject("detail").get("purse_value_ext").getAsString();
        listCard.setPurseValue(purseValueExt);
        cardAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupCards();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
        mListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != sl)
            sl.killRequests();
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

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

        @Override
        public void onCompleted(Exception e, Response<JsonObject> result) {
            if (null == result) {
                if (e instanceof TimeoutException) {
                    Log.i(TAG, "Login, Connection Timeout");
//                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getActivity(), getString(R.string.error_connectivity), Toast.LENGTH_LONG).show();
                }else
                    Log.i(TAG, "Login Canceled");
            }else if (result.getHeaders().getResponseCode() == 200){
                Log.d(TAG, "Login successfull: " + result.getHeaders().getResponseCode() + result.getHeaders().getResponseMessage());
                authenticated = true;
                processCardQueue();
            }else {
//                System.out.println(result.getResult());
//                Toast.makeText(getActivity(), "failed to refresh", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Login failed: " + result.getHeaders().getResponseCode() + result.getHeaders().getResponseMessage());
            }
        }
    }

    private class GetTravelCardDetailsCallback implements FutureCallback<Response<JsonObject>> {

        private AccessCard mCard;
        private BareboneCard mBareboneCard;

        public GetTravelCardDetailsCallback(AccessCard card, BareboneCard bareboneCard) {
            mCard = card;
            mBareboneCard = bareboneCard;
        }

        public String getCardId(){
            return mBareboneCard.getId();
        }

        @Override
        public void onCompleted(Exception e, Response<JsonObject> result) {
            if (result.getHeaders().getResponseCode() == 200){
//                System.out.println(result.getResult());
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
            return mBareboneCard.getDetailStoreFileName();
        }

        public AccessCard getListCard() {
            return mCard;
        }
    }
}
