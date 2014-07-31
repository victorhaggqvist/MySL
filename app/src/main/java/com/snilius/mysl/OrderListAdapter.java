package com.snilius.mysl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.snilius.mysl.model.OrderItemChild;
import com.snilius.mysl.model.OrderItemHeader;

import java.util.HashMap;
import java.util.List;

/**
 * Created by victor on 7/28/14.
 */
public class OrderListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<OrderItemHeader> mListDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<OrderItemHeader, List<OrderItemChild>> mListDataChild;

    public OrderListAdapter(Context context,
                            List<OrderItemHeader> listDataHeader,
                            HashMap<OrderItemHeader,
                                    List<OrderItemChild>> listChildData) {
        mContext = context;
        mListDataHeader = listDataHeader;
        mListDataChild = listChildData;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return mListDataChild.get(mListDataHeader.get(groupPosition)).get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final OrderItemChild child = (OrderItemChild) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_orderitem_child, null);
        }

        TextView desctiption = (TextView) convertView.findViewById(R.id.order_list_child_desctiption);
        TextView price = (TextView) convertView.findViewById(R.id.order_list_child_price);

        desctiption.setText(child.getQuantity() +" x "+ child.getDescription());
//        desctiption.setText(child.getDescription());
        price.setText(child.getTotalPriceIncVat()+"kr");
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.mListDataChild.get(this.mListDataHeader.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.mListDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.mListDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        OrderItemHeader header = (OrderItemHeader) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_orderitem_header, null);
        }

        TextView number = (TextView) convertView.findViewById(R.id.order_list_head_ordernumber);
        TextView date = (TextView) convertView.findViewById(R.id.order_list_head_date);
        TextView price = (TextView) convertView.findViewById(R.id.order_list_head_price);

        number.setText(Integer.toString(header.getOrderNumber()));
        date.setText(header.getDate());
        price.setText(header.getAmountIncVat()+"kr");

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}