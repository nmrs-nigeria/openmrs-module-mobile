package org.openmrs.mobile.activities.viewdistribution;

import org.openmrs.mobile.activities.BasePresenter;

public class ViewDistributionPresenter extends BasePresenter implements ViewDistributionContract.Presenter {

    private final ViewDistributionContract.View mViewDistributionView;

    public ViewDistributionPresenter(ViewDistributionContract.View viewDistributionContract){
        mViewDistributionView = viewDistributionContract;
        mViewDistributionView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        //Do nothing for now
        //mViewDistributionView.bind
    }
}
