package org.openmrs.mobile.activities.viewadjustment;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;

public class ViewAdjustmentActivity extends ACBaseActivity {

    public ViewAdjustmentContract.Presenter mPresenter;
    ViewAdjustmentFragment consumptionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_adjustment);

        consumptionFragment =
                (ViewAdjustmentFragment) getSupportFragmentManager().findFragmentById(R.id.viewAdjustmentContentFrame);
        if (consumptionFragment == null) {
            consumptionFragment = ViewAdjustmentFragment.newInstance();
        }
        if (!consumptionFragment.isActive()) {
            addFragmentToActivity(getSupportFragmentManager(),
                    consumptionFragment, R.id.viewAdjustmentContentFrame);
        }


        mPresenter = new ViewAdjustmentPresenter(consumptionFragment);
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