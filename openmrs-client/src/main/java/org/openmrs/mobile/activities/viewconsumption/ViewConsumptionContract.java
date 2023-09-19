package org.openmrs.mobile.activities.viewconsumption;

import org.openmrs.mobile.activities.BasePresenter;
import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;


public interface ViewConsumptionContract {

    interface View extends BaseView<ViewConsumptionContract.Presenter> {

    }

    interface Presenter extends BasePresenterContract {

    }
}
