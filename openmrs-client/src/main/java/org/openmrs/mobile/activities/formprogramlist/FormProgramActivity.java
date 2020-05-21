package org.openmrs.mobile.activities.formprogramlist;


import android.os.Bundle;
import android.view.Menu;

import org.openmrs.mobile.R;
import org.openmrs.mobile.activities.ACBaseActivity;
import org.openmrs.mobile.dao.VisitDAO;
import org.openmrs.mobile.models.Encountercreate;
import org.openmrs.mobile.utilities.ApplicationConstants;

import java.util.List;

public class FormProgramActivity extends ACBaseActivity {
    private String programName = null;
    private boolean isFirstTime = false;
    private boolean isEnrolled = false;
    private boolean isEligible = false;
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
        List<Encountercreate> encountercreateList = new VisitDAO().getLocalEncounterByPatientIDStr(Long.parseLong(mPatientID), "Client intake form");
        if(encountercreateList.isEmpty()){
            this.isFirstTime = true;
        }else{
            this.isFirstTime = false;
        }

        List<Encountercreate> encountercreateListEligible = new VisitDAO().getLocalEncounterByPatientIDEligible(Long.parseLong(mPatientID), "Client intake form","Yes");
        if(encountercreateListEligible.isEmpty()){
            this.isEligible = false;
        }else{
            this.isEligible = true;
        }
        List<Encountercreate> encountercreateListEnrolled = new VisitDAO().getLocalEncounterByPatientIDStr(Long.parseLong(mPatientID), "HIV Enrollment");
        if(encountercreateListEnrolled.isEmpty()){
            this.isEnrolled = false;
        }else{
            this.isEnrolled = true;
        }


        // Create the presenter
        new FormProgramPresenter(formProgramFragment, Long.parseLong(mPatientID),programName);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    public boolean isFirstTime() {
        return isFirstTime;
    }

    public boolean isEnrolled() {
        return isEnrolled;
    }

    public boolean isEligible() {
        return isEligible;
    }

    public void setEnrolled(boolean enrolled) {
        isEnrolled = enrolled;
    }

    public void setEligible(boolean eligible) {
        isEligible = eligible;
    }
}
