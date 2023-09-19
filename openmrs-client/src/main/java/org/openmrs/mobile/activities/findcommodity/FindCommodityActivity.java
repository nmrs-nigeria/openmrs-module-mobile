package org.openmrs.mobile.activities.findcommodity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;


public class FindCommodityActivity extends ACBaseActivity {

    public FindCommodityContract.Presenter mPresenter;
    FindCommodityFragment consumptionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_commodity);

        consumptionFragment =
                (FindCommodityFragment) getSupportFragmentManager().findFragmentById(R.id.findCommodityContentFrame);
        if (consumptionFragment == null) {
            consumptionFragment = FindCommodityFragment.newInstance();
        }
        if (!consumptionFragment.isActive()) {
            addFragmentToActivity(getSupportFragmentManager(),
                    consumptionFragment, R.id.findCommodityContentFrame);
        }


        mPresenter = new FindCommodityPresenter(consumptionFragment);
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
