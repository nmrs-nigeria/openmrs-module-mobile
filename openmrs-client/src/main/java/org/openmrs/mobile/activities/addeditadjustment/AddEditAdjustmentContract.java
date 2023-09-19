package org.openmrs.mobile.activities.addeditadjustment;

import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;
import org.openmrs.mobile.models.Adjustment;
import org.openmrs.mobile.models.AdjustmentItem;
import org.openmrs.mobile.models.ReceiptItem;

import java.util.List;

public class AddEditAdjustmentContract {

    interface View extends BaseView<AddEditAdjustmentContract.Presenter> {

        void finishAdjustmentInfoActivity();

        void setErrorsVisibility(boolean adjustmentError);

        void scrollToTop();

        void startCommodityDashboardActivity();

        void hideSoftKeys();

        void setProgressBarVisibility(boolean visibility);

        boolean areFieldsNotEmpty();
    }

    interface Presenter extends BasePresenterContract {

        Adjustment getAdjustmentToUpdate();

        boolean isRegisteringAdjustment();

        void confirmRegister(List<Adjustment> adjustment, AdjustmentItem adjustmentItem);

        void confirmUpdate(Adjustment adjustment, AdjustmentItem adjustmentItem);

        void finishAdjustmentInfoActivity();

        void registerAdjustment();

        void deleteCommodity();
    }

}

