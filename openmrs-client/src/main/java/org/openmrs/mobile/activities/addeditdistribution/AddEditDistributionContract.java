package org.openmrs.mobile.activities.addeditdistribution;

import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;
import org.openmrs.mobile.models.Distribution;

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

        void confirmRegister(Distribution distribution);

        void confirmUpdate(Distribution distribution);

        void finishDistributionInfoActivity();

        void registerDistribution();

        void updateDistribution(Distribution distribution);
    }

}

