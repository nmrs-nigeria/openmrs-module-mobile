package org.openmrs.mobile.activities.viewadjustment;

import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;


public interface ViewAdjustmentContract {

    interface View extends BaseView<Presenter> {

    }

    interface Presenter extends BasePresenterContract {

    }
}
