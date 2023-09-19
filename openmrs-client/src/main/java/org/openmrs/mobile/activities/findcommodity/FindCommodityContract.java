package org.openmrs.mobile.activities.findcommodity;

import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;

public interface FindCommodityContract {

    interface View extends BaseView<org.openmrs.mobile.activities.findcommodity.FindCommodityContract.Presenter> {

        void bindDrawableResources();

    }


    interface Presenter extends BasePresenterContract {

    }
}
