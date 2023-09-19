package org.openmrs.mobile.activities.viewconsumption;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.activities.addeditconsumption.AddEditConsumptionContract;
import org.openmrs.mobile.activities.addeditconsumption.AddEditConsumptionPresenter;
import org.openmrs.mobile.activities.commodity.CommodityFragment;

public class ViewConsumptionActivity extends ACBaseActivity {

    public ViewConsumptionContract.Presenter mPresenter;
    ViewConsumptionFragment consumptionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_consumption);

        consumptionFragment =
                (ViewConsumptionFragment) getSupportFragmentManager().findFragmentById(R.id.viewConsumptionContentFrame);
        if (consumptionFragment == null) {
            consumptionFragment = ViewConsumptionFragment.newInstance();
        }
        if (!consumptionFragment.isActive()) {
            addFragmentToActivity(getSupportFragmentManager(),
                    consumptionFragment, R.id.viewConsumptionContentFrame);
        }


        mPresenter = new ViewConsumptionPresenter(consumptionFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}