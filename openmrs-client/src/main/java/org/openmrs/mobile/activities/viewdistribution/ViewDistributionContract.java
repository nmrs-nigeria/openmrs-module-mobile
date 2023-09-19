package org.openmrs.mobile.activities.viewdistribution;

import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;


public interface ViewDistributionContract {

    interface View extends BaseView<Presenter> {

    }

    interface Presenter extends BasePresenterContract {

    }
}
