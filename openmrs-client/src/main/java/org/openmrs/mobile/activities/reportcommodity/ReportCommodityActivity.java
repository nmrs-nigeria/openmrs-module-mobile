package org.openmrs.mobile.activities.reportcommodity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;


public class ReportCommodityActivity extends ACBaseActivity {

    public ReportCommodityContract.Presenter mPresenter;
    ReportCommodityFragment reportFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_commodity);

        reportFragment =
                (ReportCommodityFragment) getSupportFragmentManager().findFragmentById(R.id.reportCommodityContentFrame);
        if (reportFragment == null) {
            reportFragment = ReportCommodityFragment.newInstance();
        }
        if (!reportFragment.isActive()) {
            addFragmentToActivity(getSupportFragmentManager(),
                    reportFragment, R.id.reportCommodityContentFrame);
        }


        mPresenter = new ReportCommodityPresenter(reportFragment);
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
