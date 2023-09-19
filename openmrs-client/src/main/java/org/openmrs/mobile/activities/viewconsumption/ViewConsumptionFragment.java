package org.openmrs.mobile.activities.viewconsumption;

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
import com.google.gson.Gson;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseFragment;
import org.openmrs.mobile.activities.addeditconsumption.AddEditConsumptionActivity;
import org.openmrs.mobile.models.Consumption;
import org.openmrs.mobile.models.Department;
import org.openmrs.mobile.utilities.FontsUtil;

import java.util.ArrayList;
import java.util.List;

public class ViewConsumptionFragment extends ACBaseFragment<ViewConsumptionContract.Presenter> implements ViewConsumptionContract.View {

    private ArrayList<ViewConsumptionRowItem> viewConsumptionRowItems;
    private ViewConsumptionAdapter viewConsumptionAdapter;
    private ArrayList<Long> consumptionId = new ArrayList<Long>();
    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_view_consumption, container, false);
        setHasOptionsMenu(true);
        FontsUtil.setFont((ViewGroup) root);
        listView = (ListView) root.findViewById(R.id.consumptionList);
        viewConsumptionRowItems = new ArrayList<ViewConsumptionRowItem>();

        viewConsumptionAdapter = new ViewConsumptionAdapter(getContext(), viewConsumptionRowItems);
        getConsumption();
        listView.setAdapter(viewConsumptionAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getActivity(), "You clicked at position: " + (position + 1), Toast.LENGTH_SHORT).show();
                Bundle basket = new Bundle();
                basket.putLong("id", consumptionId.get(position));
                Intent intent = new Intent(getActivity(), AddEditConsumptionActivity.class);
                intent.putExtras(basket);
                startActivity(intent);
            }
        });

        return root;
    }

    public static ViewConsumptionFragment newInstance() {
        return new ViewConsumptionFragment();
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

    public void getConsumption() {
        ViewConsumptionRowItem viewConsumptionRowItem;
        List<Consumption> consumption = new Select().all().from(Consumption.class).where("isSynced = ?", 0).execute();
        for(Consumption row: consumption){
            if(row.getItem() != null) {
                row.getConsumptionDate();
                row.getDepartment();
                row.getItem();
                row.getBatchNumber();
                viewConsumptionRowItem = new ViewConsumptionRowItem(row.getConsumptionDate(), row.getDepartment(), row.getItem(), row.getBatchNumber());
                viewConsumptionRowItems.add(viewConsumptionRowItem);
                consumptionId.add(row.getId());
            }
        }
        //Gson gson = new Gson();
        //String s = gson.toJson(viewConsumptionRowItems);
        viewConsumptionAdapter.notifyDataSetChanged();
    }


}
