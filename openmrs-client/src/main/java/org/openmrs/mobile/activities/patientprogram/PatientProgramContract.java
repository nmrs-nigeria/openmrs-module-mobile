package org.openmrs.mobile.activities.patientprogram;

import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;

public interface PatientProgramContract {

    interface View extends BaseView<Presenter> {

        void bindDrawableResources();
        void setPatientId(String Id);

    }

    interface Presenter extends BasePresenterContract {

    }

}