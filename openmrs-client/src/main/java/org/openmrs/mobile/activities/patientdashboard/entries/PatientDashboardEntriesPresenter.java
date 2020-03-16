package org.openmrs.mobile.activities.patientdashboard.entries;

import org.openmrs.mobile.activities.patientdashboard.PatientDashboardContract;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardMainPresenterImpl;
import org.openmrs.mobile.dao.PatientDAO;
import org.openmrs.mobile.dao.VisitDAO;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.models.Patient;

import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Arinze on 10/12/2018.
 */

public class PatientDashboardEntriesPresenter extends PatientDashboardMainPresenterImpl implements PatientDashboardContract.PatientEntriesPresenter {

    private PatientDashboardContract.ViewPatientEntries mPatientEntriesView;

    public PatientDashboardEntriesPresenter(String id, PatientDashboardContract.ViewPatientEntries mPatientEntriesView) {
        this.mPatient = new PatientDAO().findPatientByID(id);
        this.mPatientEntriesView = mPatientEntriesView;
        this.mPatientEntriesView.setPresenter(this);

    }

    public PatientDashboardEntriesPresenter(Patient patient,
                                            PatientDashboardContract.ViewPatientEntries mPatientEntriesView) {
        this.mPatient = patient;
        this.mPatientEntriesView = mPatientEntriesView;
        this.mPatientEntriesView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        addSubscription(new VisitDAO().getLocalEncounterByPatientID(mPatient.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(patientEncounters -> {
                    if (patientEncounters != null && patientEncounters.isEmpty()) {
                        mPatientEntriesView.setEmptyListVisibility(false);
                    } else {
                        mPatientEntriesView.setEmptyListVisibility(true);
                        mPatientEntriesView.updateList(patientEncounters);
                    }
                }));
    }


    @Override
    public void startFormDisplayActivityWithEncounter(Encountercreate encounter) {
        mPatientEntriesView.startFormDisplayActivity(encounter);
    }
}
