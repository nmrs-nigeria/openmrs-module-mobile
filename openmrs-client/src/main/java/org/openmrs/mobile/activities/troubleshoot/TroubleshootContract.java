package org.openmrs.mobile.activities.troubleshoot;

import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;
import org.openmrs.mobile.activities.addeditreceipt.AddEditReceiptContract;
import org.openmrs.mobile.models.Receipt;
import org.openmrs.mobile.models.ReceiptItem;

import java.util.List;

public class TroubleshootContract {
    interface View extends BaseView<TroubleshootContract.Presenter> {

        void finishTroubleshootActivity();

        void setErrorsVisibility(boolean receiptError);

        void scrollToTop();

        void hideSoftKeys();

        void setProgressBarVisibility(boolean visibility);

        void startTroubleshootActivity();

    }

    interface Presenter extends BasePresenterContract {


    }

}
