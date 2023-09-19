package org.openmrs.mobile.activities.viewtransfer;

import org.openmrs.mobile.activities.BasePresenter;

public class ViewTransferPresenter extends BasePresenter implements ViewTransferContract.Presenter {

    private final ViewTransferContract.View mViewTransferView;

    public ViewTransferPresenter(ViewTransferContract.View viewTransferContract){
        mViewTransferView = viewTransferContract;
        mViewTransferView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        //Do nothing for now
        //mViewTransferView.bind
    }
}
