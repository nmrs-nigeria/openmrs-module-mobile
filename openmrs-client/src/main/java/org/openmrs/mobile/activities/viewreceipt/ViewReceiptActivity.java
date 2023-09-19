package org.openmrs.mobile.activities.viewreceipt;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;

public class ViewReceiptActivity extends ACBaseActivity {

    public ViewReceiptContract.Presenter mPresenter;
    ViewReceiptFragment consumptionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_receipt);

        consumptionFragment =
                (ViewReceiptFragment) getSupportFragmentManager().findFragmentById(R.id.viewReceiptContentFrame);
        if (consumptionFragment == null) {
            consumptionFragment = ViewReceiptFragment.newInstance();
        }
        if (!consumptionFragment.isActive()) {
            addFragmentToActivity(getSupportFragmentManager(),
                    consumptionFragment, R.id.viewReceiptContentFrame);
        }


        mPresenter = new ViewReceiptPresenter(consumptionFragment);
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