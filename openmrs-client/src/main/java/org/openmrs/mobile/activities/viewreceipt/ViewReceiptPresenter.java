package org.openmrs.mobile.activities.viewreceipt;

import org.openmrs.mobile.activities.BasePresenter;

public class ViewReceiptPresenter extends BasePresenter implements ViewReceiptContract.Presenter {

    private final ViewReceiptContract.View mViewReceiptView;

    public ViewReceiptPresenter(ViewReceiptContract.View viewReceiptContract){
        mViewReceiptView = viewReceiptContract;
        mViewReceiptView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        //Do nothing for now
        //mViewReceiptView.bind
    }
}
