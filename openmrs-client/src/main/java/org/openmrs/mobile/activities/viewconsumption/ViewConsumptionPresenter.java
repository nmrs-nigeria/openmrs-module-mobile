package org.openmrs.mobile.activities.viewconsumption;

import org.openmrs.mobile.activities.BasePresenter;

import javax.annotation.Nonnull;

public class ViewConsumptionPresenter extends BasePresenter implements ViewConsumptionContract.Presenter {

    private final ViewConsumptionContract.View mViewConsumptionView;

    public ViewConsumptionPresenter(ViewConsumptionContract.View viewConsumptionContract){
        mViewConsumptionView = viewConsumptionContract;
        mViewConsumptionView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        //Do nothing for now
        //mViewConsumptionView.bind
    }
}
