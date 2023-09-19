package org.openmrs.mobile.activities.viewtransfer;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;

public class ViewTransferActivity extends ACBaseActivity {

    public ViewTransferContract.Presenter mPresenter;
    ViewTransferFragment consumptionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_transfer);

        consumptionFragment =
                (ViewTransferFragment) getSupportFragmentManager().findFragmentById(R.id.viewTransferContentFrame);
        if (consumptionFragment == null) {
            consumptionFragment = ViewTransferFragment.newInstance();
        }
        if (!consumptionFragment.isActive()) {
            addFragmentToActivity(getSupportFragmentManager(),
                    consumptionFragment, R.id.viewTransferContentFrame);
        }


        mPresenter = new ViewTransferPresenter(consumptionFragment);
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