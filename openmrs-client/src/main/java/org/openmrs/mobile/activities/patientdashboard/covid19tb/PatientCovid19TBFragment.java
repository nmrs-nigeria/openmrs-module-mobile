package org.openmrs.mobile.activities.patientdashboard.covid19tb;

import android.view.View;

import org.openmrs.mobile.activities.patientdashboard.PatientDashboardActivity;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardContract;
import org.openmrs.mobile.activities.patientdashboard.PatientDashboardFragment;
import org.openmrs.mobile.activities.patientdashboard.fingerprint.PatientFingerPrintFragment;

public class PatientCovid19TBFragment extends PatientDashboardFragment implements PatientDashboardContract.ViewPatientCovid19TB{

    private View rootView;
    private PatientDashboardActivity mPatientDashboardActivity;

    public static PatientCovid19TBFragment newInstance() {
        return new PatientCovid19TBFragment();
    }


    @Override
    public void showCovid19TBEncounter() {

    }
}