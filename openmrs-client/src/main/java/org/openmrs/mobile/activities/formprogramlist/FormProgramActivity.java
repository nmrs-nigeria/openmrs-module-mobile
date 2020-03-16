package org.openmrs.mobile.activities.formprogramlist;


import android.os.Bundle;
import android.view.Menu;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.utilities.ApplicationConstants;

public class FormProgramActivity extends ACBaseActivity {
    private String programName = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_program);

        // Create fragment
        FormProgramFragment formProgramFragment =
                (FormProgramFragment) getSupportFragmentManager().findFragmentById(R.id.formProgrameContentFrame);
        if (formProgramFragment == null) {
            formProgramFragment = FormProgramFragment.newInstance();
        }
        if (!formProgramFragment.isActive()) {
            addFragmentToActivity(getSupportFragmentManager(),
                    formProgramFragment, R.id.formProgrameContentFrame);
        }

        Bundle bundle = getIntent().getExtras();
        String mPatientID = "";

        if(bundle != null)
        {
            mPatientID = bundle.getString(ApplicationConstants.BundleKeys.PATIENT_ID_BUNDLE);
            programName = bundle.getString(ApplicationConstants.BundleKeys.PATIENT_PROGRAM);
        }


        // Create the presenter
        new FormProgramPresenter(formProgramFragment, Long.parseLong(mPatientID),programName);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

}
