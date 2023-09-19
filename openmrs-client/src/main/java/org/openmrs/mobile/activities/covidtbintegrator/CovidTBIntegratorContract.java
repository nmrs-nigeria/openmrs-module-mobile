package org.openmrs.mobile.activities.covidtbintegrator;

import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;
import org.openmrs.mobile.models.Adjustment;
import org.openmrs.mobile.models.CovidTBIntegrator;

import java.util.List;



public class CovidTBIntegratorContract {


        interface View extends BaseView<org.openmrs.mobile.activities.covidtbintegrator.CovidTBIntegratorContract.Presenter> {
        void finishCovidTBIntegratorInfoActivity();

        void setErrorsVisibility(boolean covidTBIntegratorError);

        void scrollToTop();

        void startCommodityDashboardActivity();

        void hideSoftKeys();

        void setProgressBarVisibility(boolean visibility);

        boolean areFieldsNotEmpty();
    }

    interface Presenter extends BasePresenterContract {

        CovidTBIntegrator getCovidTBIntegratorToUpdate();

        boolean isRegisteringCovidTBIntegrator();

        void confirmRegister(List<CovidTBIntegrator> covidTBIntegrator);

        void confirmUpdate(CovidTBIntegrator covidTBIntegrator);

        void finishCovidTBIntegratorInfoActivity();

        void registerCovidTBIntegrator();

        void deleteCovidTBIntegrator();
    }

}

