package org.openmrs.mobile.activities.viewtransfer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.openmrs.mobile.R;

import java.util.ArrayList;

public class ViewTransferAdapter extends BaseAdapter {

    private ArrayList<ViewTransferRowItem> singleRow;
    private LayoutInflater thisInflater;
    private Context context;

    public ViewTransferAdapter(Context context, ArrayList<ViewTransferRowItem> aRow) {
        this.singleRow = aRow;
        thisInflater = (LayoutInflater.from(context));
        this.context = context;
    }

    public void addListItemToAdapter(ArrayList<ViewTransferRowItem> myRowItems) {
        //Add list to current array list of data
        singleRow.addAll(myRowItems);
        //Notify UI
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return singleRow.size();
    }

    @Override
    public Object getItem(int position) {
        return singleRow.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        convertView = thisInflater.inflate(R.layout.row_commodity_details, parent, false);
        holder.vDate = (TextView) convertView.findViewById(R.id.consumptionDate);
        holder.vDepartment = (TextView) convertView.findViewById(R.id.department);
        holder.vItemBatch = (TextView) convertView.findViewById(R.id.itemBatch);
        holder.vItem = (TextView) convertView.findViewById(R.id.item);

        ViewTransferRowItem currentRow = (ViewTransferRowItem) getItem(position);

        holder.vDate.setText(currentRow.getDisplayDate());
        holder.vDepartment.setText(currentRow.getDisplayCommodityType());
        holder.vItem.setText(currentRow.getDisplayItem());
        holder.vItemBatch.setText(currentRow.getDisplayItemBatch());
        //sholder.postDate.setText(currentRow.getDate());

        convertView.setTag(holder);
        return convertView;
    }

    public class ViewHolder {
        public TextView vDate;
        public TextView vDepartment;
        public TextView vItemBatch;
        public TextView vItem;
    }
}

