package org.openmrs.mobile.activities.findcommodity;

import androidx.annotation.NonNull;

import org.openmrs.mobile.activities.BasePresenter;
import org.openmrs.mobile.activities.commodity.CommodityContract;


public class FindCommodityPresenter extends BasePresenter implements FindCommodityContract.Presenter {

    private final FindCommodityContract.View mFindCommodityView;

    public FindCommodityPresenter(FindCommodityContract.View FindConsumptionContract) {
        mFindCommodityView = FindConsumptionContract;
        mFindCommodityView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        mFindCommodityView.bindDrawableResources();
    }
}
