package org.openmrs.mobile.activities.addeditconsumption;

import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;
import org.openmrs.mobile.activities.addeditpatient.AddEditPatientContract;
import org.openmrs.mobile.models.Consumption;
import org.openmrs.mobile.models.Patient;

import java.util.List;

public class AddEditConsumptionContract {
    interface View extends BaseView<AddEditConsumptionContract.Presenter> {

        void finishConsumptionInfoActivity();

        void setErrorsVisibility(boolean consumptionError);

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

        Consumption getConsumptionToUpdate();

        boolean isRegisteringConsumption();

        void confirmRegister(List<Consumption> consumption);

        void confirmUpdate(Consumption consumption);

        void finishConsumptionInfoActivity();

        void registerConsumption();

        void updateConsumption(Consumption consumption);

        long getConsumptionToUpdateId();

        void deleteCommodity();
    }

}

