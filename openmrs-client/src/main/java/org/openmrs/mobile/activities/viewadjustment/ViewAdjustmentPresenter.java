package org.openmrs.mobile.activities.viewadjustment;

import org.openmrs.mobile.activities.BasePresenter;

public class ViewAdjustmentPresenter extends BasePresenter implements ViewAdjustmentContract.Presenter {

    private final ViewAdjustmentContract.View mViewAdjustmentView;

    public ViewAdjustmentPresenter(ViewAdjustmentContract.View viewAdjustmentContract){
        mViewAdjustmentView = viewAdjustmentContract;
        mViewAdjustmentView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        //Do nothing for now
        //mViewAdjustmentView.bind
    }
}
