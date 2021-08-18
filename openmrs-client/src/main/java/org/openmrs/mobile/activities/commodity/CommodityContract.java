package org.openmrs.mobile.activities.commodity;

import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;

public interface CommodityContract {

    interface View extends BaseView<Presenter> {

        void bindDrawableResources();
//        void setPatientId(String Id);

    }

    interface Presenter extends BasePresenterContract {

    }

}