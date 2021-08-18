package org.openmrs.mobile.activities.commodity;

import androidx.annotation.NonNull;

import org.openmrs.mobile.activities.BasePresenter;


public class CommodityPresenter extends BasePresenter implements CommodityContract.Presenter {
//    private String patientId;
    // View
    @NonNull
    private final CommodityContract.View mCommodityView;

    public CommodityPresenter(@NonNull CommodityContract.View commodityView) {
        mCommodityView= commodityView;
        mCommodityView.setPresenter(this);
//        this.patientId = patientId;
    }

    @Override
    public void subscribe() {
        mCommodityView.bindDrawableResources();
//        mPatientProgramView.setPatientId(patientId);
    }



}