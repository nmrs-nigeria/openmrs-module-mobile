package org.openmrs.mobile.activities.commodity;


import android.os.Bundle;
import android.view.Menu;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.utilities.ApplicationConstants;


public class CommodityActivity extends ACBaseActivity {
    public CommodityFragment commodityFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commodity);

        // Create fragment
        commodityFragment =
                (CommodityFragment) getSupportFragmentManager().findFragmentById(R.id.commodityContentFrame);
        if (commodityFragment == null) {
            commodityFragment = CommodityFragment.newInstance();
        }
        if (!commodityFragment.isActive()) {
            addFragmentToActivity(getSupportFragmentManager(),
                    commodityFragment, R.id.commodityContentFrame);
        }

        //Check if bundle includes patient ID
//        Bundle patientBundle = savedInstanceState;
//        if (patientBundle != null) {
//            patientBundle.getString(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE);
//        } else {
//            patientBundle = getIntent().getExtras();
//        }
//        String patientID = "";
//        if (patientBundle != null) {
//            patientID = patientBundle.getString(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE);
//        }


        // Create the presenter
        new CommodityPresenter(commodityFragment);
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
}
