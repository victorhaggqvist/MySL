package com.snilius.mysl;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;
import com.snilius.mysl.model.OrderItemChild;
import com.snilius.mysl.model.OrderItemHeader;
import com.snilius.mysl.util.Helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

import timber.log.Timber;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnlineOrdersFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OnlineOrdersFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class OnlineOrdersFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private OnFragmentInteractionListener mListener;
    private String mUsername, mPassword;
    private ExpandableListView listView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private OrderListAdapter listAdapter;

    private boolean mRefresh;
    private boolean mAuthenticated;
    private SLApiProvider sl;
    private JsonObject orderData;
    private ArrayList<OrderItemHeader> headers;
    private HashMap<OrderItemHeader, List<OrderItemChild>> children;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OnlineOrdersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OnlineOrdersFragment newInstance() {
        OnlineOrdersFragment fragment = new OnlineOrdersFragment();
        return fragment;
    }

    public OnlineOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mUsername = getArguments().getString(ARG_USERNAME);
//            mPassword = getArguments().getString(ARG_PASSWORD);
//        }

        GlobalState gs = (GlobalState) getActivity().getApplication();
        mUsername = gs.getUsername();
        mPassword = gs.getPassword();
        mRefresh = gs.isRefresh();
        mAuthenticated = false;
        Tracker t = ((GlobalState) getActivity().getApplication()).getTracker();
        t.setScreenName("OrderListView");
        t.send(new HitBuilders.AppViewBuilder().build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_online_orders, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.order_list_ptr);
        mSwipeRefreshLayout.setColorScheme(android.R.color.holo_blue_dark,
                android.R.color.holo_blue_light,
                android.R.color.holo_blue_dark,
                android.R.color.holo_blue_light);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        listView = (ExpandableListView) rootView.findViewById(R.id.order_list);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setup();
    }

    /**
     * Initial fragment setup
     * Fetch order list from either disk or from SL
     */
    private void setup() {
        sl = new SLApiProvider(getActivity());
        headers = new ArrayList<OrderItemHeader>();
        children = new HashMap<OrderItemHeader, List<OrderItemChild>>();

        boolean initWasRefresh = false;
        if (Helper.isFileExsist(getActivity(), getString(R.string.file_orders))) {
            try {
                String ordersFile = Helper.openFile(getActivity(), getString(R.string.file_orders));
                orderData = new JsonParser().parse(ordersFile).getAsJsonObject();
                completeSetup();
            } catch (IOException e) {
                Timber.i("No orders file present");
                doRefresh();
            }
        }else {
            doRefresh();
            initWasRefresh = true;
        }

        if (mRefresh && !initWasRefresh)
            doRefresh();
    }

    /**
     * Initiate a data refresh, or initial load for that matter
     */
    private void doRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        sl.authenticate(getActivity(), new LoginCallback(), mUsername, mPassword);
    }

    /**
     * Complete the setup, when data has come from wharever place
     */
    private void completeSetup() {
        headers.clear();
        children.clear();

        final JsonArray orderList = orderData.getAsJsonArray("sales_order_list");
        for (int i = 0; i < orderList.size(); i++) {
            JsonObject order = orderList.get(i).getAsJsonObject().getAsJsonObject("sales_order");

            OrderItemHeader orderItemHeader = new OrderItemHeader(order.get("rdesc").getAsInt(),
                    order.get("order_date_ext").getAsString(),
                    order.get("amount_incl_vat").getAsString());
            headers.add(orderItemHeader);

            JsonArray orderLines = order.get("order_lines_ext").getAsJsonArray();
            ArrayList<OrderItemChild> orderItemChildren = new ArrayList<OrderItemChild>();
            for (int j = 0; j < orderLines.size(); j++) {
                JsonObject orderLine = orderLines.get(j).getAsJsonObject();
                orderItemChildren.add(new OrderItemChild(
                        orderLine.get("quantity").getAsInt(),
                        orderLine.getAsJsonObject("product").get("description").getAsString(),
                        orderLine.getAsJsonObject("total_price").get("amount_incl_vat").getAsString()));
            }
            children.put(orderItemHeader, orderItemChildren);
        }

        listAdapter = new OrderListAdapter(getActivity(), headers, children);
        listView.setAdapter(listAdapter);

        Timber.i("List loaded");
        mSwipeRefreshLayout.setRefreshing(false);
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
        ((MainActivity) activity).onSectionAttached(2);
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
    public void onRefresh() {
        doRefresh();
//        mSwipeRefreshLayout.setRefreshing(false);
//        System.out.println("refreshed");
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
    public void onPause() {
        super.onPause();
        if (null != sl)
            sl.killRequests();
    }

    private class LoginCallback implements FutureCallback<Response<JsonObject>> {

        @Override
        public void onCompleted(Exception e, Response<JsonObject> result) {
            if (null == result) {
                if (e instanceof TimeoutException) {
                    Timber.i("Login, Connection Timeout");
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getActivity(), getString(R.string.error_connectivity), Toast.LENGTH_LONG).show();
                }else
                    Timber.i("Login Canceled");
            }else if (result.getHeaders().getResponseCode() == 200){
                Timber.d("Login successfull: " + result.getHeaders().getResponseCode() + result.getHeaders().getResponseMessage());
                mAuthenticated = true;
                if (null != getActivity())
                    sl.getSalesOrders(getActivity(), new SalesOrdersCallback());
            }else {
//                System.out.println(result.getResult());
//                Toast.makeText(getActivity(), "failed to refresh", Toast.LENGTH_LONG).show();
                Timber.d("Login failed: " + result.getHeaders().getResponseCode() + result.getHeaders().getResponseMessage());
            }
        }
    }

    private class SalesOrdersCallback implements FutureCallback<Response<JsonObject>> {
        @Override
        public void onCompleted(Exception e, Response<JsonObject> result) {
            if (null == result)
                Timber.i("SalesOrders Canceled");
            else if (result.getHeaders().getResponseCode() == 200){
                Timber.d("Sales load successfull: " + result.getHeaders().getResponseCode() + result.getHeaders().getResponseMessage());
                orderData = result.getResult().getAsJsonObject("data");
                try {
                    Helper.saveToFile(getActivity(), getString(R.string.file_orders), orderData.toString());
                } catch (IOException e1) {
                    Timber.e("Failed to store orders");
                    e1.printStackTrace();
                }
                completeSetup();
            }else{
                Timber.d("Sales load failed: " + result.getHeaders().getResponseCode() + result.getHeaders().getResponseMessage());
            }
        }
    }
}
