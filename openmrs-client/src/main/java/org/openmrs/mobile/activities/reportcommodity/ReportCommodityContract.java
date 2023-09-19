package org.openmrs.mobile.activities.reportcommodity;

import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;

public interface ReportCommodityContract {

    interface View extends BaseView<Presenter> {

        void bindDrawableResources();

    }


    interface Presenter extends BasePresenterContract {

    }
}
