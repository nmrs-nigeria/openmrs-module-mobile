package org.openmrs.mobile.activities.viewtransfer;

import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;


public interface ViewTransferContract {

    interface View extends BaseView<Presenter> {

    }

    interface Presenter extends BasePresenterContract {

    }
}
