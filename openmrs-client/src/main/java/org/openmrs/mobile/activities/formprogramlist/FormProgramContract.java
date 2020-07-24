package org.openmrs.mobile.activities.formprogramlist;

import org.openmrs.mobile.activities.BasePresenterContract;
import org.openmrs.mobile.activities.BaseView;

import java.util.List;

public interface FormProgramContract {
    interface View extends BaseView<Presenter> {

        void showFormList(String[] forms, String programName, List<String> formName, boolean isEligible, boolean isEnrolled, boolean isFirstTime, boolean isCompleted, boolean isPositive, boolean isClientExist);

        void startFormDisplayActivity(String formName, Long patientId, String valueRefString, String encounterType);

        void showError(String message);

        void bindDrawableResources();

//        Boolean formCreate(String uuid, String formName);
    }

    interface Presenter extends BasePresenterContract {

        void loadFormResourceList();

        void listItemClicked(int position, String formName);
    }

}
