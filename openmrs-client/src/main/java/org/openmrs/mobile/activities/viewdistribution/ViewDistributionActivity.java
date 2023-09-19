package org.openmrs.mobile.activities.viewdistribution;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;

public class ViewDistributionActivity extends ACBaseActivity {

    public ViewDistributionContract.Presenter mPresenter;
    ViewDistributionFragment consumptionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_distribution);

        consumptionFragment =
                (ViewDistributionFragment) getSupportFragmentManager().findFragmentById(R.id.viewDistributionContentFrame);
        if (consumptionFragment == null) {
            consumptionFragment = ViewDistributionFragment.newInstance();
        }
        if (!consumptionFragment.isActive()) {
            addFragmentToActivity(getSupportFragmentManager(),
                    consumptionFragment, R.id.viewDistributionContentFrame);
        }


        mPresenter = new ViewDistributionPresenter(consumptionFragment);
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