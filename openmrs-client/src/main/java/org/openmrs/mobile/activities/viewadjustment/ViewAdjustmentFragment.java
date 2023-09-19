package org.openmrs.mobile.activities.viewadjustment;

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
import org.openmrs.mobile.activities.addeditadjustment.AddEditAdjustmentActivity;
import org.openmrs.mobile.models.Adjustment;
import org.openmrs.mobile.models.AdjustmentItem;
import org.openmrs.mobile.utilities.FontsUtil;

import java.util.ArrayList;
import java.util.List;

public class ViewAdjustmentFragment extends ACBaseFragment<ViewAdjustmentContract.Presenter> implements ViewAdjustmentContract.View {

    private ArrayList<ViewAdjustmentRowItem> viewAdjustmentRowItems;
    private ViewAdjustmentAdapter viewAdjustmentAdapter;
    private ArrayList<Long> adjustmentId = new ArrayList<Long>();
    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_view_adjustment, container, false);
        setHasOptionsMenu(true);
        FontsUtil.setFont((ViewGroup) root);
        listView = (ListView) root.findViewById(R.id.adjustmentList);
        viewAdjustmentRowItems = new ArrayList<ViewAdjustmentRowItem>();

        viewAdjustmentAdapter = new ViewAdjustmentAdapter(getContext(), viewAdjustmentRowItems);
        getAdjustment();
        listView.setAdapter(viewAdjustmentAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getActivity(), "You clicked at position: " + (position + 1), Toast.LENGTH_SHORT).show();
                Bundle basket = new Bundle();
                basket.putLong("id", adjustmentId.get(position));
                Intent intent = new Intent(getActivity(), AddEditAdjustmentActivity.class);
                intent.putExtras(basket);
                startActivity(intent);
            }
        });

        return root;
    }

    public static ViewAdjustmentFragment newInstance() {
        return new ViewAdjustmentFragment();
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

    public void getAdjustment() {
        ViewAdjustmentRowItem viewAdjustmentRowItem;
        List<Adjustment> adjustment = new Select().all().from(Adjustment.class).where("isSynced = ?", 0).execute();

        for (Adjustment row : adjustment) {

            AdjustmentItem adjustmentItem = new Select().from(AdjustmentItem.class).where("isSynced = ? AND adjustmentId = ?", 0, row.getId()).executeSingle();
            if (adjustmentItem != null) {
                viewAdjustmentRowItem = new ViewAdjustmentRowItem(row.getOperationDate(), row.getCommodityType(), adjustmentItem.getItem(), adjustmentItem.getItemBatch());
                viewAdjustmentRowItems.add(viewAdjustmentRowItem);
                adjustmentId.add(row.getId());
            }
        }
        viewAdjustmentAdapter.notifyDataSetChanged();
    }

}
