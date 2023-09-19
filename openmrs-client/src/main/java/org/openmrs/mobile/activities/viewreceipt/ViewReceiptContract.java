package org.openmrs.mobile.activities.viewreceipt;

import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;


public interface ViewReceiptContract {

    interface View extends BaseView<Presenter> {

    }

    interface Presenter extends BasePresenterContract {

    }
}
