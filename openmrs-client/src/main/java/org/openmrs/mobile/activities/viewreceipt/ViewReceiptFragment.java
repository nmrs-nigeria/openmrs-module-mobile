package org.openmrs.mobile.activities.viewreceipt;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.activeandroid.query.Select;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseFragment;
import org.openmrs.mobile.activities.addeditreceipt.AddEditReceiptActivity;
import org.openmrs.mobile.application.OpenMRSCustomHandler;
import org.openmrs.mobile.models.Receipt;
import org.openmrs.mobile.models.ReceiptItem;
import org.openmrs.mobile.utilities.FontsUtil;

import java.util.ArrayList;
import java.util.List;

public class ViewReceiptFragment extends ACBaseFragment<ViewReceiptContract.Presenter> implements ViewReceiptContract.View {

    private ArrayList<ViewReceiptRowItem> viewReceiptRowItems;
    private ViewReceiptAdapter viewReceiptAdapter;
    private ArrayList<Long> receiptId = new ArrayList<Long>();
    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_view_receipt, container, false);
        setHasOptionsMenu(true);
        FontsUtil.setFont((ViewGroup) root);
        listView = (ListView) root.findViewById(R.id.receiptList);
        viewReceiptRowItems = new ArrayList<ViewReceiptRowItem>();

        viewReceiptAdapter = new ViewReceiptAdapter(getContext(), viewReceiptRowItems);
        getReceipt();
        listView.setAdapter(viewReceiptAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getActivity(), "You clicked at position: " + (position + 1), Toast.LENGTH_SHORT).show();
                Bundle basket = new Bundle();
                basket.putLong("id", receiptId.get(position));
                Intent intent = new Intent(getActivity(), AddEditReceiptActivity.class);
                intent.putExtras(basket);
                startActivity(intent);
            }
        });

        return root;
    }

    public static ViewReceiptFragment newInstance() {
        return new ViewReceiptFragment();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.actionSubmit:

                return true;
            default:
                // Do nothing
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getReceipt() {
        ViewReceiptRowItem viewReceiptRowItem;
        List<Receipt> receipt = new Select().all().from(Receipt.class).where("isSynced = ?", 0).execute();

        for (Receipt row : receipt) {

            ReceiptItem receiptItem = new Select().from(ReceiptItem.class).where("isSynced = ? AND receiptId = ?", 0, row.getId()).executeSingle();
            if (receiptItem != null) {
                viewReceiptRowItem = new ViewReceiptRowItem(row.getOperationDate(), row.getCommodityType(), receiptItem.getItem(), receiptItem.getItemBatch());
                viewReceiptRowItems.add(viewReceiptRowItem);
                receiptId.add(row.getId());
            }
        }
        viewReceiptAdapter.notifyDataSetChanged();
    }

}
