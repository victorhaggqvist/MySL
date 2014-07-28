package com.snilius.mysl;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.snilius.mysl.model.OrderItemChild;
import com.snilius.mysl.model.OrderItemHeader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnlineOrdersFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OnlineOrdersFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class OnlineOrdersFragment extends Fragment {

//    private static final String ARG_USERNAME = "CardList.Username";
//    private static final String ARG_PASSWORD = "CardList.Password";

    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private String mUsername, mPassword;
    private ExpandableListView listView;
    private OrderListAdapter listAdapter;

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_online_orders, container, false);
        listView = (ExpandableListView) rootView.findViewById(R.id.order_list);
        setup();
        return rootView;
    }

    private void setup() {
        ArrayList<OrderItemHeader> headers = new ArrayList<OrderItemHeader>();
        HashMap<OrderItemHeader, List<OrderItemChild>> children = new HashMap<OrderItemHeader, List<OrderItemChild>>();

        headers.add(new OrderItemHeader(45, "23", "23"));
        headers.add(new OrderItemHeader(45, "23", "23"));
        headers.add(new OrderItemHeader(45, "23", "23"));
        headers.add(new OrderItemHeader(45, "23", "23"));
        
        ArrayList<OrderItemChild> children1 = new ArrayList<OrderItemChild>();
        children1.add(new OrderItemChild(1, "222", "56"));
        children1.add(new OrderItemChild(1, "222", "56"));
        children1.add(new OrderItemChild(1, "222", "56"));

        ArrayList<OrderItemChild> children2 = new ArrayList<OrderItemChild>();
        children2.add(new OrderItemChild(1, "222", "56"));
        children2.add(new OrderItemChild(1, "222", "56"));
        children2.add(new OrderItemChild(1, "222", "56"));

        ArrayList<OrderItemChild> children3 = new ArrayList<OrderItemChild>();
        children3.add(new OrderItemChild(1, "222", "56"));
        children3.add(new OrderItemChild(1, "222", "56"));
        children3.add(new OrderItemChild(1, "222", "56"));

        ArrayList<OrderItemChild> children4 = new ArrayList<OrderItemChild>();
        children4.add(new OrderItemChild(1, "222", "56"));
        children4.add(new OrderItemChild(1, "222", "56"));
        children4.add(new OrderItemChild(1, "222", "56"));

        children.put(headers.get(0), children1);
        children.put(headers.get(1), children2);
        children.put(headers.get(2), children3);
        children.put(headers.get(3), children4);

        listAdapter = new OrderListAdapter(getActivity(), headers, children);
        listView.setAdapter(listAdapter);
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

}
