package org.openmrs.mobile.activities.addeditreceipt;

import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;
import org.openmrs.mobile.models.Receipt;
import org.openmrs.mobile.models.ReceiptItem;

import java.util.List;

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

        void confirmRegister(List<Receipt> receipt, ReceiptItem receiptItem);

        void confirmUpdate(Receipt receipt, ReceiptItem receiptItem);

        void finishReceiptInfoActivity();

        void registerReceipt();

        void deleteCommodity();
    }

}

