package org.openmrs.mobile.activities.patientprogram;

import androidx.annotation.NonNull;

import org.openmrs.mobile.activities.BasePresenter;


public class PatientProgramPresenter extends BasePresenter implements PatientProgramContract.Presenter {
    private String patientId;
    // View
    @NonNull
    private final PatientProgramContract.View mPatientProgramView;

    public PatientProgramPresenter(@NonNull PatientProgramContract.View patientProgramView, String patientId) {
        mPatientProgramView= patientProgramView;
        mPatientProgramView.setPresenter(this);
        this.patientId = patientId;
    }

    @Override
    public void subscribe() {
        mPatientProgramView.bindDrawableResources();
        mPatientProgramView.setPatientId(patientId);
    }



}