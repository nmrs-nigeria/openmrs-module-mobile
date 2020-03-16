package org.openmrs.mobile.activities.patientprogram;


import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.utilities.ApplicationConstants;


public class PatientProgramActivity extends ACBaseActivity {
    public PatientProgramFragment patientProgramFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_program);

        // Create fragment
        patientProgramFragment =
                (PatientProgramFragment) getSupportFragmentManager().findFragmentById(R.id.patientProgrameContentFrame);
        if (patientProgramFragment == null) {
            patientProgramFragment = PatientProgramFragment.newInstance();
        }
        if (!patientProgramFragment.isActive()) {
            addFragmentToActivity(getSupportFragmentManager(),
                    patientProgramFragment, R.id.patientProgrameContentFrame);
        }

        //Check if bundle includes patient ID
        Bundle patientBundle = savedInstanceState;
        if (patientBundle != null) {
            patientBundle.getString(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE);
        } else {
            patientBundle = getIntent().getExtras();
        }
        String patientID = "";
        if (patientBundle != null) {
            patientID = patientBundle.getString(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE);
        }


        // Create the presenter
        new PatientProgramPresenter(patientProgramFragment, patientID);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
