package org.openmrs.mobile.activities.addeditreceipt;

import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;
import org.openmrs.mobile.models.Receipt;

public class AddEditReceiptContract {
    interface View extends BaseView<AddEditReceiptContract.Presenter> {

        void finishReceiptInfoActivity();

        void setErrorsVisibility(boolean receiptError);

        void scrollToTop();

        void hideSoftKeys();

        void setProgressBarVisibility(boolean visibility);

//        void showSimilarPatientDialog(List<Patient> patients, Patient newPatient);
//
//        void startCommodityDashbordActivity(Patient patient);
        void startCommodityDashboardActivity();
//
//        void showUpgradeRegistrationModuleInfo();
//
        boolean areFieldsNotEmpty();
    }

    interface Presenter extends BasePresenterContract {

        Receipt getReceiptToUpdate();

        boolean isRegisteringReceipt();

        void confirmRegister(Receipt receipt);

        void confirmUpdate(Receipt receipt);

        void finishReceiptInfoActivity();

        void registerReceipt();

        void updateReceipt(Receipt receipt);
    }

}

