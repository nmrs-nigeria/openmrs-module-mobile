package org.openmrs.mobile.activities.addedittransfer;

import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;
import org.openmrs.mobile.models.Transfer;
import org.openmrs.mobile.models.TransferItem;

import java.util.List;

public class AddEditTransferContract {

    interface View extends BaseView<AddEditTransferContract.Presenter> {

        void finishTransferInfoActivity();

        void setErrorsVisibility(boolean transferError);

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

        Transfer getTransferToUpdate();

        boolean isRegisteringTransfer();

        void confirmRegister(List<Transfer> transfer, TransferItem transferItem);

        void confirmUpdate(Transfer transfer, TransferItem transferItem);

        void finishTransferInfoActivity();

        void registerTransfer();

        void deleteCommodity();
    }

}

