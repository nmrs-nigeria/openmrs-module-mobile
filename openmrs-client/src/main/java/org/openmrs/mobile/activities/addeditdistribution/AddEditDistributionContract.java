package org.openmrs.mobile.activities.addeditdistribution;

import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;
import org.openmrs.mobile.models.Distribution;
import org.openmrs.mobile.models.DistributionItem;
import org.openmrs.mobile.models.ReceiptItem;

import java.util.List;

public class AddEditDistributionContract {

    interface View extends BaseView<AddEditDistributionContract.Presenter> {

        void finishDistributionInfoActivity();

        void setErrorsVisibility(boolean distributionError);

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

        Distribution getDistributionToUpdate();

        boolean isRegisteringDistribution();

        void confirmRegister(List<Distribution> distribution, DistributionItem distributionItem);

        void confirmUpdate(Distribution distribution, DistributionItem distributionItem);

        void finishDistributionInfoActivity();

        void registerDistribution();

        void deleteCommodity();
    }

}

