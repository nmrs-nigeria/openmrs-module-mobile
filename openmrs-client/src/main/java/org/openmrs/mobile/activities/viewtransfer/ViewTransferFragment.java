package org.openmrs.mobile.activities.viewtransfer;

import android.content.Intent;
import android.os.Bundle;
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
import org.openmrs.mobile.activities.addedittransfer.AddEditTransferActivity;
import org.openmrs.mobile.models.Transfer;
import org.openmrs.mobile.models.TransferItem;
import org.openmrs.mobile.utilities.FontsUtil;

import java.util.ArrayList;
import java.util.List;

public class ViewTransferFragment extends ACBaseFragment<ViewTransferContract.Presenter> implements ViewTransferContract.View {

    private ArrayList<ViewTransferRowItem> viewTransferRowItems;
    private ViewTransferAdapter viewTransferAdapter;
    private ArrayList<Long> transferId = new ArrayList<Long>();
    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_view_transfer, container, false);
        setHasOptionsMenu(true);
        FontsUtil.setFont((ViewGroup) root);
        listView = (ListView) root.findViewById(R.id.transferList);
        viewTransferRowItems = new ArrayList<ViewTransferRowItem>();

        viewTransferAdapter = new ViewTransferAdapter(getContext(), viewTransferRowItems);
        getTransfer();
        listView.setAdapter(viewTransferAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getActivity(), "You clicked at position: " + (position + 1), Toast.LENGTH_SHORT).show();
                Bundle basket = new Bundle();
                basket.putLong("id", transferId.get(position));
                Intent intent = new Intent(getActivity(), AddEditTransferActivity.class);
                intent.putExtras(basket);
                startActivity(intent);
            }
        });

        return root;
    }

    public static ViewTransferFragment newInstance() {
        return new ViewTransferFragment();
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

    public void getTransfer() {
        ViewTransferRowItem viewTransferRowItem;
        List<Transfer> transfer = new Select().all().from(Transfer.class).where("isSynced = ?", 0).execute();

        for (Transfer row : transfer) {

            TransferItem transferItem = new Select().from(TransferItem.class).where("isSynced = ? AND transferId = ?", 0, row.getId()).executeSingle();
            if (transferItem != null) {
                viewTransferRowItem = new ViewTransferRowItem(row.getOperationDate(), row.getCommodityType(), transferItem.getItem(), transferItem.getItemBatch());
                viewTransferRowItems.add(viewTransferRowItem);
                transferId.add(row.getId());
            }
        }
        viewTransferAdapter.notifyDataSetChanged();
    }

}
