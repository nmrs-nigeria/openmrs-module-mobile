package org.openmrs.mobile.activities.viewdistribution;

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
import org.openmrs.mobile.activities.addeditdistribution.AddEditDistributionActivity;
import org.openmrs.mobile.models.Distribution;
import org.openmrs.mobile.models.DistributionItem;
import org.openmrs.mobile.utilities.FontsUtil;

import java.util.ArrayList;
import java.util.List;

public class ViewDistributionFragment extends ACBaseFragment<ViewDistributionContract.Presenter> implements ViewDistributionContract.View {

    private ArrayList<ViewDistributionRowItem> viewDistributionRowItems;
    private ViewDistributionAdapter viewDistributionAdapter;
    private ArrayList<Long> distributionId = new ArrayList<Long>();
    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_view_distribution, container, false);
        setHasOptionsMenu(true);
        FontsUtil.setFont((ViewGroup) root);
        listView = (ListView) root.findViewById(R.id.distributionList);
        viewDistributionRowItems = new ArrayList<ViewDistributionRowItem>();

        viewDistributionAdapter = new ViewDistributionAdapter(getContext(), viewDistributionRowItems);
        getDistribution();
        listView.setAdapter(viewDistributionAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getActivity(), "You clicked at position: " + (position + 1), Toast.LENGTH_SHORT).show();
                Bundle basket = new Bundle();
                basket.putLong("id", distributionId.get(position));
                Intent intent = new Intent(getActivity(), AddEditDistributionActivity.class);
                intent.putExtras(basket);
                startActivity(intent);
            }
        });

        return root;
    }

    public static ViewDistributionFragment newInstance() {
        return new ViewDistributionFragment();
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

    public void getDistribution() {
        ViewDistributionRowItem viewDistributionRowItem;
        List<Distribution> distribution = new Select().all().from(Distribution.class).where("isSynced = ?", 0).execute();

        for (Distribution row : distribution) {

            DistributionItem distributionItem = new Select().from(DistributionItem.class).where("isSynced = ? AND distributionId = ?", 0, row.getId()).executeSingle();
            if (distributionItem != null) {
                viewDistributionRowItem = new ViewDistributionRowItem(row.getOperationDate(), row.getCommodityType(), distributionItem.getItem(), distributionItem.getItemBatch());
                viewDistributionRowItems.add(viewDistributionRowItem);
                distributionId.add(row.getId());
            }
        }
        viewDistributionAdapter.notifyDataSetChanged();
    }

}
